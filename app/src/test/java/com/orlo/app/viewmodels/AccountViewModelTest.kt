package com.orlo.app.viewmodels

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import com.orlo.app.R
import com.orlo.app.data.model.CuentaUi
import com.orlo.app.data.model.EstiloUi
import com.orlo.app.data.model.PerfilUi
import com.orlo.app.data.model.ResultadoUi
import com.orlo.app.data.repository.AuthRepository
import com.orlo.app.data.repository.CuentaRepository
import com.orlo.app.data.repository.ErrorAlSubirImagenException
import com.orlo.app.data.repository.UsuarioRepository
import com.orlo.app.ui.screens.account.AccountViewModel
import io.mockk.coEvery
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

// Mi cuenta con los repositorios moqueados: el resumen del perfil y la foto
@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var usuarioRepository: UsuarioRepository
    private lateinit var viewModel: AccountViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val authRepository = mockk<AuthRepository>(relaxed = true)
        every { authRepository.correo } returns "laura@correo.co"
        val cuentaRepository = mockk<CuentaRepository>()
        coEvery { cuentaRepository.getCuenta() } returns Result.success(CuentaUi(premium = true))
        usuarioRepository = mockk()
        every { usuarioRepository.observarPerfil() } returns flowOf(
            Result.success(
                PerfilUi(
                    nombre = "Laura",
                    cantidadPrendas = 12,
                    resultadoActual = ResultadoUi(principal = EstiloUi(nombre = "Elegante"), secundario = EstiloUi(nombre = "Clásico")),
                )
            )
        )
        every { usuarioRepository.observarLeccionesCompletadas() } returns flowOf(Result.success(3))
        viewModel = AccountViewModel(authRepository, usuarioRepository, cuentaRepository, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun cargar_perfilYAvance_armaElResumen() = runTest {
        //Act
        viewModel.cargar()
        advanceUntilIdle()
        //Assert
        val state = viewModel.uiState.value
        assertThat(state.estilo).isEqualTo("Elegante · clásico")
        assertThat(state.cantidadPrendas).isEqualTo(12)
        assertThat(state.leccionesCompletadas).isEqualTo(3)
        assertThat(state.correo).isEqualTo("laura@correo.co")
    }

    @Test
    fun cambiarFoto_noSePuedeLeer_muestraElErrorYTerminaDeSubir() = runTest {
        //Arrange
        val foto = mockk<Uri>()
        coEvery { usuarioRepository.cambiarFoto(foto, any()) } returns Result.failure(ErrorAlSubirImagenException())
        //Act
        viewModel.cambiarFoto(foto)
        advanceUntilIdle()
        //Assert
        assertThat(viewModel.uiState.value.subiendoFoto).isFalse()
        assertThat(viewModel.uiState.value.errorMessageRes).isNotNull()
        assertThat(viewModel.uiState.value.avisoRes).isNull()
    }

    @Test
    fun cambiarFoto_subida_avisaQueQuedo() = runTest {
        //Arrange
        val foto = mockk<Uri>()
        coEvery { usuarioRepository.cambiarFoto(foto, any()) } returns Result.success("http://127.0.0.1:9199/foto.jpg")
        //Act
        viewModel.cambiarFoto(foto)
        advanceUntilIdle()
        //Assert
        assertThat(viewModel.uiState.value.avisoRes).isEqualTo(R.string.foto_actualizada)
    }
}
