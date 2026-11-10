package com.orlo.app.ui.screens.lessons

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orlo.app.R
import com.orlo.app.data.injection.IoDispatcher
import com.orlo.app.data.model.EstadoLeccion
import com.orlo.app.data.model.LeccionResumenUi
import com.orlo.app.data.repository.CuentaRepository
import com.orlo.app.data.repository.LeccionRepository
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

@HiltViewModel
class LessonsViewModel @Inject constructor(
    private val leccionRepository: LeccionRepository,
    private val cuentaRepository: CuentaRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(LessonsState())
    val uiState: StateFlow<LessonsState> = _uiState.asStateFlow()

    // Se llama al entrar y al volver de una leccion, para mostrar el avance nuevo
    fun cargar() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isLoading = it.lecciones.isEmpty(), errorMessageRes = null) }

            val (lecciones, cuenta) = coroutineScope {
                val lecciones = async { leccionRepository.getLecciones() }
                val cuenta = async { cuentaRepository.getCuenta() }
                lecciones.await() to cuenta.await()
            }

            if (lecciones.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        lecciones = lecciones.getOrThrow(),
                        premium = cuenta.getOrNull()?.premium ?: false,
                    )
                }
            } else {
                Log.d("LessonsViewModel", lecciones.exceptionOrNull()?.message.toString())
                _uiState.update {
                    it.copy(isLoading = false, errorMessageRes = mensajeDeError(lecciones.exceptionOrNull(), R.string.error_cargar_lecciones))
                }
            }
        }
    }
}
