package com.orlo.app.data.dtos

import com.orlo.app.data.model.MensajeChatUi
import com.orlo.app.data.model.OpcionesDelPerfil
import com.orlo.app.data.model.OutfitPropuestoUi
import com.orlo.app.data.model.PerfilUi
import com.orlo.app.data.model.PrendaUi
import com.orlo.app.data.model.RespuestaAsesorUi
import com.orlo.app.data.model.RolMensaje

// Lo que el asesor necesita saber del usuario. No lleva correo, apellidos ni el uid: el backend
// sabe quien es por el token y al modelo de IA no se le envia lo que no hace falta
data class PerfilAsesorDto(
    val name: String,
    val ageRange: String? = null,
    val garmentPronoun: String? = null,
    val primaryStyle: String? = null,
    val secondaryStyle: String? = null,
    val skinTone: String? = null,
    val hairColor: String? = null,
    val eyeColor: String? = null,
    val climate: String? = null,
    val budget: String? = null,
    val occasions: List<String> = emptyList(),
)

data class PrendaAsesorDto(
    val name: String,
    val category: String,
    val color: String,
)

data class HistorialAsesorDto(
    // USER o ASSISTANT
    val role: String,
    val text: String,
)

data class MensajeAsesorDto(
    val message: String,
    // MORE_CASUAL, MORE_FORMAL, ANOTHER o null si es una pregunta escrita
    val quickAction: String? = null,
    val profile: PerfilAsesorDto,
    val wardrobe: List<PrendaAsesorDto> = emptyList(),
    val history: List<HistorialAsesorDto> = emptyList(),
)

data class OutfitAsesorDto(
    val title: String? = null,
    val items: List<String>? = null,
    val why: String? = null,
)

data class RespuestaAsesorDto(
    val reply: String? = null,
    val outfits: List<OutfitAsesorDto>? = null,
    val followUpQuestion: String? = null,
    val remainingMessages: Int? = null,
)

fun OutfitAsesorDto.toOutfitPropuestoUi(): OutfitPropuestoUi = OutfitPropuestoUi(
    titulo = title ?: "",
    prendas = items ?: emptyList(),
    porQue = why ?: "",
)

fun RespuestaAsesorDto.toRespuestaAsesorUi(fecha: Long): RespuestaAsesorUi = RespuestaAsesorUi(
    mensaje = MensajeChatUi(
        rol = RolMensaje.ASESOR,
        texto = reply ?: "",
        outfits = (outfits ?: emptyList()).map { it.toOutfitPropuestoUi() },
        preguntaSeguimiento = followUpQuestion?.takeIf { it.isNotBlank() },
        fecha = fecha,
    ),
    mensajesRestantes = remainingMessages ?: 0,
)

// Lo que el asesor sabe del usuario, en palabras: el primer nombre y las preferencias. Nada de
// correo, apellidos ni identificadores. Lo usan el chat y el outfit del dia
fun PerfilUi.toPerfilAsesorDto(): PerfilAsesorDto = PerfilAsesorDto(
    name = nombre.trim().split(" ").firstOrNull().orEmpty(),
    ageRange = OpcionesDelPerfil.paraAsesor(OpcionesDelPerfil.rangosDeEdad, rangoEdad),
    garmentPronoun = OpcionesDelPerfil.paraAsesor(OpcionesDelPerfil.tratoPrendas, tratoPrendas),
    primaryStyle = resultadoActual?.principal?.nombre,
    secondaryStyle = resultadoActual?.secundario?.nombre,
    skinTone = OpcionesDelPerfil.colorParaAsesor(OpcionesDelPerfil.tonosDePiel, tonoPiel),
    hairColor = OpcionesDelPerfil.colorParaAsesor(OpcionesDelPerfil.coloresDeCabello, colorCabello),
    eyeColor = OpcionesDelPerfil.colorParaAsesor(OpcionesDelPerfil.coloresDeOjos, colorOjos),
    climate = OpcionesDelPerfil.paraAsesor(OpcionesDelPerfil.climas, clima),
    budget = OpcionesDelPerfil.paraAsesor(OpcionesDelPerfil.presupuestos, presupuesto),
    occasions = ocasiones.mapNotNull { OpcionesDelPerfil.paraAsesor(OpcionesDelPerfil.ocasiones, it) },
)

fun PrendaUi.toPrendaAsesorDto(): PrendaAsesorDto = PrendaAsesorDto(
    name = nombre.ifBlank { categoria.paraAsesor },
    category = categoria.paraAsesor,
    color = color,
)
