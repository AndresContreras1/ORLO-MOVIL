package com.orlo.app.ui.screens.explore

import androidx.annotation.StringRes
import com.orlo.app.data.model.GuardadoUi
import com.orlo.app.data.model.ItemExploraUi
import com.orlo.app.data.model.SeccionExplora
import com.orlo.app.data.model.SugerenciaArmarioUi

data class ExploreState(
    val seccion: SeccionExplora = SeccionExplora.PARA_TU_ESTILO,
    // Lo que ya se cargo de cada seccion del catalogo: volver a una pestana no lo pide de nuevo
    val items: Map<SeccionExplora, List<ItemExploraUi>> = emptyMap(),
    // null mientras no se ha pedido "Completa tu armario"
    val sugerencias: List<SugerenciaArmarioUi>? = null,
    val guardados: List<GuardadoUi> = emptyList(),
    val cargando: Boolean = false,
    @StringRes val errorCargaRes: Int? = null,
    @StringRes val errorMessageRes: Int? = null,
) {
    val idsGuardados: Set<String> get() = guardados.map { it.itemId }.toSet()

    val itemsDeLaSeccion: List<ItemExploraUi> get() = items[seccion].orEmpty()
}
