package com.orlo.app.ui.screens.splash

import androidx.annotation.StringRes

// A donde va la app al abrirse, segun en que punto del recorrido quedo el usuario
enum class DestinoInicial { BIENVENIDA, PERFIL, CUESTIONARIO, INICIO }

data class SplashState(
    val destino: DestinoInicial? = null,
    @StringRes val errorMessageRes: Int? = null,
)
