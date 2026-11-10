package com.orlo.app.viewmodels

import com.google.common.truth.Truth.assertThat
import com.orlo.app.R
import com.orlo.app.data.model.CategoriaPrenda
import com.orlo.app.data.model.CuentaUi
import com.orlo.app.data.model.OutfitUi
import com.orlo.app.data.model.PerfilUi
import com.orlo.app.data.model.PrendaUi
import com.orlo.app.data.repository.ArmarioRepository
import com.orlo.app.data.repository.AsesorRepository
import com.orlo.app.data.repository.CuentaRepository
import com.orlo.app.data.repository.SinConexionException
import com.orlo.app.data.repository.UsuarioRepository
import com.orlo.app.ui.screens.wardrobe.WardrobeViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

// Mi armario con los repositorios moqueados: los flujos en vivo se simulan con un MutableStateFlow
@OptIn(ExperimentalCoroutinesApi::class)
class WardrobeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var armarioRepository: ArmarioRepository
    private lateinit var usuarioRepository: UsuarioRepository
    private lateinit var asesorRepository: AsesorRepository
    private lateinit var cuentaRepository: CuentaRepository
    private lateinit var viewModel: WardrobeViewModel

    private val prendasEnVivo = MutableStateFlow(
        Result.success(
            listOf(
                PrendaUi(id = "p1", nombre = "Jean", categoria = CategoriaPrenda.ABAJO, color = "azul", favorita = true),
                PrendaUi(id = "p2", nombre = "Camisa", categoria = CategoriaPrenda.ARRIBA, color = "blanco"),
            )
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        armarioRepository = mockk(relaxed = true)
        usuarioRepository = mockk()
        asesorRepository = mockk()
        cuentaRepository = mockk()
        coEvery { cuentaRepository.getCuenta() } returns Result.success(CuentaUi(premium = true))
        coEvery { usuarioRepository.getResultados() } returns Result.success(emptyList())
        every { usuarioRepository.observarPerfil() } returns flowOf(Result.success(PerfilUi(nombre = "Laura")))
        every { asesorRepository.observarConversaciones() } returns flowOf(Result.success(emptyList()))
        every { armarioRepository.observarPrendas() } returns prendasEnVivo
        every { armarioRepository.observarOutfits() } returns flowOf(Result.success(listOf(OutfitUi(id = "o1", nombre = "Lunes", fecha = 5))))
        viewModel = WardrobeViewModel(armarioRepository, usuarioRepository, asesorRepository, cuentaRepository, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun cargar_conDatos_cuentaLasFavoritasYArmaElHistorial() = runTest {
        //Act
        viewModel.cargar()
        advanceUntilIdle()
        //Assert
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.prendasFavoritas).isEqualTo(1)
        assertThat(state.historial).hasSize(1)
    }

    @Test
    fun alternarPrendasFavoritas_conFiltro_soloQuedanLasFavoritas() = runTest {
        //Arrange
        viewModel.cargar()
        advanceUntilIdle()
        //Act
        viewModel.alternarPrendasFavoritas()
        //Assert
        assertThat(viewModel.uiState.value.prendasFiltradas.map { it.id }).containsExactly("p1")
    }

    @Test
    fun cargar_cambioEnFirestore_laListaSeActualizaSinRecargar() = runTest {
        //Arrange
        viewModel.cargar()
        advanceUntilIdle()
        //Act: llega otra lista por el listener
        prendasEnVivo.value = Result.success(listOf(PrendaUi(id = "p3", nombre = "Tenis", favorita = true)))
        advanceUntilIdle()
        //Assert
        assertThat(viewModel.uiState.value.prendas.map { it.id }).containsExactly("p3")
        coVerify(exactly = 1) { cuentaRepository.getCuenta() }
    }

    @Test
    fun cargar_sinConexionEnLaCuenta_errorDeCarga() = runTest {
        //Arrange
        coEvery { cuentaRepository.getCuenta() } returns Result.failure(SinConexionException())
        //Act
        viewModel.cargar()
        advanceUntilIdle()
        //Assert
        assertThat(viewModel.uiState.value.errorCargaRes).isEqualTo(R.string.error_sin_conexion)
    }
}
