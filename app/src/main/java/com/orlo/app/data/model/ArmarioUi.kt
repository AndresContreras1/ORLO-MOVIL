package com.orlo.app.data.model

import androidx.annotation.StringRes
import com.orlo.app.R

// Las categorias de una prenda. El codigo es el que se guarda en Firestore
enum class CategoriaPrenda(val codigo: String, @StringRes val etiqueta: Int, val paraAsesor: String) {
    ARRIBA("arriba", R.string.categoria_arriba, "parte de arriba"),
    ABAJO("abajo", R.string.categoria_abajo, "parte de abajo"),
    ABRIGO("abrigo", R.string.categoria_abrigo, "abrigo"),
    CALZADO("calzado", R.string.categoria_calzado, "calzado"),
    ACCESORIO("accesorio", R.string.categoria_accesorio, "accesorio"),
    OTRO("otro", R.string.categoria_otro, "otro");

    companion object {
        fun desdeCodigo(codigo: String?): CategoriaPrenda =
            entries.firstOrNull { it.codigo == codigo } ?: OTRO
    }
}

// Los colores principales con los que se clasifica una prenda
data class ColorPrenda(
    val codigo: String,
    val hex: String,
    @StringRes val etiqueta: Int,
)

val coloresDePrenda = listOf(
    ColorPrenda("negro", "#1F1F1F", R.string.color_negro),
    ColorPrenda("blanco", "#F7F7F7", R.string.color_blanco),
    ColorPrenda("gris", "#9E9E9E", R.string.color_gris),
    ColorPrenda("beige", "#D9C3A5", R.string.color_beige),
    ColorPrenda("cafe", "#7A5230", R.string.color_cafe),
    ColorPrenda("azul", "#3D5A80", R.string.color_azul),
    ColorPrenda("celeste", "#98C1D9", R.string.color_celeste),
    ColorPrenda("verde", "#5C7F5A", R.string.color_verde),
    ColorPrenda("rojo", "#C0392B", R.string.color_rojo),
    ColorPrenda("rosado", "#E8A0B4", R.string.color_rosado),
    ColorPrenda("amarillo", "#E9C46A", R.string.color_amarillo),
    ColorPrenda("morado", "#7B5EA7", R.string.color_morado),
    ColorPrenda("estampado", "#BDB2A7", R.string.color_estampado),
)

fun colorDePrenda(codigo: String): ColorPrenda? = coloresDePrenda.firstOrNull { it.codigo == codigo }

data class PrendaUi(
    val id: String = "",
    val nombre: String = "",
    val categoria: CategoriaPrenda = CategoriaPrenda.OTRO,
    val color: String = "",
    val fotoUrl: String? = null,
    val favorita: Boolean = false,
    val fecha: Long = 0,
)

enum class OrigenOutfit(val codigo: String) {
    ASESOR("asesor"),
    MANUAL("manual");

    companion object {
        fun desdeCodigo(codigo: String?): OrigenOutfit =
            entries.firstOrNull { it.codigo == codigo } ?: MANUAL
    }
}

// Las ocasiones con las que se etiqueta un outfit
enum class OcasionOutfit(val codigo: String, @StringRes val etiqueta: Int) {
    UNIVERSIDAD("universidad", R.string.ocasion_universidad),
    TRABAJO("trabajo", R.string.ocasion_trabajo),
    SALIDA("salida", R.string.ocasion_salida),
    OTRA("otra", R.string.ocasion_otra);

    companion object {
        fun desdeCodigo(codigo: String?): OcasionOutfit =
            entries.firstOrNull { it.codigo == codigo } ?: OTRA
    }
}

// Un outfit guardado: o lo armo el usuario con sus prendas, o lo propuso el asesor como texto
data class OutfitUi(
    val id: String = "",
    val nombre: String = "",
    val ocasion: OcasionOutfit = OcasionOutfit.OTRA,
    val prendasIds: List<String> = emptyList(),
    // Descripcion de las prendas cuando el outfit viene del asesor
    val prendasTexto: List<String> = emptyList(),
    val porQue: String = "",
    val origen: OrigenOutfit = OrigenOutfit.MANUAL,
    val favorito: Boolean = false,
    val fecha: Long = 0,
)

// Un elemento del historial: resultados del cuestionario, outfits y conversaciones, por fecha
sealed class ElementoHistorialUi(open val fecha: Long) {
    data class Resultado(val resultado: ResultadoUi) : ElementoHistorialUi(resultado.fecha)
    data class Outfit(val outfit: OutfitUi) : ElementoHistorialUi(outfit.fecha)
    data class Conversacion(val conversacion: ConversacionUi) : ElementoHistorialUi(conversacion.fecha)
}
