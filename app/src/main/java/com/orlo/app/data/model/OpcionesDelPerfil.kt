package com.orlo.app.data.model

import androidx.annotation.StringRes
import com.orlo.app.R

// Una opcion de las listas del perfil: el codigo es lo que se guarda, la etiqueta lo que se ve y
// paraAsesor el texto con el que se describe al usuario ante el asesor de IA
data class OpcionPerfil(
    val codigo: String,
    @StringRes val etiqueta: Int,
    val paraAsesor: String,
)

// Una opcion de color de las paletas de piel, cabello y ojos: se elige tocando el circulo
data class OpcionColor(
    val codigo: String,
    val hex: String,
    @StringRes val etiqueta: Int,
    val paraAsesor: String,
)

object OpcionesDelPerfil {

    val rangosDeEdad = listOf(
        OpcionPerfil("menos_18", R.string.edad_menos_18, "menos de 18 años"),
        OpcionPerfil("18_24", R.string.edad_18_24, "entre 18 y 24 años"),
        OpcionPerfil("25_34", R.string.edad_25_34, "entre 25 y 34 años"),
        OpcionPerfil("35_44", R.string.edad_35_44, "entre 35 y 44 años"),
        OpcionPerfil("45_59", R.string.edad_45_59, "entre 45 y 59 años"),
        OpcionPerfil("60_mas", R.string.edad_60_mas, "60 años o más"),
    )

    val tratoPrendas = listOf(
        OpcionPerfil("femenino", R.string.trato_femenino, "femenino"),
        OpcionPerfil("masculino", R.string.trato_masculino, "masculino"),
        OpcionPerfil("neutro", R.string.trato_neutro, "neutro"),
    )

    val tiposDeCuerpo = listOf(
        OpcionPerfil("rectangulo", R.string.cuerpo_rectangulo, "hombros y cadera de ancho parecido"),
        OpcionPerfil("triangulo", R.string.cuerpo_triangulo, "cadera más ancha que los hombros"),
        OpcionPerfil("triangulo_invertido", R.string.cuerpo_triangulo_invertido, "hombros más anchos que la cadera"),
        OpcionPerfil("reloj_de_arena", R.string.cuerpo_reloj_de_arena, "hombros y cadera parejos con cintura marcada"),
        OpcionPerfil("ovalo", R.string.cuerpo_ovalo, "volumen concentrado en el centro"),
        OpcionPerfil("prefiero_no_decir", R.string.prefiero_no_decir, ""),
    )

    val tonosDePiel = listOf(
        OpcionColor("piel_muy_clara", "#F6DCC8", R.string.piel_muy_clara, "muy clara"),
        OpcionColor("piel_clara", "#E8C1A0", R.string.piel_clara, "clara"),
        OpcionColor("piel_media", "#C99A72", R.string.piel_media, "media"),
        OpcionColor("piel_morena", "#9C6B47", R.string.piel_morena, "morena"),
        OpcionColor("piel_oscura", "#6B4429", R.string.piel_oscura, "oscura"),
        OpcionColor("piel_muy_oscura", "#3F2717", R.string.piel_muy_oscura, "muy oscura"),
    )

    val coloresDeCabello = listOf(
        OpcionColor("cabello_negro", "#1F1B18", R.string.cabello_negro, "negro"),
        OpcionColor("cabello_castano_oscuro", "#4A3022", R.string.cabello_castano_oscuro, "castaño oscuro"),
        OpcionColor("cabello_castano_claro", "#8A5A3B", R.string.cabello_castano_claro, "castaño claro"),
        OpcionColor("cabello_rubio", "#D9B26F", R.string.cabello_rubio, "rubio"),
        OpcionColor("cabello_rojizo", "#A9442B", R.string.cabello_rojizo, "rojizo"),
        OpcionColor("cabello_gris", "#B8B4AE", R.string.cabello_gris, "gris o canoso"),
    )

    val coloresDeOjos = listOf(
        OpcionColor("ojos_negros", "#231A15", R.string.ojos_negros, "negros"),
        OpcionColor("ojos_cafes", "#5E3B22", R.string.ojos_cafes, "cafés"),
        OpcionColor("ojos_miel", "#A6762E", R.string.ojos_miel, "miel"),
        OpcionColor("ojos_verdes", "#5F7D4E", R.string.ojos_verdes, "verdes"),
        OpcionColor("ojos_azules", "#4F7BA8", R.string.ojos_azules, "azules"),
        OpcionColor("ojos_grises", "#8A9099", R.string.ojos_grises, "grises"),
    )

    val climas = listOf(
        OpcionPerfil("calido", R.string.clima_calido, "cálido"),
        OpcionPerfil("templado", R.string.clima_templado, "templado"),
        OpcionPerfil("frio", R.string.clima_frio, "frío"),
        OpcionPerfil("variable", R.string.clima_variable, "variable"),
    )

    val presupuestos = listOf(
        OpcionPerfil("bajo", R.string.presupuesto_bajo, "bajo"),
        OpcionPerfil("medio", R.string.presupuesto_medio, "medio"),
        OpcionPerfil("alto", R.string.presupuesto_alto, "alto"),
        OpcionPerfil("prefiero_no_decir", R.string.prefiero_no_decir, ""),
    )

    val ocasiones = listOf(
        OpcionPerfil("estudio", R.string.ocasion_estudio, "estudio"),
        OpcionPerfil("trabajo", R.string.ocasion_trabajo, "trabajo"),
        OpcionPerfil("salidas", R.string.ocasion_salidas, "salidas"),
        OpcionPerfil("casa", R.string.ocasion_casa, "casa"),
        OpcionPerfil("deporte", R.string.ocasion_deporte, "deporte"),
        OpcionPerfil("eventos", R.string.ocasion_eventos, "eventos"),
    )

    // El texto para el asesor de un codigo guardado; null si no se eligio nada o no hay texto
    fun paraAsesor(opciones: List<OpcionPerfil>, codigo: String): String? =
        opciones.firstOrNull { it.codigo == codigo }?.paraAsesor?.takeIf { it.isNotBlank() }

    fun colorParaAsesor(opciones: List<OpcionColor>, codigo: String): String? =
        opciones.firstOrNull { it.codigo == codigo }?.paraAsesor?.takeIf { it.isNotBlank() }
}

// Reglas del registro
const val MIN_PASSWORD_LENGTH = 6
const val MAX_NOMBRE_LENGTH = 40
