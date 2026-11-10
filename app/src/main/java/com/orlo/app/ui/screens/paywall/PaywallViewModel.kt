package com.orlo.app.ui.screens.paywall

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

// El cobro con la tienda de aplicaciones todavia no esta conectado: el boton explica que por ahora
// el acceso lo activa el equipo, en vez de fallar
@HiltViewModel
class PaywallViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(PaywallState())
    val uiState: StateFlow<PaywallState> = _uiState.asStateFlow()

    fun suscribirme() {
        _uiState.update { it.copy(mostrarAviso = true) }
    }

    fun cerrarAviso() {
        _uiState.update { it.copy(mostrarAviso = false) }
    }
}
