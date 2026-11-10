package com.orlo.app.ui.screens.result

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orlo.app.R
import com.orlo.app.data.injection.IoDispatcher
import com.orlo.app.data.repository.ContenidoNoEncontradoException
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

// El resultado quedo guardado en el perfil al terminar el cuestionario: aqui solo se lee
@HiltViewModel
class ResultViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultState())
    val uiState: StateFlow<ResultState> = _uiState.asStateFlow()

    init {
        cargarResultado()
    }

    fun cargarResultado() {
        _uiState.update { it.copy(isLoading = true, errorMessageRes = null) }
        viewModelScope.launch(ioDispatcher) {
            val perfil = usuarioRepository.getPerfil()
            val resultado = perfil.getOrNull()?.resultadoActual
            if (resultado != null) {
                _uiState.update { it.copy(isLoading = false, resultado = resultado) }
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
