package com.orlo.app.ui.screens.home

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orlo.app.R
import com.orlo.app.data.injection.IoDispatcher
import com.orlo.app.data.model.CuentaUi
import com.orlo.app.data.model.ResultadoUi
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

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val cuentaRepository: CuentaRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeState())
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

    // Se llama al entrar y al volver a la pestana (LaunchedEffect): el estilo o el premium pueden
    // haber cambiado en otra pantalla
    fun cargar() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isLoading = it.nombre.isBlank(), errorMessageRes = null) }

            val (perfil, cuenta) = coroutineScope {
                val perfil = async { usuarioRepository.getPerfil() }
                val cuenta = async { cuentaRepository.getCuenta() }
                perfil.await() to cuenta.await()
            }

            if (perfil.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        nombre = perfil.getOrNull()?.nombre ?: "",
                        resultado = perfil.getOrNull()?.resultadoActual,
                        cuenta = cuenta.getOrNull(),
                    )
                }
            } else {
                Log.d("HomeViewModel", perfil.exceptionOrNull()?.message.toString())
                _uiState.update {
                    it.copy(isLoading = false, errorMessageRes = mensajeDeError(perfil.exceptionOrNull(), R.string.error_cargar_inicio))
                }
            }
        }
    }
}
