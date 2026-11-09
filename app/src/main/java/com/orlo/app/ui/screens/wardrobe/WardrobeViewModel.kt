package com.orlo.app.ui.screens.wardrobe

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orlo.app.R
import com.orlo.app.data.injection.IoDispatcher
import com.orlo.app.data.model.CategoriaPrenda
import com.orlo.app.data.model.ElementoHistorialUi
import com.orlo.app.data.model.OutfitUi
import com.orlo.app.data.model.PerfilUi
import com.orlo.app.data.repository.ArmarioRepository
import com.orlo.app.data.repository.AsesorRepository
import com.orlo.app.data.repository.CuentaRepository
import com.orlo.app.data.repository.UsuarioRepository
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

// Mi armario: prendas, outfits, mi estilo e historial en un solo lugar
@HiltViewModel
class WardrobeViewModel @Inject constructor(
    private val armarioRepository: ArmarioRepository,
    private val usuarioRepository: UsuarioRepository,
    private val asesorRepository: AsesorRepository,
    private val cuentaRepository: CuentaRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(WardrobeState())
    val uiState: StateFlow<WardrobeState> = _uiState.asStateFlow()

    // Se llama al entrar y al volver de un formulario: cada fuente se pide al tiempo
    fun cargar() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isLoading = it.cuenta == null, errorCargaRes = null) }

            coroutineScope {
                val cuentaAsync = async { cuentaRepository.getCuenta() }
                val prendasAsync = async { armarioRepository.getPrendas() }
                val outfitsAsync = async { armarioRepository.getOutfits() }
                val perfilAsync = async { usuarioRepository.getPerfil() }
                val resultadosAsync = async { usuarioRepository.getResultados() }
                val conversacionesAsync = async { asesorRepository.getConversaciones() }

                val cuenta = cuentaAsync.await()
                val prendas = prendasAsync.await()
                val outfits = outfitsAsync.await()

                if (cuenta.isFailure || prendas.isFailure || outfits.isFailure) {
                    val error = cuenta.exceptionOrNull() ?: prendas.exceptionOrNull() ?: outfits.exceptionOrNull()
                    Log.d("WardrobeViewModel", error?.message.toString())
                    _uiState.update {
                        it.copy(isLoading = false, errorCargaRes = mensajeDeError(error, R.string.error_cargar_armario))
                    }
                    return@coroutineScope
                }

                val listaOutfits = outfits.getOrThrow()
                // El historial junta tres fuentes y las ordena por fecha, de la mas reciente
                val historial = (
                    resultadosAsync.await().getOrNull().orEmpty().map { ElementoHistorialUi.Resultado(it) } +
                        listaOutfits.map { ElementoHistorialUi.Outfit(it) } +
                        conversacionesAsync.await().getOrNull().orEmpty().map { ElementoHistorialUi.Conversacion(it) }
                    ).sortedByDescending { it.fecha }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        cuenta = cuenta.getOrNull(),
                        prendas = prendas.getOrThrow(),
                        outfits = listaOutfits,
                        perfil = perfilAsync.await().getOrNull() ?: PerfilUi(),
                        historial = historial,
                    )
                }
            }
        }
    }

    fun cambiarPestana(pestana: PestanaArmario) {
        _uiState.update { it.copy(pestana = pestana) }
    }

    // Tocar el filtro elegido lo quita
    fun filtrarCategoria(categoria: CategoriaPrenda?) {
        _uiState.update { it.copy(filtroCategoria = if (it.filtroCategoria == categoria) null else categoria) }
    }

    fun filtrarColor(color: String?) {
        _uiState.update { it.copy(filtroColor = if (it.filtroColor == color) null else color) }
    }

    // Se cambia en pantalla de una vez y, si falla, se devuelve como estaba
    fun marcarFavorito(outfit: OutfitUi) {
        val nuevo = !outfit.favorito
        _uiState.update { estado ->
            estado.copy(outfits = estado.outfits.map { if (it.id == outfit.id) it.copy(favorito = nuevo) else it })
        }
        viewModelScope.launch(ioDispatcher) {
            val result = armarioRepository.marcarOutfitFavorito(outfit.id, nuevo)
            if (result.isFailure) {
                Log.d("WardrobeViewModel", result.exceptionOrNull()?.message.toString())
                _uiState.update { estado ->
                    estado.copy(
                        outfits = estado.outfits.map { if (it.id == outfit.id) it.copy(favorito = outfit.favorito) else it },
                        errorMessageRes = mensajeDeError(result.exceptionOrNull(), R.string.error_guardar_outfit),
                    )
                }
            }
        }
    }

    fun duplicar(outfit: OutfitUi, sufijo: String) {
        _uiState.update { it.copy(outfitConMenu = null) }
        viewModelScope.launch(ioDispatcher) {
            val result = armarioRepository.duplicarOutfit(outfit, sufijo, System.currentTimeMillis())
            if (result.isSuccess) {
                _uiState.update { it.copy(outfits = listOf(result.getOrThrow()) + it.outfits, errorMessageRes = null) }
            } else {
                Log.d("WardrobeViewModel", result.exceptionOrNull()?.message.toString())
                _uiState.update { it.copy(errorMessageRes = mensajeDeError(result.exceptionOrNull(), R.string.error_guardar_outfit)) }
            }
        }
    }

    fun abrirMenu(outfitId: String) {
        _uiState.update { it.copy(outfitConMenu = outfitId) }
    }

    fun cerrarMenu() {
        _uiState.update { it.copy(outfitConMenu = null) }
    }

    fun pedirBorrarOutfit(outfit: OutfitUi) {
        _uiState.update { it.copy(outfitPorBorrar = outfit, outfitConMenu = null) }
    }

    fun cancelarBorrarOutfit() {
        _uiState.update { it.copy(outfitPorBorrar = null) }
    }

    // La lista se actualiza cuando el repositorio confirma el borrado
    fun borrarOutfit() {
        val outfit = _uiState.value.outfitPorBorrar ?: return
        _uiState.update { it.copy(outfitPorBorrar = null) }
        viewModelScope.launch(ioDispatcher) {
            val result = armarioRepository.borrarOutfit(outfit.id)
            if (result.isSuccess) {
                _uiState.update { estado -> estado.copy(outfits = estado.outfits.filterNot { it.id == outfit.id }, errorMessageRes = null) }
            } else {
                Log.d("WardrobeViewModel", result.exceptionOrNull()?.message.toString())
                _uiState.update { it.copy(errorMessageRes = mensajeDeError(result.exceptionOrNull(), R.string.error_borrar_outfit)) }
            }
        }
    }
}
