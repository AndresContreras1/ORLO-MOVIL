package com.orlo.app.ui.screens.wardrobe

import androidx.annotation.StringRes
import com.orlo.app.data.model.CategoriaPrenda
import com.orlo.app.data.model.CuentaUi
import com.orlo.app.data.model.ElementoHistorialUi
import com.orlo.app.data.model.OutfitUi
import com.orlo.app.data.model.PerfilUi
import com.orlo.app.data.model.PrendaUi

// Las cuatro partes de Mi armario
enum class PestanaArmario { PRENDAS, OUTFITS, MI_ESTILO, HISTORIAL }

data class WardrobeState(
    val pestana: PestanaArmario = PestanaArmario.PRENDAS,
    val cuenta: CuentaUi? = null,
    val prendas: List<PrendaUi> = emptyList(),
    val outfits: List<OutfitUi> = emptyList(),
    val perfil: PerfilUi = PerfilUi(),
    val historial: List<ElementoHistorialUi> = emptyList(),
    // null muestra todas las categorias
    val filtroCategoria: CategoriaPrenda? = null,
    val filtroColor: String? = null,
    val isLoading: Boolean = true,
    @StringRes val errorCargaRes: Int? = null,
    @StringRes val errorMessageRes: Int? = null,
    // El outfit que se va a borrar, mientras se confirma
    val outfitPorBorrar: OutfitUi? = null,
) {
    val premium: Boolean get() = cuenta?.premium == true

    val prendasFiltradas: List<PrendaUi>
        get() = prendas.filter { prenda ->
            (filtroCategoria == null || prenda.categoria == filtroCategoria) &&
                (filtroColor == null || prenda.color == filtroColor)
        }

    // Para mostrar un outfit armado a mano con los nombres de sus prendas
    fun nombresDePrendas(outfit: OutfitUi): List<String> =
        outfit.prendasTexto.ifEmpty {
            outfit.prendasIds.mapNotNull { id -> prendas.firstOrNull { it.id == id }?.nombre?.takeIf { it.isNotBlank() } }
        }
}
