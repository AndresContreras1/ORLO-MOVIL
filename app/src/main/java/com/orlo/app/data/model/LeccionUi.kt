package com.orlo.app.data.model

// Los tres estados de una capsula. El codigo es el que se guarda en Firestore
enum class EstadoLeccion(val codigo: String) {
    SIN_EMPEZAR("sin_empezar"),
    EN_CURSO("en_curso"),
    COMPLETADA("completada");

    companion object {
        fun desdeCodigo(codigo: String?): EstadoLeccion =
            entries.firstOrNull { it.codigo == codigo } ?: SIN_EMPEZAR
    }
}

// Una capsula en la lista de Aprende
data class LeccionResumenUi(
    val id: String = "",
    val titulo: String = "",
    val resumen: String = "",
    val orden: Int = 0,
    val muestraGratis: Boolean = false,
    val portadaUrl: String? = null,
    val minutos: Int = 0,
    val estado: EstadoLeccion = EstadoLeccion.SIN_EMPEZAR,
)

// El contenido completo de una capsula: idea principal, ejemplos, consejo y repaso opcional
data class LeccionUi(
    val id: String = "",
    val titulo: String = "",
    val ideaPrincipal: String = "",
    val ejemplos: List<EjemploUi> = emptyList(),
    val consejo: String = "",
    val repaso: RepasoUi? = null,
    val muestraGratis: Boolean = false,
    val usaColorimetria: Boolean = false,
)

data class EjemploUi(
    val imagenUrl: String? = null,
    val texto: String = "",
)

data class RepasoUi(
    val pregunta: String = "",
    val opciones: List<String> = emptyList(),
    val indiceCorrecto: Int = 0,
)
