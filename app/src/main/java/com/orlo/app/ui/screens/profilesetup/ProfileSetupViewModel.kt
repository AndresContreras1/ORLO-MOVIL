package com.orlo.app.ui.screens.profilesetup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orlo.app.R
import com.orlo.app.data.injection.IoDispatcher
import com.orlo.app.data.model.MAX_NOMBRE_LENGTH
import com.orlo.app.data.model.PerfilUi
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

// Los datos basicos del perfil. Sirve en el registro y para editarlos despues desde Mi estilo o
// Cuenta: el perfil se puede cambiar en cualquier momento sin perder el historial
@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileSetupState())
    val uiState: StateFlow<ProfileSetupState> = _uiState.asStateFlow()

    init {
        cargarPerfil()
    }

    fun cargarPerfil() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isLoading = true, errorCargaRes = null) }
            val result = usuarioRepository.getPerfil()
            if (result.isSuccess) {
                // Sin documento todavia se empieza con el perfil vacio
                val perfil = result.getOrNull() ?: PerfilUi()
                _uiState.update { it.copy(isLoading = false, perfil = perfil) }
            } else {
                Log.d("ProfileSetupViewModel", result.exceptionOrNull()?.message.toString())
                _uiState.update {
                    it.copy(isLoading = false, errorCargaRes = mensajeDeError(result.exceptionOrNull(), R.string.error_cargar_perfil))
                }
            }
        }
    }

    private fun editar(cambio: (PerfilUi) -> PerfilUi) {
        _uiState.update { it.copy(perfil = cambio(it.perfil), errorMessageRes = null) }
    }

    fun updateNombre(nombre: String) = editar { it.copy(nombre = nombre.take(MAX_NOMBRE_LENGTH)) }

    // En las listas de una sola opcion, tocar la elegida la quita: todo es opcional
    fun updateRangoEdad(codigo: String) = editar { it.copy(rangoEdad = if (it.rangoEdad == codigo) "" else codigo) }

    fun updateTratoPrendas(codigo: String) =
        editar { it.copy(tratoPrendas = if (it.tratoPrendas == codigo) "" else codigo) }

    fun updateClima(codigo: String) = editar { it.copy(clima = if (it.clima == codigo) "" else codigo) }

    fun updatePresupuesto(codigo: String) =
        editar { it.copy(presupuesto = if (it.presupuesto == codigo) "" else codigo) }

    fun toggleOcasion(codigo: String) = editar {
        it.copy(ocasiones = if (codigo in it.ocasiones) it.ocasiones - codigo else it.ocasiones + codigo)
    }

    // Sin consentimiento no se guardan los datos de apariencia: al quitarlo se borran
    fun updateConsentimiento(acepta: Boolean) = editar {
        if (acepta) {
            it.copy(consentimientoDatosSensibles = true)
        } else {
            it.copy(
                consentimientoDatosSensibles = false,
                tipoCuerpo = "",
                tonoPiel = "",
                colorCabello = "",
                colorOjos = "",
            )
        }
    }

    fun updateTipoCuerpo(codigo: String) =
        editar { it.copy(tipoCuerpo = if (it.tipoCuerpo == codigo) "" else codigo) }

    fun updateTonoPiel(codigo: String) = editar { it.copy(tonoPiel = codigo) }

    fun updateColorCabello(codigo: String) = editar { it.copy(colorCabello = codigo) }

    fun updateColorOjos(codigo: String) = editar { it.copy(colorOjos = codigo) }

    fun guardarButtonPressed() {
        val estado = _uiState.value
        if (estado.guardando) return

        if (estado.perfil.nombre.isBlank()) {
            _uiState.update { it.copy(errorMessageRes = R.string.error_nombre_obligatorio) }
            return
        }

        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(guardando = true, errorMessageRes = null) }
            val result = usuarioRepository.guardarPerfil(estado.perfil)
            if (result.isSuccess) {
                _uiState.update { it.copy(guardando = false, navigate = true) }
            } else {
                Log.d("ProfileSetupViewModel", result.exceptionOrNull()?.message.toString())
                _uiState.update {
                    it.copy(guardando = false, errorMessageRes = mensajeDeError(result.exceptionOrNull(), R.string.error_guardar_perfil))
                }
            }
        }
    }
}
