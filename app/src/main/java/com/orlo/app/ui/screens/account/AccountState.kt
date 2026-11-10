package com.orlo.app.ui.screens.account

import androidx.annotation.StringRes
import com.orlo.app.data.model.CuentaUi

data class AccountState(
    val nombre: String = "",
    val correo: String = "",
    val fotoUrl: String? = null,
    // Lo cuenta una Cloud Function cada vez que se crea o se borra una prenda
    val cantidadPrendas: Int = 0,
    // El estilo del cuestionario y el avance de Aprende, para el resumen del perfil
    val estilo: String? = null,
    val leccionesCompletadas: Int = 0,
    val subiendoFoto: Boolean = false,
    @StringRes val avisoRes: Int? = null,
    // null si no se pudo leer la cuenta del backend: la pantalla sigue sirviendo (cerrar sesion,
    // editar datos) y lo explica en la tarjeta de la suscripcion
    val cuenta: CuentaUi? = null,
    val isLoading: Boolean = true,
    @StringRes val errorCargaRes: Int? = null,
    @StringRes val errorMessageRes: Int? = null,
    val confirmarBorrado: Boolean = false,
    val borrando: Boolean = false,
    // Al cerrar sesion o borrar la cuenta se vuelve a la bienvenida
    val salir: Boolean = false,
)
