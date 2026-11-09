package com.orlo.app.ui.screens.splash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orlo.app.R
import com.orlo.app.data.injection.IoDispatcher
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

// A donde va la app al abrirse, segun en que punto del recorrido quedo el usuario
enum class DestinoInicial { BIENVENIDA, PERFIL, CUESTIONARIO, INICIO }

data class SplashState(
    val destino: DestinoInicial? = null,
    val errorMessageRes: Int? = null,
)

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

        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(errorMessageRes = null) }
            val result = usuarioRepository.getPerfil()

            if (result.isSuccess) {
                val perfil = result.getOrNull()
                val destino = when {
                    perfil == null || perfil.nombre.isBlank() -> DestinoInicial.PERFIL
                    perfil.resultadoActual == null -> DestinoInicial.CUESTIONARIO
                    else -> DestinoInicial.INICIO
                }
                _uiState.update { it.copy(destino = destino) }
            } else {
                Log.d("SplashViewModel", result.exceptionOrNull()?.message.toString())
                _uiState.update {
                    it.copy(errorMessageRes = mensajeDeError(result.exceptionOrNull(), R.string.error_cargar_perfil))
                }
            }
        }
    }
}
