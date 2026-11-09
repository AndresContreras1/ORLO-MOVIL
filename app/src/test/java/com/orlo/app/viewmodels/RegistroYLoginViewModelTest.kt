package com.orlo.app.viewmodels

import com.google.common.truth.Truth.assertThat
import com.orlo.app.R
import com.orlo.app.data.repository.AuthRepository
import com.orlo.app.data.repository.CorreoYaRegistradoException
import com.orlo.app.data.repository.UsuarioRepository
import com.orlo.app.ui.screens.login.LoginViewModel
import com.orlo.app.ui.screens.signup.SignUpViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegistroYLoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var usuarioRepository: UsuarioRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk()
        usuarioRepository = mockk()
        every { authRepository.haySesion } returns false
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun signUpLleno(): SignUpViewModel = SignUpViewModel(authRepository, usuarioRepository, testDispatcher).apply {
        updateNombre("Laura")
        updateEmail("laura@correo.com")
        updateContrasena("123456")
        updateConfirmacion("123456")
    }

    @Test
    fun login_correoSinArroba_errorSinLlamarAFirebase() = runTest {
        //Arrange
        val viewModel = LoginViewModel(authRepository, testDispatcher)
        viewModel.updateEmail("laura.correo.com")
        viewModel.updateContrasena("123456")
        //Act
        viewModel.loginButtonPressed()
        advanceUntilIdle()
        //Assert
        assertThat(viewModel.uiState.value.errorMessageRes).isEqualTo(R.string.error_email_invalido)
        coVerify(exactly = 0) { authRepository.signIn(any(), any()) }
    }

    @Test
    fun login_credencialesValidas_navega() = runTest {
        //Arrange
        coEvery { authRepository.signIn("laura@correo.com", "123456") } returns Result.success(Unit)
        val viewModel = LoginViewModel(authRepository, testDispatcher)
        viewModel.updateEmail("laura@correo.com")
        viewModel.updateContrasena("123456")
        //Act
        viewModel.loginButtonPressed()
        advanceUntilIdle()
        //Assert
        assertThat(viewModel.uiState.value.navigate).isTrue()
        assertThat(viewModel.uiState.value.cargando).isFalse()
    }

    @Test
    fun registrar_sinAceptarTerminos_errorYNoCreaLaCuenta() = runTest {
        //Arrange
        val viewModel = signUpLleno()
        //Act
        viewModel.registrarButtonPressed()
        advanceUntilIdle()
        //Assert
        assertThat(viewModel.uiState.value.errorMessageRes).isEqualTo(R.string.error_aceptar_terminos)
        coVerify(exactly = 0) { authRepository.signUp(any(), any()) }
    }

    @Test
    fun registrar_contrasenasDistintas_error() = runTest {
        //Arrange
        val viewModel = signUpLleno()
        viewModel.updateConfirmacion("654321")
        viewModel.updateAceptaTerminos(true)
        //Act
        viewModel.registrarButtonPressed()
        //Assert
        assertThat(viewModel.uiState.value.errorMessageRes).isEqualTo(R.string.error_contrasenas_distintas)
    }

    @Test
    fun registrar_correoYaRegistrado_mensajeDelCorreo() = runTest {
        //Arrange
        coEvery { authRepository.signUp(any(), any()) } returns Result.failure(CorreoYaRegistradoException())
        val viewModel = signUpLleno()
        viewModel.updateAceptaTerminos(true)
        //Act
        viewModel.registrarButtonPressed()
        advanceUntilIdle()
        //Assert
        assertThat(viewModel.uiState.value.errorMessageRes).isEqualTo(R.string.error_correo_ya_registrado)
        assertThat(viewModel.uiState.value.navigate).isFalse()
    }

    @Test
    fun registrar_todoValido_creaElDocumentoYNavega() = runTest {
        //Arrange
        coEvery { authRepository.signUp(any(), any()) } returns Result.success(Unit)
        coEvery { usuarioRepository.registrarUsuario("Laura", any()) } returns Result.success(Unit)
        val viewModel = signUpLleno()
        viewModel.updateAceptaTerminos(true)
        //Act
        viewModel.registrarButtonPressed()
        advanceUntilIdle()
        //Assert
        assertThat(viewModel.uiState.value.navigate).isTrue()
        coVerify { usuarioRepository.registrarUsuario("Laura", any()) }
    }
}
