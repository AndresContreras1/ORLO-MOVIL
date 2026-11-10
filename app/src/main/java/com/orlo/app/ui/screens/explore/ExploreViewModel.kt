package com.orlo.app.ui.screens.explore

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orlo.app.R
import com.orlo.app.data.injection.IoDispatcher
import com.orlo.app.data.model.ItemExploraUi
import com.orlo.app.data.model.SeccionExplora
import com.orlo.app.data.repository.ExploraRepository
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

// Explora: cada pestana se pide la primera vez que se abre. Los guardados llegan en vivo
@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val exploraRepository: ExploraRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreState())
    val uiState: StateFlow<ExploreState> = _uiState.asStateFlow()

    private var guardados: Job? = null

    // Se llama al entrar (LaunchedEffect)
    fun cargar() {
        observarGuardados()
        cargarSeccion(_uiState.value.seccion)
    }

    fun cambiarSeccion(seccion: SeccionExplora) {
        _uiState.update { it.copy(seccion = seccion, errorCargaRes = null) }
        cargarSeccion(seccion)
    }

    fun reintentar() {
        cargarSeccion(_uiState.value.seccion, forzar = true)
    }

    private fun observarGuardados() {
        if (guardados?.isActive == true) return
        guardados = viewModelScope.launch(ioDispatcher) {
            exploraRepository.observarGuardados().collect { result ->
                if (result.isSuccess) {
                    _uiState.update { it.copy(guardados = result.getOrThrow()) }
                } else {
                    Log.d("ExploreViewModel", result.exceptionOrNull()?.message.toString())
                }
            }
        }
    }

    private fun cargarSeccion(seccion: SeccionExplora, forzar: Boolean = false) {
        val estado = _uiState.value
        val yaEsta = when (seccion) {
            SeccionExplora.GUARDADOS -> true
            SeccionExplora.COMPLETA_TU_ARMARIO -> estado.sugerencias != null
            else -> estado.items.containsKey(seccion)
        }
        if (yaEsta && !forzar) return

        _uiState.update { it.copy(cargando = true, errorCargaRes = null) }
        viewModelScope.launch(ioDispatcher) {
            if (seccion == SeccionExplora.COMPLETA_TU_ARMARIO) {
                val result = exploraRepository.completarArmario()
                _uiState.update {
                    if (result.isSuccess) {
                        it.copy(cargando = false, sugerencias = result.getOrThrow())
                    } else {
                        Log.d("ExploreViewModel", result.exceptionOrNull()?.message.toString())
                        it.copy(cargando = false, errorCargaRes = mensajeDeError(result.exceptionOrNull(), R.string.error_cargar_explora))
                    }
                }
            } else {
                val result = exploraRepository.getSeccion(seccion)
                _uiState.update {
                    if (result.isSuccess) {
                        it.copy(cargando = false, items = it.items + (seccion to result.getOrThrow()))
                    } else {
                        Log.d("ExploreViewModel", result.exceptionOrNull()?.message.toString())
                        it.copy(cargando = false, errorCargaRes = mensajeDeError(result.exceptionOrNull(), R.string.error_cargar_explora))
                    }
                }
            }
        }
    }

    // El marcador guarda o quita. La pestana Guardados cambia sola porque escucha Firestore
    fun alternarGuardado(item: ItemExploraUi) {
        val guardado = item.id in _uiState.value.idsGuardados
        viewModelScope.launch(ioDispatcher) {
            val result = if (guardado) {
                exploraRepository.quitar(item.id)
            } else {
                exploraRepository.guardar(item, System.currentTimeMillis())
            }
            if (result.isFailure) {
                Log.d("ExploreViewModel", result.exceptionOrNull()?.message.toString())
                _uiState.update { it.copy(errorMessageRes = mensajeDeError(result.exceptionOrNull(), R.string.error_guardar_item)) }
            }
        }
    }
}
