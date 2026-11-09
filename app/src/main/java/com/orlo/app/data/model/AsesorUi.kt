package com.orlo.app.data.model

enum class RolMensaje { USUARIO, ASESOR }

// Los botones rapidos debajo de una respuesta del asesor. El codigo es el que entiende el backend
enum class AccionRapida(val codigo: String) {
    MAS_CASUAL("MORE_CASUAL"),
    MAS_FORMAL("MORE_FORMAL"),
    OTRA_OPCION("ANOTHER"),
}

// Una opcion de outfit que propone el asesor, con el porque en pocas lineas
data class OutfitPropuestoUi(
    val titulo: String = "",
    val prendas: List<String> = emptyList(),
    val porQue: String = "",
)

data class MensajeChatUi(
    val id: String = "",
    val rol: RolMensaje = RolMensaje.USUARIO,
    val texto: String = "",
    val outfits: List<OutfitPropuestoUi> = emptyList(),
    val preguntaSeguimiento: String? = null,
    val fecha: Long = 0,
)

data class ConversacionUi(
    val id: String = "",
    val titulo: String = "",
    val fecha: Long = 0,
)

// Lo que devuelve el asesor despues de una pregunta
data class RespuestaAsesorUi(
    val mensaje: MensajeChatUi = MensajeChatUi(),
    val mensajesRestantes: Int = 0,
)
