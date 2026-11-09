package com.orlo.app.ui.screens.outfitform

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orlo.app.R
import com.orlo.app.data.injection.IoDispatcher
import com.orlo.app.data.model.MAX_NOMBRE_LENGTH
import com.orlo.app.data.model.OcasionOutfit
import com.orlo.app.data.model.OutfitUi
import com.orlo.app.data.model.PrendaUi
import com.orlo.app.data.repository.ArmarioRepository
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

// Crear un outfit eligiendo prendas del armario, o editar uno (tambien los del asesor)
@HiltViewModel
class OutfitFormViewModel @Inject constructor(
    private val armarioRepository: ArmarioRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(OutfitFormState())
    val uiState: StateFlow<OutfitFormState> = _uiState.asStateFlow()

    fun cargar(outfitId: String?) {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isLoading = true, esNuevo = outfitId.isNullOrBlank(), errorCargaRes = null) }
            coroutineScope {
                val prendasAsync = async { armarioRepository.getPrendas() }
                val outfitAsync = async {
                    if (outfitId.isNullOrBlank()) Result.success(OutfitUi()) else armarioRepository.getOutfit(outfitId)
                }
                val prendas = prendasAsync.await()
                val outfit = outfitAsync.await()

                if (prendas.isSuccess && outfit.isSuccess) {
                    _uiState.update { it.copy(isLoading = false, prendas = prendas.getOrThrow(), outfit = outfit.getOrThrow()) }
                } else {
                    val error = outfit.exceptionOrNull() ?: prendas.exceptionOrNull()
                    Log.d("OutfitFormViewModel", error?.message.toString())
                    _uiState.update { it.copy(isLoading = false, errorCargaRes = mensajeDeError(error, R.string.error_cargar_outfit)) }
                }
            }
        }
    }

    fun updateNombre(nombre: String) {
        _uiState.update { it.copy(outfit = it.outfit.copy(nombre = nombre.take(MAX_NOMBRE_LENGTH)), errorMessageRes = null) }
    }

    fun updateOcasion(ocasion: OcasionOutfit) {
        _uiState.update { it.copy(outfit = it.outfit.copy(ocasion = ocasion), errorMessageRes = null) }
    }

    fun togglePrenda(prendaId: String) {
        _uiState.update { estado ->
            val ids = estado.outfit.prendasIds
            val nuevos = if (prendaId in ids) ids - prendaId else ids + prendaId
            estado.copy(outfit = estado.outfit.copy(prendasIds = nuevos), errorMessageRes = null)
        }
    }

    fun guardar() {
        val estado = _uiState.value
        if (estado.guardando) return

        val errorRes = when {
            estado.outfit.nombre.isBlank() -> R.string.error_nombre_outfit
            estado.outfit.prendasIds.isEmpty() && estado.outfit.prendasTexto.isEmpty() -> R.string.error_elige_prendas
            else -> null
        }
        if (errorRes != null) {
            _uiState.update { it.copy(errorMessageRes = errorRes) }
            return
        }

        _uiState.update { it.copy(guardando = true, errorMessageRes = null) }
        viewModelScope.launch(ioDispatcher) {
            val result = armarioRepository.guardarOutfit(estado.outfit, System.currentTimeMillis())
            if (result.isSuccess) {
                _uiState.update { it.copy(guardando = false, navigateBack = true) }
            } else {
                Log.d("OutfitFormViewModel", result.exceptionOrNull()?.message.toString())
                _uiState.update {
                    it.copy(guardando = false, errorMessageRes = mensajeDeError(result.exceptionOrNull(), R.string.error_guardar_outfit))
                }
            }
        }
    }
}
