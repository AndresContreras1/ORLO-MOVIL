package com.orlo.app.ui.screens.wardrobe

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orlo.app.R
import com.orlo.app.data.injection.IoDispatcher
import com.orlo.app.data.model.CategoriaPrenda
import com.orlo.app.data.model.ConversacionUi
import com.orlo.app.data.model.OutfitUi
import com.orlo.app.data.model.PerfilUi
import com.orlo.app.data.model.PrendaUi
import com.orlo.app.data.repository.ArmarioRepository
import com.orlo.app.data.repository.AsesorRepository
import com.orlo.app.data.repository.CuentaRepository
import com.orlo.app.data.repository.UsuarioRepository
import com.orlo.app.ui.utils.mensajeDeError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Mi armario: prendas, outfits, mi estilo e historial en un solo lugar. Las prendas, los outfits,
// el perfil y las conversaciones llegan en vivo de Firestore: guardar, borrar o marcar favorito no
// necesita recargar, la lista cambia sola (y al instante, con la cache local de Firestore)
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

    private var observacion: Job? = null

    // Se llama al entrar (LaunchedEffect). Si ya esta escuchando no hace nada; si fallo, empieza
    // de nuevo
    fun cargar() {
        if (observacion?.isActive == true && _uiState.value.errorCargaRes == null) return
        observacion?.cancel()
        _uiState.update { it.copy(isLoading = it.cuenta == null, errorCargaRes = null) }
        observacion = viewModelScope.launch(ioDispatcher) {
            // La cuenta y los resultados del cuestionario se piden una vez
            val (cuenta, resultados) = coroutineScope {
                val cuenta = async { cuentaRepository.getCuenta() }
                val resultados = async { usuarioRepository.getResultados() }
                cuenta.await() to resultados.await()
            }
            if (cuenta.isFailure) {
                Log.d("WardrobeViewModel", cuenta.exceptionOrNull()?.message.toString())
                _uiState.update {
                    it.copy(isLoading = false, errorCargaRes = mensajeDeError(cuenta.exceptionOrNull(), R.string.error_cargar_armario))
                }
                return@launch
            }
            _uiState.update { it.copy(cuenta = cuenta.getOrNull(), resultados = resultados.getOrNull().orEmpty()) }

            combine(
                armarioRepository.observarPrendas(),
                armarioRepository.observarOutfits(),
                usuarioRepository.observarPerfil(),
                asesorRepository.observarConversaciones(),
            ) { prendas, outfits, perfil, conversaciones ->
                EnVivo(prendas, outfits, perfil, conversaciones)
            }.collect { datos ->
                // Sin prendas u outfits no hay armario que mostrar; el perfil y las conversaciones
                // solo completan Mi estilo y el historial
                val error = datos.prendas.exceptionOrNull() ?: datos.outfits.exceptionOrNull()
                if (error != null) {
                    Log.d("WardrobeViewModel", error.message.toString())
                    _uiState.update {
                        it.copy(isLoading = false, errorCargaRes = mensajeDeError(error, R.string.error_cargar_armario))
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            prendas = datos.prendas.getOrThrow(),
                            outfits = datos.outfits.getOrThrow(),
                            perfil = datos.perfil.getOrNull() ?: it.perfil,
                            conversaciones = datos.conversaciones.getOrNull() ?: it.conversaciones,
                        )
                    }
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

    fun alternarPrendasFavoritas() {
        _uiState.update { it.copy(soloPrendasFavoritas = !it.soloPrendasFavoritas) }
    }

    fun alternarOutfitsFavoritos() {
        _uiState.update { it.copy(soloOutfitsFavoritos = !it.soloOutfitsFavoritos) }
    }

    // Firestore cambia la lista en pantalla de una vez; si la escritura falla, la devuelve como
    // estaba y aqui solo queda el mensaje
    fun marcarPrendaFavorita(prenda: PrendaUi) {
        viewModelScope.launch(ioDispatcher) {
            val result = armarioRepository.marcarPrendaFavorita(prenda.id, !prenda.favorita)
            if (result.isFailure) {
                Log.d("WardrobeViewModel", result.exceptionOrNull()?.message.toString())
                _uiState.update { it.copy(errorMessageRes = mensajeDeError(result.exceptionOrNull(), R.string.error_guardar_prenda)) }
            }
        }
    }

    fun marcarFavorito(outfit: OutfitUi) {
        viewModelScope.launch(ioDispatcher) {
            val result = armarioRepository.marcarOutfitFavorito(outfit.id, !outfit.favorito)
            if (result.isFailure) {
                Log.d("WardrobeViewModel", result.exceptionOrNull()?.message.toString())
                _uiState.update { it.copy(errorMessageRes = mensajeDeError(result.exceptionOrNull(), R.string.error_guardar_outfit)) }
            }
        }
    }

    fun duplicar(outfit: OutfitUi, sufijo: String) {
        _uiState.update { it.copy(outfitConMenu = null) }
        viewModelScope.launch(ioDispatcher) {
            val result = armarioRepository.duplicarOutfit(outfit, sufijo, System.currentTimeMillis())
            if (result.isFailure) {
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

    fun borrarOutfit() {
        val outfit = _uiState.value.outfitPorBorrar ?: return
        _uiState.update { it.copy(outfitPorBorrar = null) }
        viewModelScope.launch(ioDispatcher) {
            val result = armarioRepository.borrarOutfit(outfit.id)
            if (result.isFailure) {
                Log.d("WardrobeViewModel", result.exceptionOrNull()?.message.toString())
                _uiState.update { it.copy(errorMessageRes = mensajeDeError(result.exceptionOrNull(), R.string.error_borrar_outfit)) }
            }
        }
    }
}

// Lo ultimo que llego de cada fuente en vivo
private data class EnVivo(
    val prendas: Result<List<PrendaUi>>,
    val outfits: Result<List<OutfitUi>>,
    val perfil: Result<PerfilUi>,
    val conversaciones: Result<List<ConversacionUi>>,
)
