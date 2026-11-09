package com.orlo.app.ui.screens.lessondetail

import androidx.annotation.StringRes
import com.orlo.app.data.model.EstadoLeccion
import com.orlo.app.data.model.LeccionUi

data class LessonDetailState(
    val leccion: LeccionUi? = null,
    val estado: EstadoLeccion = EstadoLeccion.SIN_EMPEZAR,
    // Para la capsula de colorimetria: los colores del perfil ya resueltos (piel, cabello y ojos).
    // Vacia si el usuario no los lleno
    val coloresDelPerfil: List<String> = emptyList(),
    // La opcion que eligio en la pregunta de repaso
    val respuestaRepaso: Int? = null,
    val requierePremium: Boolean = false,
    val isLoading: Boolean = true,
    @StringRes val errorCargaRes: Int? = null,
    @StringRes val errorMessageRes: Int? = null,
    val guardando: Boolean = false,
)
