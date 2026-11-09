package com.orlo.app.ui.screens.account

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orlo.app.R
import com.orlo.app.data.injection.IoDispatcher
import com.orlo.app.data.repository.AuthRepository
import com.orlo.app.data.repository.CuentaRepository
import com.orlo.app.data.repository.UsuarioRepository
import com.orlo.app.ui.utils.mensajeDeError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Mi cuenta: el perfil llega en vivo (la foto nueva y el contador de prendas aparecen solos) y la
// suscripcion se pide al backend cada vez que se entra
@HiltViewModel
class AccountViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val usuarioRepository: UsuarioRepository,
    private val cuentaRepository: CuentaRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountState())
    val uiState: StateFlow<AccountState> = _uiState.asStateFlow()

    private var observacion: Job? = null
    private var avance: Job? = null

    fun cargar() {
        viewModelScope.launch(ioDispatcher) {
            val cuenta = cuentaRepository.getCuenta()
            _uiState.update { it.copy(cuenta = cuenta.getOrNull() ?: it.cuenta) }
        }
        observarAvance()
        if (observacion?.isActive == true && _uiState.value.errorCargaRes == null) return
        observacion?.cancel()
        _uiState.update { it.copy(isLoading = it.nombre.isBlank(), errorCargaRes = null, correo = authRepository.correo) }
        observacion = viewModelScope.launch(ioDispatcher) {
            usuarioRepository.observarPerfil().collect { result ->
                val perfil = result.getOrNull()
                if (perfil != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            nombre = perfil.nombre,
                            fotoUrl = perfil.fotoUrl,
                            cantidadPrendas = perfil.cantidadPrendas,
                            estilo = perfil.resultadoActual?.nombreCompleto,
                        )
                    }
                } else {
                    Log.d("AccountViewModel", result.exceptionOrNull()?.message.toString())
                    _uiState.update {
                        it.copy(isLoading = false, errorCargaRes = mensajeDeError(result.exceptionOrNull(), R.string.error_cargar_cuenta))
                    }
                }
            }
        }
    }

    // El avance es un dato mas del resumen: si falla, la cuenta se ve igual
    private fun observarAvance() {
        if (avance?.isActive == true) return
        avance = viewModelScope.launch(ioDispatcher) {
            usuarioRepository.observarLeccionesCompletadas().collect { result ->
                result.getOrNull()?.let { completadas -> _uiState.update { it.copy(leccionesCompletadas = completadas) } }
            }
        }
    }

    // La foto nueva aparece sola cuando Firestore avisa que el perfil cambio
    fun cambiarFoto(foto: Uri) {
        if (_uiState.value.subiendoFoto) return
        _uiState.update { it.copy(subiendoFoto = true, errorMessageRes = null) }
        viewModelScope.launch(ioDispatcher) {
            val result = usuarioRepository.cambiarFoto(foto, System.currentTimeMillis())
            if (result.isSuccess) {
                _uiState.update { it.copy(subiendoFoto = false, avisoRes = R.string.foto_actualizada) }
            } else {
                Log.d("AccountViewModel", result.exceptionOrNull()?.message.toString())
                _uiState.update {
                    it.copy(subiendoFoto = false, errorMessageRes = mensajeDeError(result.exceptionOrNull(), R.string.error_cambiar_foto))
                }
            }
        }
    }

    fun avisoMostrado() {
        _uiState.update { it.copy(avisoRes = null) }
    }

    fun cerrarSesion() {
        observacion?.cancel()
        avance?.cancel()
        authRepository.signOut()
        _uiState.update { it.copy(salir = true) }
    }

    fun pedirBorrado() {
        _uiState.update { it.copy(confirmarBorrado = true) }
    }

    fun cancelarBorrado() {
        _uiState.update { it.copy(confirmarBorrado = false) }
    }

    // Borra todo: datos, fotos y la cuenta. No se puede deshacer, por eso se confirma antes. Se deja
    // de escuchar el perfil antes de borrarlo
    fun borrarCuenta() {
        if (_uiState.value.borrando) return
        observacion?.cancel()
        avance?.cancel()
        _uiState.update { it.copy(confirmarBorrado = false, borrando = true, errorMessageRes = null) }
        viewModelScope.launch(ioDispatcher) {
            val result = cuentaRepository.eliminarCuenta()
            if (result.isSuccess) {
                _uiState.update { it.copy(borrando = false, salir = true) }
            } else {
                Log.d("AccountViewModel", result.exceptionOrNull()?.message.toString())
                _uiState.update {
                    it.copy(borrando = false, errorMessageRes = mensajeDeError(result.exceptionOrNull(), R.string.error_borrar_cuenta))
                }
            }
        }
    }
}
