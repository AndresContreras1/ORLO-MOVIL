package com.orlo.app.ui.screens.lessons

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orlo.app.R
import com.orlo.app.data.injection.IoDispatcher
import com.orlo.app.data.repository.CuentaRepository
import com.orlo.app.data.repository.LeccionRepository
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

// Aprende: las lecciones con el avance en vivo. Al completar una en el detalle, la lista se
// actualiza sola al volver
@HiltViewModel
class LessonsViewModel @Inject constructor(
    private val leccionRepository: LeccionRepository,
    private val cuentaRepository: CuentaRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(LessonsState())
    val uiState: StateFlow<LessonsState> = _uiState.asStateFlow()

    private var observacion: Job? = null

    fun cargar() {
        if (observacion?.isActive == true && _uiState.value.errorMessageRes == null) return
        observacion?.cancel()
        _uiState.update { it.copy(isLoading = it.lecciones.isEmpty(), errorMessageRes = null) }
        observacion = viewModelScope.launch(ioDispatcher) {
            val cuenta = cuentaRepository.getCuenta()
            _uiState.update { it.copy(premium = cuenta.getOrNull()?.premium ?: false) }

            leccionRepository.observarLecciones().collect { result ->
                if (result.isSuccess) {
                    _uiState.update { it.copy(isLoading = false, lecciones = result.getOrThrow()) }
                } else {
                    Log.d("LessonsViewModel", result.exceptionOrNull()?.message.toString())
                    _uiState.update {
                        it.copy(isLoading = false, errorMessageRes = mensajeDeError(result.exceptionOrNull(), R.string.error_cargar_lecciones))
                    }
                }
            }
        }
    }
}
