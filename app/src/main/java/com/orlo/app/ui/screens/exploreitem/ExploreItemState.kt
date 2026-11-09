package com.orlo.app.ui.screens.exploreitem

import androidx.annotation.StringRes
import com.orlo.app.data.model.ItemExploraUi
import com.orlo.app.data.model.TiendaUi

data class ExploreItemState(
    val item: ItemExploraUi? = null,
    val tiendas: List<TiendaUi> = emptyList(),
    val guardado: Boolean = false,
    val isLoading: Boolean = true,
    @StringRes val errorCargaRes: Int? = null,
    @StringRes val errorMessageRes: Int? = null,
)
