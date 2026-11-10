package com.orlo.app.repository

import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseUser
import com.orlo.app.data.datasource.ArmarioRemoteDataSource
import com.orlo.app.data.datasource.AsesorRemoteDataSource
import com.orlo.app.data.datasource.AuthRemoteDataSource
import com.orlo.app.data.datasource.ConversacionRemoteDataSource
import com.orlo.app.data.datasource.UsuarioRemoteDataSource
import com.orlo.app.data.dtos.EstiloFirestoreDto
import com.orlo.app.data.dtos.GuardarMensajeDto
import com.orlo.app.data.dtos.MensajeAsesorDto
import com.orlo.app.data.dtos.OutfitAsesorDto
import com.orlo.app.data.dtos.PrendaFirestoreDto
import com.orlo.app.data.dtos.ResultadoFirestoreDto
import com.orlo.app.data.dtos.RespuestaAsesorDto
import com.orlo.app.data.dtos.UsuarioFirestoreDto
import com.orlo.app.data.model.AccionRapida
import com.orlo.app.data.model.MensajeChatUi
import com.orlo.app.data.model.RolMensaje
import com.orlo.app.data.repository.AsesorRepository
import com.orlo.app.data.repository.LimiteAlcanzadoException
import com.orlo.app.data.repository.SinSesionException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

// El repositorio arma lo que el asesor sabe del usuario con el perfil y el armario, llama al
// backend y guarda el intercambio. Todos los data sources moqueados
class AsesorRepositoryTest {

    private lateinit var asesorDataSource: AsesorRemoteDataSource
    private lateinit var conversacionDataSource: ConversacionRemoteDataSource
    private lateinit var usuarioDataSource: UsuarioRemoteDataSource
    private lateinit var armarioDataSource: ArmarioRemoteDataSource
    private lateinit var authDataSource: AuthRemoteDataSource
    private lateinit var repository: AsesorRepository

    private val laura = UsuarioFirestoreDto(
        name = "Laura Gómez",
        currentResult = ResultadoFirestoreDto(primary = EstiloFirestoreDto(code = "urbano", name = "Urbano")),
    )

    @Before
    fun setUp() {
        asesorDataSource = mockk()
        conversacionDataSource = mockk()
        usuarioDataSource = mockk()
        armarioDataSource = mockk()
        authDataSource = mockk()
        val usuario = mockk<FirebaseUser>()
        every { usuario.uid } returns "user_1"
        every { authDataSource.currentUser } returns usuario
        coEvery { usuarioDataSource.getUsuario("user_1") } returns laura
        coEvery { armarioDataSource.getPrendas("user_1") } returns emptyList()
        repository = AsesorRepository(asesorDataSource, conversacionDataSource, usuarioDataSource, armarioDataSource, authDataSource)
    }

    @Test
    fun preguntar_conversacionNueva_guardaPreguntaYRespuestaJuntas() = runTest {
        //Arrange
        coEvery { asesorDataSource.enviarMensaje(any()) } returns RespuestaAsesorDto(
            reply = "Dos ideas",
            outfits = listOf(OutfitAsesorDto(title = "Fresco", items = listOf("Jean", "Camiseta"), why = "Cómodo")),
            remainingMessages = 9,
        )
        val pregunta = slot<GuardarMensajeDto>()
        val respuesta = slot<GuardarMensajeDto>()
        coEvery {
            conversacionDataSource.guardarIntercambio("user_1", null, any(), capture(pregunta), capture(respuesta))
        } returns "conv_1"
        //Act
        val result = repository.preguntar(null, "¿Qué me pongo?", null, emptyList(), fecha = 100)
        //Assert
        assertThat(result.getOrNull()?.conversacionId).isEqualTo("conv_1")
        assertThat(result.getOrNull()?.respuesta?.mensajesRestantes).isEqualTo(9)
        assertThat(pregunta.captured.role).isEqualTo("user")
        assertThat(respuesta.captured.role).isEqualTo("assistant")
        assertThat(respuesta.captured.outfits).hasSize(1)
    }

