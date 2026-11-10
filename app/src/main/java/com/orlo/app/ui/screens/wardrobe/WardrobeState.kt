package com.orlo.app.ui.screens.wardrobe

import androidx.annotation.StringRes
import com.orlo.app.data.model.CategoriaPrenda
import com.orlo.app.data.model.ConversacionUi
import com.orlo.app.data.model.CuentaUi
import com.orlo.app.data.model.ElementoHistorialUi
import com.orlo.app.data.model.OutfitUi
import com.orlo.app.data.model.PerfilUi
import com.orlo.app.data.model.PrendaUi
import com.orlo.app.data.model.ResultadoUi

// Las cuatro partes de Mi armario
enum class PestanaArmario { PRENDAS, OUTFITS, MI_ESTILO, HISTORIAL }

data class WardrobeState(
    val pestana: PestanaArmario = PestanaArmario.PRENDAS,
    val cuenta: CuentaUi? = null,
    val prendas: List<PrendaUi> = emptyList(),
    val outfits: List<OutfitUi> = emptyList(),
    val perfil: PerfilUi = PerfilUi(),
    val resultados: List<ResultadoUi> = emptyList(),
    val conversaciones: List<ConversacionUi> = emptyList(),
    // null muestra todas las categorias
    val filtroCategoria: CategoriaPrenda? = null,
    val filtroColor: String? = null,
    val soloPrendasFavoritas: Boolean = false,
    val soloOutfitsFavoritos: Boolean = false,
    val isLoading: Boolean = true,
    @StringRes val errorCargaRes: Int? = null,
    @StringRes val errorMessageRes: Int? = null,
    // El outfit que se va a borrar, mientras se confirma
    val outfitPorBorrar: OutfitUi? = null,
    // El outfit con el menu de opciones abierto (duplicar, borrar)
    val outfitConMenu: String? = null,
) {
    val premium: Boolean get() = cuenta?.premium == true

    // Los contadores de los filtros de favoritos
    val prendasFavoritas: Int get() = prendas.count { it.favorita }
    val outfitsFavoritos: Int get() = outfits.count { it.favorito }

    val prendasFiltradas: List<PrendaUi>
        get() = prendas.filter { prenda ->
            (!soloPrendasFavoritas || prenda.favorita) &&
                (filtroCategoria == null || prenda.categoria == filtroCategoria) &&
                (filtroColor == null || prenda.color == filtroColor)
        }

    val outfitsFiltrados: List<OutfitUi>
        get() = if (soloOutfitsFavoritos) outfits.filter { it.favorito } else outfits

    // El historial junta tres fuentes y las ordena por fecha, de la mas reciente
    val historial: List<ElementoHistorialUi>
        get() = (
            resultados.map { ElementoHistorialUi.Resultado(it) } +
                outfits.map { ElementoHistorialUi.Outfit(it) } +
                conversaciones.map { ElementoHistorialUi.Conversacion(it) }
            ).sortedByDescending { it.fecha }

    // Para mostrar un outfit armado a mano con los nombres de sus prendas
    fun nombresDePrendas(outfit: OutfitUi): List<String> =
        outfit.prendasTexto.ifEmpty {
            outfit.prendasIds.mapNotNull { id -> prendas.firstOrNull { it.id == id }?.nombre?.takeIf { it.isNotBlank() } }
        }
}
