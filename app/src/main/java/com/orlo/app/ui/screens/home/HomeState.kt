package com.orlo.app.ui.screens.home

import androidx.annotation.StringRes
import com.orlo.app.data.model.CuentaUi
import com.orlo.app.data.model.ResultadoUi

data class HomeState(
    val nombre: String = "",
    val fotoUrl: String? = null,
    val resultado: ResultadoUi? = null,
    // null si no se pudo leer la cuenta: la pantalla no muestra el aviso de premium ni los mensajes
    val cuenta: CuentaUi? = null,
    val isLoading: Boolean = true,
    @StringRes val errorMessageRes: Int? = null,
)
