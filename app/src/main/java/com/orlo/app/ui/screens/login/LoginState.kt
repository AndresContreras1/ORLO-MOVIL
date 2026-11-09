package com.orlo.app.ui.screens.login

import androidx.annotation.StringRes

data class LoginState(
    val email: String = "",
    val contrasena: String = "",
    val contrasenaVisible: Boolean = false,
    // Se guarda el id del string, no el texto: el ViewModel no tiene acceso a los recursos
    @StringRes val errorMessageRes: Int? = null,
    val cargando: Boolean = false,
    // La navegacion solo entra cuando el ViewModel lo autoriza
    val navigate: Boolean = false,
)
