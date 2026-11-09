package com.orlo.app.repository

import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseUser
import com.orlo.app.data.datasource.ArmarioRemoteDataSource
import com.orlo.app.data.datasource.AuthRemoteDataSource
import com.orlo.app.data.datasource.ExploraRemoteDataSource
import com.orlo.app.data.datasource.GuardadoRemoteDataSource
import com.orlo.app.data.datasource.UsuarioRemoteDataSource
import com.orlo.app.data.dtos.CompletarArmarioDto
import com.orlo.app.data.dtos.EstiloFirestoreDto
import com.orlo.app.data.dtos.GuardadoFirestoreDto
import com.orlo.app.data.dtos.ItemExploraDto
import com.orlo.app.data.dtos.PrendaFirestoreDto
import com.orlo.app.data.dtos.ResultadoFirestoreDto
import com.orlo.app.data.dtos.UsuarioFirestoreDto
import com.orlo.app.data.model.CategoriaPrenda
import com.orlo.app.data.model.ItemExploraUi
import com.orlo.app.data.model.SeccionExplora
import com.orlo.app.data.repository.DatosInvalidosException
import com.orlo.app.data.repository.ExploraRepository
import com.orlo.app.data.repository.SinConexionException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException

// Explora con todos los data sources moqueados: que se pide al backend y que se guarda en Firestore
class ExploraRepositoryTest {

    private lateinit var explora: ExploraRemoteDataSource
    private lateinit var guardados: GuardadoRemoteDataSource
    private lateinit var usuario: UsuarioRemoteDataSource
    private lateinit var armario: ArmarioRemoteDataSource
    private lateinit var auth: AuthRemoteDataSource
    private lateinit var repository: ExploraRepository

    @Before
    fun setUp() {
        explora = mockk()
        guardados = mockk()
        usuario = mockk()
        armario = mockk()
        auth = mockk()
        val sesion = mockk<FirebaseUser>()
        every { sesion.uid } returns "user_1"
        every { auth.currentUser } returns sesion
        coEvery { usuario.getUsuario("user_1") } returns UsuarioFirestoreDto(
            name = "Laura",
            currentResult = ResultadoFirestoreDto(primary = EstiloFirestoreDto(id = 3, code = "elegante", name = "Elegante")),
        )
        repository = ExploraRepository(explora, guardados, usuario, armario, auth)
    }

    @Test
    fun getSeccion_paraTuEstilo_pideElCatalogoConElEstiloPrincipal() = runTest {
        //Arrange
        coEvery { explora.getItems("FOR_YOUR_STYLE", "elegante") } returns listOf(ItemExploraDto(id = "17", name = "Blazer", category = "abrigo"))
        //Act
        val result = repository.getSeccion(SeccionExplora.PARA_TU_ESTILO)
        //Assert
        assertThat(result.getOrThrow().single().categoria).isEqualTo(CategoriaPrenda.ABRIGO)
    }

    @Test
    fun getSeccion_tendencias_noMandaEstiloNiLeeElPerfil() = runTest {
        //Arrange
        coEvery { explora.getItems("TRENDS", null) } returns emptyList()
        //Act
        repository.getSeccion(SeccionExplora.TENDENCIAS)
        //Assert
        coVerify(exactly = 0) { usuario.getUsuario(any()) }
    }

    @Test
    fun getSeccion_guardados_noEsDelCatalogo() = runTest {
        //Act
        val result = repository.getSeccion(SeccionExplora.GUARDADOS)
        //Assert
        assertThat(result.exceptionOrNull()).isInstanceOf(DatosInvalidosException::class.java)
        coVerify(exactly = 0) { explora.getItems(any(), any()) }
    }

    @Test
    fun completarArmario_sinRed_sinConexion() = runTest {
        //Arrange
        coEvery { armario.getPrendas("user_1") } returns listOf(PrendaFirestoreDto(category = "arriba", color = "negro"))
        coEvery { explora.completarArmario(any()) } throws IOException("timeout")
        //Act
        val result = repository.completarArmario()
        //Assert
        assertThat(result.exceptionOrNull()).isInstanceOf(SinConexionException::class.java)
    }

    @Test
    fun completarArmario_conPrendas_soloMandaCategoriaYColor() = runTest {
        //Arrange
        val pedido = slot<CompletarArmarioDto>()
        coEvery { armario.getPrendas("user_1") } returns listOf(PrendaFirestoreDto(name = "Mi camisa favorita", category = "arriba", color = "negro"))
        coEvery { explora.completarArmario(capture(pedido)) } returns emptyList()
        //Act
        repository.completarArmario()
        //Assert
        assertThat(pedido.captured.primaryStyle).isEqualTo("elegante")
        assertThat(pedido.captured.wardrobe.single().category).isEqualTo("arriba")
        assertThat(pedido.captured.wardrobe.single().color).isEqualTo("negro")
    }

    @Test
    fun guardar_idNumerico_seGuardaConElIdComoNumero() = runTest {
        //Arrange
        val dto = slot<GuardadoFirestoreDto>()
        coEvery { guardados.guardar("user_1", "17", capture(dto)) } just runs
        //Act
        val result = repository.guardar(ItemExploraUi(id = "17", nombre = "Blazer", categoria = CategoriaPrenda.ABRIGO), fecha = 5)
        //Assert
        assertThat(result.isSuccess).isTrue()
        assertThat(dto.captured.itemId).isEqualTo(17L)
        assertThat(dto.captured.category).isEqualTo("abrigo")
    }

    @Test
    fun guardar_idQueNoEsNumero_datosInvalidosYNoEscribe() = runTest {
        //Act
        val result = repository.guardar(ItemExploraUi(id = "abc"), fecha = 5)
        //Assert
        assertThat(result.exceptionOrNull()).isInstanceOf(DatosInvalidosException::class.java)
        coVerify(exactly = 0) { guardados.guardar(any(), any(), any()) }
    }

    @Test
    fun observarGuardados_sinSesion_unSoloFallo() = runTest {
        //Arrange
        every { auth.currentUser } returns null
        every { guardados.observarGuardados(any()) } returns flowOf(emptyList())
        //Act
        val primero = repository.observarGuardados().first()
        //Assert
        assertThat(primero.isFailure).isTrue()
    }
}
