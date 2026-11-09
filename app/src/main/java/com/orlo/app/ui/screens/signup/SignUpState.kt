package com.orlo.app.ui.screens.signup

import androidx.annotation.StringRes

data class SignUpState(
    val nombre: String = "",
    val email: String = "",
    val contrasena: String = "",
    val confirmacion: String = "",
    val contrasenaVisible: Boolean = false,
    // La autorizacion de tratamiento de datos: obligatoria y sin marcar de antemano
    val aceptaTerminos: Boolean = false,
    @StringRes val errorMessageRes: Int? = null,
    val cargando: Boolean = false,
    val navigate: Boolean = false,
)
