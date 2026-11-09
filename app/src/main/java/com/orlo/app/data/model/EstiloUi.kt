package com.orlo.app.data.model

// Un estilo base (casual, urbano, clasico...) como lo muestra la app. Ningun estilo es mejor que
// otro: todos tienen los mismos campos
data class EstiloUi(
    val id: Long = 0,
    val code: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    // Colores en hexadecimal (#RRGGBB)
    val paleta: List<String> = emptyList(),
    val galeria: List<String> = emptyList(),
)

// El diagnostico: estilo principal y secundario, por ejemplo "Urbano creativo · elegante"
data class ResultadoUi(
    val id: String = "",
    val principal: EstiloUi = EstiloUi(),
    val secundario: EstiloUi = EstiloUi(),
    val fecha: Long = 0,
) {
    val nombreCompleto: String
        get() = if (secundario.nombre.isBlank()) principal.nombre
        else "${principal.nombre} · ${secundario.nombre.lowercase()}"
}
