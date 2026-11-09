package com.orlo.app.ui.screens.signup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orlo.app.R
import com.orlo.app.data.injection.IoDispatcher
import com.orlo.app.data.model.MAX_NOMBRE_LENGTH
import com.orlo.app.data.model.MIN_PASSWORD_LENGTH
import com.orlo.app.data.repository.AuthRepository
import com.orlo.app.data.repository.UsuarioRepository
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
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val usuarioRepository: UsuarioRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpState())
    val uiState: StateFlow<SignUpState> = _uiState.asStateFlow()

    fun updateNombre(nombre: String) {
        _uiState.update { it.copy(nombre = nombre.take(MAX_NOMBRE_LENGTH), errorMessageRes = null) }
    }

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email, errorMessageRes = null) }
    }

    fun updateContrasena(contrasena: String) {
        _uiState.update { it.copy(contrasena = contrasena, errorMessageRes = null) }
    }

    fun updateConfirmacion(confirmacion: String) {
        _uiState.update { it.copy(confirmacion = confirmacion, errorMessageRes = null) }
    }

    fun mostrarEsconderContrasena() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }

    fun updateAceptaTerminos(acepta: Boolean) {
        _uiState.update { it.copy(aceptaTerminos = acepta, errorMessageRes = null) }
    }

    fun registrarButtonPressed() {
        val estado = _uiState.value
        if (estado.cargando) return

        val errorRes = when {
            estado.nombre.isBlank() || estado.email.isBlank() || estado.contrasena.isBlank() ->
                R.string.error_campos_obligatorios
            !estado.email.contains("@") -> R.string.error_email_invalido
            estado.contrasena.length < MIN_PASSWORD_LENGTH -> R.string.error_contrasena_corta
            estado.contrasena != estado.confirmacion -> R.string.error_contrasenas_distintas
            !estado.aceptaTerminos -> R.string.error_aceptar_terminos
            else -> null
        }

        if (errorRes != null) {
            _uiState.update { it.copy(errorMessageRes = errorRes) }
            return
        }

        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(cargando = true, errorMessageRes = null) }

            // Si un intento anterior ya creo la cuenta pero fallo el documento, no se vuelve a crear
            val cuenta = if (authRepository.haySesion) {
                Result.success(Unit)
            } else {
                authRepository.signUp(email = estado.email, password = estado.contrasena)
            }

            if (cuenta.isFailure) {
                Log.d("SignUpViewModel", cuenta.exceptionOrNull()?.message.toString())
                _uiState.update {
                    it.copy(cargando = false, errorMessageRes = mensajeDeError(cuenta.exceptionOrNull(), R.string.error_registro))
                }
                return@launch
            }

            // Autenticar primero y despues crear el documento con el uid como id
            val documento = usuarioRepository.registrarUsuario(
                nombre = estado.nombre,
                fecha = System.currentTimeMillis(),
            )

            if (documento.isSuccess) {
                _uiState.update { it.copy(cargando = false, navigate = true) }
            } else {
                Log.d("SignUpViewModel", documento.exceptionOrNull()?.message.toString())
                _uiState.update {
                    it.copy(cargando = false, errorMessageRes = mensajeDeError(documento.exceptionOrNull(), R.string.error_registro))
                }
            }
        }
    }
}
