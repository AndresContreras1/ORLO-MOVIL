package com.orlo.app.data.model

// El cuestionario activo, tal como lo configuran los fundadores desde el panel
data class CuestionarioUi(
    val version: Long = 0,
    val preguntas: List<PreguntaUi> = emptyList(),
)

data class PreguntaUi(
    val id: String = "",
    val texto: String = "",
    val ayuda: String = "",
    // true: se pueden elegir varias opciones (colores que usa, ocasiones...)
    val multiple: Boolean = false,
    val opciones: List<OpcionUi> = emptyList(),
)

data class OpcionUi(
    val id: String = "",
    val texto: String = "",
    val imagenUrl: String? = null,
)
