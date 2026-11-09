package com.orlo.app.viewmodels

import com.google.common.truth.Truth.assertThat
import com.orlo.app.data.model.GuardadoUi
import com.orlo.app.data.model.ItemExploraUi
import com.orlo.app.data.model.SeccionExplora
import com.orlo.app.data.repository.ExploraRepository
import com.orlo.app.ui.screens.explore.ExploreViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: ExploraRepository
    private lateinit var viewModel: ExploreViewModel

    private val blazer = ItemExploraUi(id = "17", nombre = "Blazer")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        every { repository.observarGuardados() } returns flowOf(Result.success(listOf(GuardadoUi(itemId = "17"))))
        coEvery { repository.getSeccion(any()) } returns Result.success(listOf(blazer))
        coEvery { repository.quitar(any()) } returns Result.success(Unit)
        viewModel = ExploreViewModel(repository, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun cambiarSeccion_volverAUnaYaCargada_noLaPideDeNuevo() = runTest {
        //Arrange
        viewModel.cargar()
        advanceUntilIdle()
        //Act
        viewModel.cambiarSeccion(SeccionExplora.TENDENCIAS)
        advanceUntilIdle()
        viewModel.cambiarSeccion(SeccionExplora.PARA_TU_ESTILO)
        advanceUntilIdle()
        //Assert
        coVerify(exactly = 1) { repository.getSeccion(SeccionExplora.PARA_TU_ESTILO) }
        coVerify(exactly = 1) { repository.getSeccion(SeccionExplora.TENDENCIAS) }
    }

    @Test
    fun alternarGuardado_prendaYaGuardada_laQuita() = runTest {
        //Arrange
        viewModel.cargar()
        advanceUntilIdle()
        //Act
        viewModel.alternarGuardado(blazer)
        advanceUntilIdle()
        //Assert
        coVerify { repository.quitar("17") }
        coVerify(exactly = 0) { repository.guardar(any(), any()) }
    }

    @Test
    fun cambiarSeccion_guardados_noLlamaAlBackend() = runTest {
        //Act
        viewModel.cambiarSeccion(SeccionExplora.GUARDADOS)
        advanceUntilIdle()
        //Assert
        coVerify(exactly = 0) { repository.getSeccion(any()) }
        assertThat(viewModel.uiState.value.cargando).isFalse()
    }
}