    @Test
    fun preguntar_limiteAlcanzado_noGuardaNadaEnLaConversacion() = runTest {
        //Arrange
        coEvery { asesorDataSource.enviarMensaje(any()) } throws
            HttpException(Response.error<Any>(429, "{}".toResponseBody()))
        //Act
        val result = repository.preguntar("conv_1", "Otra opción", AccionRapida.OTRA_OPCION, emptyList(), fecha = 1)
        //Assert
        assertThat(result.exceptionOrNull()).isInstanceOf(LimiteAlcanzadoException::class.java)
        coVerify(exactly = 0) { conversacionDataSource.guardarIntercambio(any(), any(), any(), any(), any()) }
    }

    @Test
    fun preguntar_historialLargo_soloEnviaLosUltimosMensajesConTexto() = runTest {
        //Arrange
        val enviado = slot<MensajeAsesorDto>()
        coEvery { asesorDataSource.enviarMensaje(capture(enviado)) } returns RespuestaAsesorDto(reply = "Listo")
        coEvery { conversacionDataSource.guardarIntercambio(any(), any(), any(), any(), any()) } returns "conv_1"
        val historial = (1..15).map { MensajeChatUi(id = "$it", rol = RolMensaje.USUARIO, texto = "m$it") } +
            MensajeChatUi(id = "vacio", rol = RolMensaje.ASESOR, texto = " ")
        //Act
        repository.preguntar("conv_1", "Hazlo más formal", AccionRapida.MAS_FORMAL, historial, fecha = 1)
        //Assert
        assertThat(enviado.captured.history).hasSize(AsesorRepository.MAX_HISTORIAL)
        assertThat(enviado.captured.history.last().text).isEqualTo("m15")
        assertThat(enviado.captured.quickAction).isEqualTo("MORE_FORMAL")
    }

    @Test
    fun preguntar_conPerfilYArmario_enviaSoloElPrimerNombreYHasta80Prendas() = runTest {
        //Arrange
        val enviado = slot<MensajeAsesorDto>()
        coEvery { armarioDataSource.getPrendas("user_1") } returns
            (1..100).map { PrendaFirestoreDto(id = "g$it", name = "Prenda $it", category = "arriba", color = "negro") }
        coEvery { asesorDataSource.enviarMensaje(capture(enviado)) } returns RespuestaAsesorDto(reply = "Listo")
        coEvery { conversacionDataSource.guardarIntercambio(any(), any(), any(), any(), any()) } returns "conv_1"
        //Act
        repository.preguntar(null, "Algo para el lunes", null, emptyList(), fecha = 1)
        //Assert
        assertThat(enviado.captured.profile.name).isEqualTo("Laura")
        assertThat(enviado.captured.profile.primaryStyle).isEqualTo("Urbano")
        assertThat(enviado.captured.wardrobe).hasSize(AsesorRepository.MAX_PRENDAS)
    }

    @Test
    fun preguntar_textoMuyLargo_seRecortaYElTituloTambien() = runTest {
        //Arrange
        val enviado = slot<MensajeAsesorDto>()
        val titulo = slot<String>()
        coEvery { asesorDataSource.enviarMensaje(capture(enviado)) } returns RespuestaAsesorDto(reply = "Listo")
        coEvery { conversacionDataSource.guardarIntercambio(any(), any(), capture(titulo), any(), any()) } returns "conv_1"
        //Act
        repository.preguntar(null, "a".repeat(1500), null, emptyList(), fecha = 1)
        //Assert
        assertThat(enviado.captured.message).hasLength(AsesorRepository.MAX_TEXTO)
        assertThat(titulo.captured).hasLength(AsesorRepository.MAX_TITULO)
    }

    @Test
    fun preguntar_falloElPerfil_elAsesorRespondeIgual() = runTest {
        //Arrange
        coEvery { usuarioDataSource.getUsuario(any()) } throws RuntimeException("Firestore caido")
        coEvery { asesorDataSource.enviarMensaje(any()) } returns RespuestaAsesorDto(reply = "Listo")
        coEvery { conversacionDataSource.guardarIntercambio(any(), any(), any(), any(), any()) } returns "conv_1"
        //Act
        val result = repository.preguntar(null, "Hola", null, emptyList(), fecha = 1)
        //Assert
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun preguntar_sinSesion_sinSesionException() = runTest {
        //Arrange
        every { authDataSource.currentUser } returns null
        //Act
        val result = repository.preguntar(null, "Hola", null, emptyList(), fecha = 1)
        //Assert
        assertThat(result.exceptionOrNull()).isInstanceOf(SinSesionException::class.java)
        coVerify(exactly = 0) { asesorDataSource.enviarMensaje(any()) }
    }
}
