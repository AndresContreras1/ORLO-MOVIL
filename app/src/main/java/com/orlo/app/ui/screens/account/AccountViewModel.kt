package com.orlo.app.ui.screens.account

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orlo.app.R
import com.orlo.app.data.injection.IoDispatcher
import com.orlo.app.data.model.CuentaUi
import com.orlo.app.data.repository.AuthRepository
import com.orlo.app.data.repository.CuentaRepository
import com.orlo.app.data.repository.UsuarioRepository
import com.orlo.app.ui.utils.mensajeDeError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountState(
    val nombre: String = "",
    val correo: String = "",
    val cuenta: CuentaUi? = null,
    val isLoading: Boolean = true,
    @StringRes val errorMessageRes: Int? = null,
    val confirmarBorrado: Boolean = false,
    val borrando: Boolean = false,
    // Al cerrar sesion o borrar la cuenta se vuelve a la bienvenida
    val salir: Boolean = false,
)

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val usuarioRepository: UsuarioRepository,
    private val cuentaRepository: CuentaRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountState())
    val uiState: StateFlow<AccountState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isLoading = true, errorMessageRes = null, correo = authRepository.correo) }
            coroutineScope {
                val perfil = async { usuarioRepository.getPerfil() }
                val cuenta = async { cuentaRepository.getCuenta() }
                val perfilResult = perfil.await()
                val cuentaResult = cuenta.await()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        nombre = perfilResult.getOrNull()?.nombre ?: "",
                        // Sin la cuenta la pantalla sigue sirviendo: cerrar sesion, editar datos
                        cuenta = cuentaResult.getOrNull(),
                    )
                }
            }
        }
    }

    fun cerrarSesion() {
        authRepository.signOut()
        _uiState.update { it.copy(salir = true) }
    }

    fun pedirBorrado() {
        _uiState.update { it.copy(confirmarBorrado = true) }
    }

    fun cancelarBorrado() {
        _uiState.update { it.copy(confirmarBorrado = false) }
    }

    // Borra todo: datos, fotos y la cuenta. No se puede deshacer, por eso se confirma antes
    fun borrarCuenta() {
        if (_uiState.value.borrando) return
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(confirmarBorrado = false, borrando = true, errorMessageRes = null) }
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
