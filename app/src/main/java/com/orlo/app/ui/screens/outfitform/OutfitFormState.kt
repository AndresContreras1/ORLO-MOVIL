package com.orlo.app.ui.screens.outfitform

import androidx.annotation.StringRes
import com.orlo.app.data.model.OutfitUi
import com.orlo.app.data.model.PrendaUi

data class OutfitFormState(
    val outfit: OutfitUi = OutfitUi(),
    val prendas: List<PrendaUi> = emptyList(),
    val esNuevo: Boolean = true,
    val isLoading: Boolean = true,
    // Error al cargar: la pantalla muestra el error y no un formulario vacio
    @StringRes val errorCargaRes: Int? = null,
    @StringRes val errorMessageRes: Int? = null,
    val guardando: Boolean = false,
    val navigateBack: Boolean = false,
)
