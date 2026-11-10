package com.orlo.app.ui.screens.quiz

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orlo.app.R
import com.orlo.app.data.injection.IoDispatcher
import com.orlo.app.data.repository.CuestionarioCambioException
import com.orlo.app.data.repository.CuestionarioRepository
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

// El cuestionario: una pregunta por pantalla, se puede volver atras y no hay respuestas correctas.
// Al final el backend calcula el estilo y el resultado queda guardado en el perfil
@HiltViewModel
class QuizViewModel @Inject constructor(
    private val cuestionarioRepository: CuestionarioRepository,
    private val usuarioRepository: UsuarioRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizState())
    val uiState: StateFlow<QuizState> = _uiState.asStateFlow()

    init {
        cargarCuestionario()
    }

    fun cargarCuestionario() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isLoading = true, errorCargaRes = null) }
            val result = cuestionarioRepository.getCuestionario()
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(isLoading = false, cuestionario = result.getOrThrow(), indice = 0, respuestas = emptyMap())
                }
            } else {
                Log.d("QuizViewModel", result.exceptionOrNull()?.message.toString())
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorCargaRes = mensajeDeError(result.exceptionOrNull(), R.string.error_cargar_cuestionario),
                    )
                }
            }
        }
    }

    // En las preguntas de una opcion, elegir otra reemplaza la anterior; en las de varias, alterna
    fun seleccionarOpcion(opcionId: String) {
        val pregunta = _uiState.value.preguntaActual ?: return
        _uiState.update { estado ->
            val actuales = estado.respuestas[pregunta.id] ?: emptySet()
            val nuevas = when {
                !pregunta.multiple -> setOf(opcionId)
                opcionId in actuales -> actuales - opcionId
                else -> actuales + opcionId
            }
            estado.copy(respuestas = estado.respuestas + (pregunta.id to nuevas), errorMessageRes = null)
        }
    }

    fun siguiente() {
        val estado = _uiState.value
        if (estado.seleccionActual.isEmpty()) {
            _uiState.update { it.copy(errorMessageRes = R.string.error_elige_una_opcion) }
            return
        }
        if (estado.esUltima) {
            enviar()
        } else {
            _uiState.update { it.copy(indice = it.indice + 1, errorMessageRes = null) }
        }
    }

    fun atras() {
        _uiState.update { if (it.indice > 0) it.copy(indice = it.indice - 1, errorMessageRes = null) else it }
    }

    private fun enviar() {
        if (_uiState.value.enviando) return

        _uiState.update { it.copy(enviando = true, errorMessageRes = null) }
        viewModelScope.launch(ioDispatcher) {
            val fecha = System.currentTimeMillis()
            val calculo = cuestionarioRepository.puntuar(_uiState.value.respuestas, fecha)
            if (calculo.isFailure) {
                val error = calculo.exceptionOrNull()
                Log.d("QuizViewModel", error?.message.toString())
                _uiState.update {
                    it.copy(enviando = false, errorMessageRes = mensajeDeError(error, R.string.error_calcular_resultado))
                }
                // Los fundadores editaron el cuestionario mientras se respondia: se carga el nuevo
                if (error is CuestionarioCambioException) cargarCuestionario()
                return@launch
            }

            // El resultado anterior se conserva en el historial; este pasa a ser el actual
            val guardado = usuarioRepository.guardarResultado(calculo.getOrThrow())
            if (guardado.isSuccess) {
                _uiState.update { it.copy(enviando = false, navigate = true) }
            } else {
                Log.d("QuizViewModel", guardado.exceptionOrNull()?.message.toString())
                _uiState.update {
                    it.copy(enviando = false, errorMessageRes = mensajeDeError(guardado.exceptionOrNull(), R.string.error_guardar_resultado))
                }
            }
        }
    }
}
