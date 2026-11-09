package com.orlo.app.ui.screens.garmentform

import android.net.Uri
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orlo.app.R
import com.orlo.app.data.injection.IoDispatcher
import com.orlo.app.data.model.CategoriaPrenda
import com.orlo.app.data.model.MAX_NOMBRE_LENGTH
import com.orlo.app.data.model.PrendaUi
import com.orlo.app.data.repository.ArmarioRepository
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

data class GarmentFormState(
    val prenda: PrendaUi = PrendaUi(),
    // La foto elegida en la galeria, todavia sin subir
    val fotoNueva: Uri? = null,
    val esNueva: Boolean = true,
    val isLoading: Boolean = false,
    @StringRes val errorMessageRes: Int? = null,
    val guardando: Boolean = false,
    val confirmarBorrado: Boolean = false,
    val navigateBack: Boolean = false,
)

// Agregar o editar una prenda. Se puede subir una foto (opcion A) o elegir del catalogo generico
// sin foto (opcion B): categoria, color y un nombre como "camiseta blanca"
@HiltViewModel
class GarmentFormViewModel @Inject constructor(
    private val armarioRepository: ArmarioRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(GarmentFormState())
    val uiState: StateFlow<GarmentFormState> = _uiState.asStateFlow()

    // Sin id es una prenda nueva y no hay nada que cargar
    fun cargar(prendaId: String?) {
        if (prendaId.isNullOrBlank()) {
            _uiState.update { GarmentFormState() }
            return
        }
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isLoading = true, esNueva = false, errorMessageRes = null) }
            val result = armarioRepository.getPrenda(prendaId)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, prenda = result.getOrThrow()) }
            } else {
                Log.d("GarmentFormViewModel", result.exceptionOrNull()?.message.toString())
                _uiState.update {
                    it.copy(isLoading = false, errorMessageRes = mensajeDeError(result.exceptionOrNull(), R.string.error_cargar_prenda))
                }
            }
        }
    }

    fun updateNombre(nombre: String) {
        _uiState.update { it.copy(prenda = it.prenda.copy(nombre = nombre.take(MAX_NOMBRE_LENGTH)), errorMessageRes = null) }
    }

    fun updateCategoria(categoria: CategoriaPrenda) {
        _uiState.update { it.copy(prenda = it.prenda.copy(categoria = categoria), errorMessageRes = null) }
    }

    fun updateColor(color: String) {
        _uiState.update { it.copy(prenda = it.prenda.copy(color = color), errorMessageRes = null) }
    }

    fun updateFoto(uri: Uri) {
        _uiState.update { it.copy(fotoNueva = uri, errorMessageRes = null) }
    }

    // Un atajo del catalogo generico: llena el nombre y la categoria de una vez
    fun usarSugerencia(nombre: String, categoria: CategoriaPrenda) {
        _uiState.update { it.copy(prenda = it.prenda.copy(nombre = nombre, categoria = categoria), errorMessageRes = null) }
    }

    fun guardar() {
        val estado = _uiState.value
        if (estado.guardando) return
        if (estado.prenda.color.isBlank()) {
            _uiState.update { it.copy(errorMessageRes = R.string.error_elige_color) }
            return
        }

        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(guardando = true, errorMessageRes = null) }
            val result = armarioRepository.guardarPrenda(estado.prenda, estado.fotoNueva, System.currentTimeMillis())
            if (result.isSuccess) {
                _uiState.update { it.copy(guardando = false, navigateBack = true) }
            } else {
                Log.d("GarmentFormViewModel", result.exceptionOrNull()?.message.toString())
                _uiState.update {
                    it.copy(guardando = false, errorMessageRes = mensajeDeError(result.exceptionOrNull(), R.string.error_guardar_prenda))
                }
            }
        }
    }

    fun pedirBorrado() {
        _uiState.update { it.copy(confirmarBorrado = true) }
    }

    fun cancelarBorrado() {
        _uiState.update { it.copy(confirmarBorrado = false) }
    }

    fun borrar() {
        val prenda = _uiState.value.prenda
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(confirmarBorrado = false, guardando = true, errorMessageRes = null) }
            val result = armarioRepository.borrarPrenda(prenda)
            if (result.isSuccess) {
                _uiState.update { it.copy(guardando = false, navigateBack = true) }
            } else {
                Log.d("GarmentFormViewModel", result.exceptionOrNull()?.message.toString())
                _uiState.update {
                    it.copy(guardando = false, errorMessageRes = mensajeDeError(result.exceptionOrNull(), R.string.error_borrar_prenda))
                }
            }
        }
    }
}
