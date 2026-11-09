package com.orlo.app.data.model

// Los datos del perfil. Solo el nombre es obligatorio: lo demas mejora las recomendaciones y la
// app funciona sin ellos. Los datos de apariencia (cuerpo y colores) se guardan solo si el usuario
// da su consentimiento
data class PerfilUi(
    val nombre: String = "",
    val rangoEdad: String = "",
    val tratoPrendas: String = "",
    val tipoCuerpo: String = "",
    val tonoPiel: String = "",
    val colorCabello: String = "",
    val colorOjos: String = "",
    val clima: String = "",
    val presupuesto: String = "",
    val ocasiones: List<String> = emptyList(),
    val consentimientoDatosSensibles: Boolean = false,
    val resultadoActual: ResultadoUi? = null,
) {
    val tieneColorimetria: Boolean
        get() = consentimientoDatosSensibles &&
            tonoPiel.isNotBlank() && colorCabello.isNotBlank() && colorOjos.isNotBlank()
}

// Estado de la cuenta en el backend: si es premium y cuantos mensajes del asesor le quedan hoy
data class CuentaUi(
    val uid: String = "",
    val premium: Boolean = false,
    val limiteDiario: Int = 0,
    val mensajesRestantes: Int = 0,
)
