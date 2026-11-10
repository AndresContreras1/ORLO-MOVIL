package com.orlo.app.ui.screens.exploreitem

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orlo.app.R
import com.orlo.app.data.injection.IoDispatcher
import com.orlo.app.data.repository.ExploraRepository
import com.orlo.app.data.repository.TiendaRepository
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

// El detalle de una prenda de Explora: la prenda con sus ideas para combinarla, las tiendas que
// venden ese tipo de prenda y si esta guardada (en vivo)
@HiltViewModel
class ExploreItemViewModel @Inject constructor(
    private val exploraRepository: ExploraRepository,
    private val tiendaRepository: TiendaRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreItemState())
    val uiState: StateFlow<ExploreItemState> = _uiState.asStateFlow()

    private var guardados: Job? = null

    // Primero la prenda; las tiendas dependen de su categoria. Si fallan las tiendas, la prenda se
    // muestra igual sin esa parte
    fun cargar(itemId: String) {
        observarGuardado(itemId)
        _uiState.update { it.copy(isLoading = it.item == null, errorCargaRes = null) }
        viewModelScope.launch(ioDispatcher) {
            val item = exploraRepository.getItem(itemId)
            if (item.isFailure) {
                Log.d("ExploreItemViewModel", item.exceptionOrNull()?.message.toString())
                _uiState.update {
                    it.copy(isLoading = false, errorCargaRes = mensajeDeError(item.exceptionOrNull(), R.string.error_cargar_explora))
                }
                return@launch
            }
            val prenda = item.getOrThrow()
            _uiState.update { it.copy(isLoading = false, item = prenda) }

            val tiendas = tiendaRepository.getTiendas(prenda.categoria)
            _uiState.update { it.copy(tiendas = tiendas.getOrNull().orEmpty()) }
        }
    }

    private fun observarGuardado(itemId: String) {
        if (guardados?.isActive == true) return
        guardados = viewModelScope.launch(ioDispatcher) {
            exploraRepository.observarGuardados().collect { result ->
                val lista = result.getOrNull() ?: return@collect
                _uiState.update { it.copy(guardado = lista.any { guardado -> guardado.itemId == itemId }) }
            }
        }
    }

    fun alternarGuardado() {
        val item = _uiState.value.item ?: return
        val guardado = _uiState.value.guardado
        _uiState.update { it.copy(errorMessageRes = null) }
        viewModelScope.launch(ioDispatcher) {
            val result = if (guardado) exploraRepository.quitar(item.id) else exploraRepository.guardar(item, System.currentTimeMillis())
            if (result.isFailure) {
                Log.d("ExploreItemViewModel", result.exceptionOrNull()?.message.toString())
                _uiState.update { it.copy(errorMessageRes = mensajeDeError(result.exceptionOrNull(), R.string.error_guardar_item)) }
            }
        }
    }
}
