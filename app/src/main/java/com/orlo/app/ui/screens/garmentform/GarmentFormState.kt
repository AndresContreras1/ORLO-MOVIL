package com.orlo.app.ui.screens.garmentform

import android.net.Uri
import androidx.annotation.StringRes
import com.orlo.app.data.model.PrendaUi

data class GarmentFormState(
    val prenda: PrendaUi = PrendaUi(),
    // La foto elegida (galeria, archivo o camara), todavia sin subir
    val fotoNueva: Uri? = null,
    val esNueva: Boolean = true,
    val isLoading: Boolean = false,
    // Error al cargar una prenda existente: la pantalla muestra el error y no el formulario vacio
    @StringRes val errorCargaRes: Int? = null,
    // Error de validacion o al guardar, debajo del formulario
    @StringRes val errorMessageRes: Int? = null,
    val guardando: Boolean = false,
    val confirmarBorrado: Boolean = false,
    val navigateBack: Boolean = false,
)
