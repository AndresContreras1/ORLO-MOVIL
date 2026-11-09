package com.orlo.app.ui.screens.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orlo.app.R
import com.orlo.app.data.injection.IoDispatcher
import com.orlo.app.data.model.MIN_PASSWORD_LENGTH
import com.orlo.app.data.repository.AuthRepository
import com.orlo.app.ui.utils.mensajeDeError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    // Al corregir el campo se limpia el error del intento anterior
    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email, errorMessageRes = null) }
    }

    fun updateContrasena(contrasena: String) {
        _uiState.update { it.copy(contrasena = contrasena, errorMessageRes = null) }
    }

    fun mostrarEsconderContrasena() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }

    // Validar es responsabilidad del ViewModel, no de la vista
    fun loginButtonPressed() {
        val estado = _uiState.value
        if (estado.cargando) return

        val errorRes = when {
            estado.email.isBlank() || estado.contrasena.isBlank() -> R.string.error_campos_obligatorios
            !estado.email.contains("@") -> R.string.error_email_invalido
            estado.contrasena.length < MIN_PASSWORD_LENGTH -> R.string.error_contrasena_corta
            else -> null
        }

        // Si el formulario no pasa las validaciones locales no se consulta a Firebase
        if (errorRes != null) {
            _uiState.update { it.copy(errorMessageRes = errorRes) }
            return
        }

        _uiState.update { it.copy(cargando = true, errorMessageRes = null) }
        viewModelScope.launch(ioDispatcher) {
            val result = authRepository.signIn(email = estado.email, password = estado.contrasena)

            if (result.isSuccess) {
                _uiState.update { it.copy(cargando = false, navigate = true) }
            } else {
                Log.d("LoginViewModel", result.exceptionOrNull()?.message.toString())
                _uiState.update {
                    it.copy(
                        cargando = false,
                        errorMessageRes = mensajeDeError(result.exceptionOrNull(), R.string.error_inicio_sesion),
                    )
                }
            }
        }
    }
}
