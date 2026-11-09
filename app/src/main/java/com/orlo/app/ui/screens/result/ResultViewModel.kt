package com.orlo.app.ui.screens.result

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orlo.app.R
import com.orlo.app.data.injection.IoDispatcher
import com.orlo.app.data.model.ResultadoUi
import com.orlo.app.data.repository.ContenidoNoEncontradoException
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

data class ResultState(
    val resultado: ResultadoUi? = null,
    val premium: Boolean = false,
    val isLoading: Boolean = true,
    @StringRes val errorMessageRes: Int? = null,
)

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val cuentaRepository: CuentaRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultState())
    val uiState: StateFlow<ResultState> = _uiState.asStateFlow()

    init {
        cargarResultado()
    }

    // El resultado y la cuenta son independientes: se piden al tiempo
    fun cargarResultado() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isLoading = true, errorMessageRes = null) }

            val (perfil, cuenta) = coroutineScope {
                val perfil = async { usuarioRepository.getPerfil() }
                val cuenta = async { cuentaRepository.getCuenta() }
                perfil.await() to cuenta.await()
            }

            val resultado = perfil.getOrNull()?.resultadoActual
            if (resultado != null) {
                // Si no se pudo leer la cuenta se sigue como gratis: el backend decide al final
                _uiState.update {
                    it.copy(isLoading = false, resultado = resultado, premium = cuenta.getOrNull()?.premium ?: false)
                }
            } else {
                val error = perfil.exceptionOrNull() ?: ContenidoNoEncontradoException()
                Log.d("ResultViewModel", error.message.toString())
                _uiState.update {
                    it.copy(isLoading = false, errorMessageRes = mensajeDeError(error, R.string.error_cargar_resultado))
                }
            }
        }
    }
}
