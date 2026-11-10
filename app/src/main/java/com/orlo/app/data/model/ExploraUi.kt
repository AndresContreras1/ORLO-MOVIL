package com.orlo.app.data.model

// Las pestanas de Explora. Las tres primeras salen del catalogo del backend
enum class SeccionExplora(val codigo: String?) {
    PARA_TU_ESTILO("FOR_YOUR_STYLE"),
    COMPLETA_TU_ARMARIO(null),
    TENDENCIAS("TRENDS"),
    COMPLEMENTOS("ACCESSORIES"),
    GUARDADOS(null),
}

// Una prenda de referencia del catalogo: para inspirarse, no es del armario del usuario
data class ItemExploraUi(
    val id: String = "",
    val nombre: String = "",
    val imagenUrl: String? = null,
    // Autor y licencia de la foto
    val credito: String = "",
    val categoria: CategoriaPrenda = CategoriaPrenda.OTRO,
    val color: String = "",
    val estilos: List<String> = emptyList(),
    val porQueTeQueda: String = "",
    val tendencia: Boolean = false,
    val accesorio: Boolean = false,
    // Solo en el detalle: casual, universidad y elegante
    val ideas: List<IdeaCombinarUi> = emptyList(),
)

data class IdeaCombinarUi(
    val contexto: String = "",
    val prendas: List<String> = emptyList(),
)

// Un tipo de prenda que daria mas combinaciones con lo que ya hay en el armario
data class SugerenciaArmarioUi(
    val categoria: CategoriaPrenda = CategoriaPrenda.OTRO,
    val titulo: String = "",
    val razon: String = "",
    val items: List<ItemExploraUi> = emptyList(),
)

data class TiendaUi(
    val id: String = "",
    val nombre: String = "",
    val logoUrl: String? = null,
    val tipos: List<String> = emptyList(),
    val url: String = "",
    val afiliada: Boolean = false,
)

data class GuardadoUi(
    val itemId: String = "",
    val nombre: String = "",
    val imagenUrl: String? = null,
    val categoria: CategoriaPrenda = CategoriaPrenda.OTRO,
    val fecha: Long = 0,
)

// Un guardado se muestra con la misma tarjeta que el catalogo
fun GuardadoUi.comoItem(): ItemExploraUi =
    ItemExploraUi(id = itemId, nombre = nombre, imagenUrl = imagenUrl, categoria = categoria)

// El outfit que el asesor propone para hoy con lo que hay en el armario
data class OutfitDelDiaUi(
    val titulo: String = "",
    val prendas: List<String> = emptyList(),
    val porQue: String = "",
    val fecha: Long = 0,
)
