package com.orlo.app.ui.screens.splash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orlo.app.R
import com.orlo.app.data.injection.IoDispatcher
import com.orlo.app.data.repository.AuthRepository
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

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val usuarioRepository: UsuarioRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashState())
    val uiState: StateFlow<SplashState> = _uiState.asStateFlow()

    // La primera carga no necesita parametros: va en init
    init {
        decidirDestino()
    }

    // Sin sesion: bienvenida. Con sesion: el perfil, el cuestionario o el inicio, lo que falte
    fun decidirDestino() {
        if (!authRepository.haySesion) {
            _uiState.update { it.copy(destino = DestinoInicial.BIENVENIDA) }
            return
        }

        _uiState.update { it.copy(errorMessageRes = null) }
        viewModelScope.launch(ioDispatcher) {
            val result = usuarioRepository.getPerfil()
            val error = result.exceptionOrNull()

            val destino = when {
                // Se registro pero no alcanzo a guardar el perfil
                error is ContenidoNoEncontradoException -> DestinoInicial.PERFIL
                error != null -> null
                result.getOrThrow().nombre.isBlank() -> DestinoInicial.PERFIL
                result.getOrThrow().resultadoActual == null -> DestinoInicial.CUESTIONARIO
                else -> DestinoInicial.INICIO
            }

            if (destino != null) {
                _uiState.update { it.copy(destino = destino) }
            } else {
                Log.d("SplashViewModel", error?.message.toString())
                _uiState.update { it.copy(errorMessageRes = mensajeDeError(error, R.string.error_cargar_perfil)) }
            }
        }
    }
}
