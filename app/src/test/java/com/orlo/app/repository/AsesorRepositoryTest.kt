package com.orlo.app.repository

import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseUser
import com.orlo.app.data.datasource.AsesorRemoteDataSource
import com.orlo.app.data.datasource.AuthRemoteDataSource
import com.orlo.app.data.datasource.ConversacionRemoteDataSource
import com.orlo.app.data.dtos.MensajeAsesorDto
import com.orlo.app.data.dtos.OutfitAsesorDto
import com.orlo.app.data.dtos.RespuestaAsesorDto
import com.orlo.app.data.model.AccionRapida
import com.orlo.app.data.model.MensajeChatUi
import com.orlo.app.data.model.RolMensaje
import com.orlo.app.data.repository.AsesorRepository
import com.orlo.app.data.repository.ContextoAsesor
import com.orlo.app.data.repository.LimiteAlcanzadoException
import com.orlo.app.data.repository.SinSesionException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class AsesorRepositoryTest {

    private lateinit var asesorDataSource: AsesorRemoteDataSource
    private lateinit var conversacionDataSource: ConversacionRemoteDataSource
    private lateinit var authDataSource: AuthRemoteDataSource
    private lateinit var repository: AsesorRepository

    private val contexto = ContextoAsesor(nombre = "Laura", estiloPrincipal = "Urbano")

    @Before
    fun setUp() {
        asesorDataSource = mockk()
        conversacionDataSource = mockk()
        authDataSource = mockk()
        val usuario = mockk<FirebaseUser>()
        every { usuario.uid } returns "user_1"
        every { authDataSource.currentUser } returns usuario
        repository = AsesorRepository(asesorDataSource, conversacionDataSource, authDataSource)
    }

    @Test
    fun preguntar_conversacionNueva_creaLaConversacionYGuardaLosDosMensajes() = runTest {
        //Arrange
        coEvery { asesorDataSource.enviarMensaje(any()) } returns RespuestaAsesorDto(
            reply = "Dos ideas",
            outfits = listOf(OutfitAsesorDto(title = "Fresco", items = listOf("Jean", "Camiseta"), why = "Cómodo")),
            remainingMessages = 9,
        )
        coEvery { conversacionDataSource.crearConversacion("user_1", any(), any()) } returns "conv_1"
        coEvery { conversacionDataSource.agregarMensaje(any(), any(), any()) } just runs
        //Act
        val result = repository.preguntar(null, "¿Qué me pongo?", null, contexto, emptyList(), fecha = 100)
        //Assert
        assertThat(result.getOrNull()?.conversacionId).isEqualTo("conv_1")
        assertThat(result.getOrNull()?.respuesta?.mensajesRestantes).isEqualTo(9)
        coVerify(exactly = 2) { conversacionDataSource.agregarMensaje("user_1", "conv_1", any()) }
    }

    @Test
    fun preguntar_limiteAlcanzado_noGuardaNadaEnLaConversacion() = runTest {
        //Arrange
        coEvery { asesorDataSource.enviarMensaje(any()) } throws
            HttpException(Response.error<Any>(429, "{}".toResponseBody()))
        //Act
        val result = repository.preguntar("conv_1", "Otra opción", AccionRapida.OTRA_OPCION, contexto, emptyList(), fecha = 1)
        //Assert
        assertThat(result.exceptionOrNull()).isInstanceOf(LimiteAlcanzadoException::class.java)
        coVerify(exactly = 0) { conversacionDataSource.agregarMensaje(any(), any(), any()) }
        coVerify(exactly = 0) { conversacionDataSource.crearConversacion(any(), any(), any()) }
    }

    @Test
    fun preguntar_historialLargo_soloEnviaLosUltimosMensajesYLaAccion() = runTest {
        //Arrange
        val enviado = slot<MensajeAsesorDto>()
        coEvery { asesorDataSource.enviarMensaje(capture(enviado)) } returns RespuestaAsesorDto(reply = "Listo")
        coEvery { conversacionDataSource.agregarMensaje(any(), any(), any()) } just runs
        val historial = (1..15).map { MensajeChatUi(id = "$it", rol = RolMensaje.USUARIO, texto = "m$it") }
        //Act
        repository.preguntar("conv_1", "Hazlo más formal", AccionRapida.MAS_FORMAL, contexto, historial, fecha = 1)
        //Assert
        assertThat(enviado.captured.history).hasSize(AsesorRepository.MAX_HISTORIAL)
        assertThat(enviado.captured.history.last().text).isEqualTo("m15")
        assertThat(enviado.captured.quickAction).isEqualTo("MORE_FORMAL")
        assertThat(enviado.captured.profile.primaryStyle).isEqualTo("Urbano")
    }

    @Test
    fun preguntar_sinSesion_sinSesionException() = runTest {
        //Arrange
        every { authDataSource.currentUser } returns null
        //Act
        val result = repository.preguntar(null, "Hola", null, contexto, emptyList(), fecha = 1)
        //Assert
        assertThat(result.exceptionOrNull()).isInstanceOf(SinSesionException::class.java)
    }
}
