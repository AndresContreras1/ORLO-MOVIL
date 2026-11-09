package com.orlo.app.ui.screens.lessondetail

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orlo.app.R
import com.orlo.app.data.injection.IoDispatcher
import com.orlo.app.data.model.EstadoLeccion
import com.orlo.app.data.model.LeccionUi
import com.orlo.app.data.model.PerfilUi
import com.orlo.app.data.repository.LeccionRepository
import com.orlo.app.data.repository.PremiumRequeridoException
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

data class LessonDetailState(
    val leccion: LeccionUi? = null,
    val estado: EstadoLeccion = EstadoLeccion.SIN_EMPEZAR,
    // Para la capsula de colorimetria: los colores del perfil, si el usuario los lleno
    val perfil: PerfilUi? = null,
    // La opcion que eligio en la pregunta de repaso
    val respuestaRepaso: Int? = null,
    val requierePremium: Boolean = false,
    val isLoading: Boolean = true,
    @StringRes val errorMessageRes: Int? = null,
    val guardando: Boolean = false,
)

@HiltViewModel
class LessonDetailViewModel @Inject constructor(
    private val leccionRepository: LeccionRepository,
    private val usuarioRepository: UsuarioRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(LessonDetailState())
    val uiState: StateFlow<LessonDetailState> = _uiState.asStateFlow()

    // El id llega por la ruta: por eso se carga con LaunchedEffect y no en init
    fun cargar(leccionId: Long) {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isLoading = true, errorMessageRes = null, requierePremium = false) }

            val result = leccionRepository.getLeccion(leccionId)
            if (result.isFailure) {
                val error = result.exceptionOrNull()
                Log.d("LessonDetailViewModel", error?.message.toString())
                // Una leccion premium sin serlo no es un error: se muestra que incluye premium
                if (error is PremiumRequeridoException) {
                    _uiState.update { it.copy(isLoading = false, requierePremium = true) }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessageRes = mensajeDeError(error, R.string.error_cargar_leccion))
                    }
                }
                return@launch
            }

            val leccion = result.getOrThrow()
            val estado = leccionRepository.getEstado(leccionId).getOrNull() ?: EstadoLeccion.SIN_EMPEZAR
            val perfil = if (leccion.usaColorimetria) usuarioRepository.getPerfil().getOrNull() else null

            // Abrirla la deja en curso, sin bajar una que ya estaba completada
            val estadoFinal = if (estado == EstadoLeccion.SIN_EMPEZAR) {
                leccionRepository.guardarEstado(leccionId, EstadoLeccion.EN_CURSO, System.currentTimeMillis())
                EstadoLeccion.EN_CURSO
            } else {
                estado
            }

            _uiState.update {
                it.copy(isLoading = false, leccion = leccion, estado = estadoFinal, perfil = perfil)
            }
        }
    }

    fun responderRepaso(indice: Int) {
        _uiState.update { it.copy(respuestaRepaso = indice) }
    }

    fun marcarCompletada() {
        val leccion = _uiState.value.leccion ?: return
        if (_uiState.value.guardando) return

        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(guardando = true, errorMessageRes = null) }
            val result = leccionRepository.guardarEstado(leccion.id, EstadoLeccion.COMPLETADA, System.currentTimeMillis())
            if (result.isSuccess) {
                _uiState.update { it.copy(guardando = false, estado = EstadoLeccion.COMPLETADA) }
            } else {
                Log.d("LessonDetailViewModel", result.exceptionOrNull()?.message.toString())
                _uiState.update {
                    it.copy(guardando = false, errorMessageRes = mensajeDeError(result.exceptionOrNull(), R.string.error_guardar_avance))
                }
            }
        }
    }
}
