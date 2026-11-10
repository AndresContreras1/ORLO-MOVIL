package com.orlo.app.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orlo.app.R
import com.orlo.app.data.injection.IoDispatcher
import com.orlo.app.data.repository.CuentaRepository
import com.orlo.app.data.repository.NotificacionRepository
import com.orlo.app.data.repository.OutfitDelDiaRepository
import com.orlo.app.data.repository.UsuarioRepository
import com.orlo.app.ui.utils.mensajeDeError
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// El inicio: el perfil llega en vivo (nombre, foto y estilo cambian solos al editarlos) y la cuenta
// se pide cada vez que se vuelve, porque los mensajes del asesor bajan en otra pantalla
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val cuentaRepository: CuentaRepository,
    private val notificacionRepository: NotificacionRepository,
    private val outfitDelDiaRepository: OutfitDelDiaRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeState())
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

    private var observacion: Job? = null

    init {
        suscribirALecciones()
        cargarOutfitDelDia()
    }

    fun cargar() {
        cargarCuenta()
        // El perfil ya esta escuchando: solo se vuelve a empezar si fallo
        if (observacion?.isActive == true && _uiState.value.errorMessageRes == null) return
        observacion?.cancel()
        _uiState.update { it.copy(isLoading = it.nombre.isBlank(), errorMessageRes = null) }
        observacion = viewModelScope.launch(ioDispatcher) {
            usuarioRepository.observarPerfil().collect { result ->
                val perfil = result.getOrNull()
                if (perfil != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            nombre = perfil.nombre,
                            fotoUrl = perfil.fotoUrl,
                            resultado = perfil.resultadoActual,
                        )
                    }
                } else {
                    Log.d("HomeViewModel", result.exceptionOrNull()?.message.toString())
                    _uiState.update {
                        it.copy(isLoading = false, errorMessageRes = mensajeDeError(result.exceptionOrNull(), R.string.error_cargar_inicio))
                    }
                }
            }
        }
    }

    // Si falla, el inicio se ve igual: solo no se muestran los mensajes del asesor
    private fun cargarCuenta() {
        viewModelScope.launch(ioDispatcher) {
            val cuenta = cuentaRepository.getCuenta()
            _uiState.update { it.copy(cuenta = cuenta.getOrNull() ?: it.cuenta) }
        }
    }

    // Si ya se pidio hoy, se muestra el guardado; si no, la tarjeta ofrece pedirlo
    private fun cargarOutfitDelDia() {
        viewModelScope.launch(ioDispatcher) {
            val result = outfitDelDiaRepository.getDeHoy(hoy())
            if (result.isSuccess) {
                _uiState.update { it.copy(outfitDelDia = result.getOrNull()) }
            } else {
                Log.d("HomeViewModel", result.exceptionOrNull()?.message.toString())
            }
        }
    }

    // Tambien sirve para "Otra opcion": el nuevo reemplaza al de hoy. El backend permite 3 al dia
    fun pedirOutfitDelDia() {
        if (_uiState.value.pidiendoOutfit) return
        _uiState.update { it.copy(pidiendoOutfit = true, errorOutfitRes = null) }
        viewModelScope.launch(ioDispatcher) {
            val result = outfitDelDiaRepository.pedir(hoy(), System.currentTimeMillis())
            if (result.isSuccess) {
                _uiState.update { it.copy(pidiendoOutfit = false, outfitDelDia = result.getOrThrow()) }
            } else {
                Log.d("HomeViewModel", result.exceptionOrNull()?.message.toString())
                _uiState.update {
                    it.copy(pidiendoOutfit = false, errorOutfitRes = mensajeDeError(result.exceptionOrNull(), R.string.error_outfit_del_dia))
                }
            }
        }
    }

    // La fecha local, que es el id del documento del outfit del dia (AAAA-MM-DD)
    private fun hoy(): String = LocalDate.now().toString()

    // Para recibir el aviso de cada leccion nueva. Si falla (sin Google Play, sin red) la app
    // sigue igual: solo no llegan los avisos
    private fun suscribirALecciones() {
        viewModelScope.launch(ioDispatcher) {
            val result = notificacionRepository.suscribirALecciones()
            if (result.isFailure) Log.d("HomeViewModel", result.exceptionOrNull()?.message.toString())
        }
    }
}
