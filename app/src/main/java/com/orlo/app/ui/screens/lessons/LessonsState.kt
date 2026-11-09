package com.orlo.app.ui.screens.lessons

import androidx.annotation.StringRes
import com.orlo.app.data.model.EstadoLeccion
import com.orlo.app.data.model.LeccionResumenUi

data class LessonsState(
    val lecciones: List<LeccionResumenUi> = emptyList(),
    val premium: Boolean = false,
    val isLoading: Boolean = true,
    @StringRes val errorMessageRes: Int? = null,
) {
    // El avance total, por ejemplo "3 de 6"
    val completadas: Int get() = lecciones.count { it.estado == EstadoLeccion.COMPLETADA }
}
