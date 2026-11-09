package com.orlo.app.data.dtos

import com.orlo.app.data.model.CategoriaPrenda
import com.orlo.app.data.model.ConversacionUi
import com.orlo.app.data.model.MensajeChatUi
import com.orlo.app.data.model.OcasionOutfit
import com.orlo.app.data.model.OrigenOutfit
import com.orlo.app.data.model.OutfitPropuestoUi
import com.orlo.app.data.model.OutfitUi
import com.orlo.app.data.model.PrendaUi
import com.orlo.app.data.model.RolMensaje

data class PrendaFirestoreDto(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val color: String = "",
    val photoUrl: String? = null,
    val createdAt: Long = 0,
)

fun PrendaFirestoreDto.toPrendaUi(): PrendaUi = PrendaUi(
    id = id,
    nombre = name,
    categoria = CategoriaPrenda.desdeCodigo(category),
    color = color,
    fotoUrl = photoUrl,
    fecha = createdAt,
)

fun PrendaUi.toPrendaFirestoreDto(): PrendaFirestoreDto = PrendaFirestoreDto(
    id = id,
    name = nombre.trim(),
    category = categoria.codigo,
    color = color,
    photoUrl = fotoUrl,
    createdAt = fecha,
)

data class OutfitFirestoreDto(
    val id: String = "",
    val name: String = "",
    val occasion: String = "",
    val garmentIds: List<String> = emptyList(),
    val itemsText: List<String> = emptyList(),
    val why: String = "",
    val origin: String = "",
    val favorite: Boolean = false,
    val createdAt: Long = 0,
)

fun OutfitFirestoreDto.toOutfitUi(): OutfitUi = OutfitUi(
    id = id,
    nombre = name,
    ocasion = OcasionOutfit.desdeCodigo(occasion),
    prendasIds = garmentIds,
    prendasTexto = itemsText,
    porQue = why,
    origen = OrigenOutfit.desdeCodigo(origin),
    favorito = favorite,
    fecha = createdAt,
)

fun OutfitUi.toOutfitFirestoreDto(): OutfitFirestoreDto = OutfitFirestoreDto(
    id = id,
    name = nombre.trim(),
    occasion = ocasion.codigo,
    garmentIds = prendasIds,
    itemsText = prendasTexto,
    why = porQue,
    origin = origen.codigo,
    favorite = favorito,
    createdAt = fecha,
)

data class ConversacionFirestoreDto(
    val id: String = "",
    val title: String = "",
    val updatedAt: Long = 0,
)

fun ConversacionFirestoreDto.toConversacionUi(): ConversacionUi = ConversacionUi(
    id = id,
    titulo = title,
    fecha = updatedAt,
)

data class OutfitPropuestoFirestoreDto(
    val title: String = "",
    val items: List<String> = emptyList(),
    val why: String = "",
)

data class MensajeFirestoreDto(
    val id: String = "",
    // "user" o "assistant"
    val role: String = "",
    val text: String = "",
    val outfits: List<OutfitPropuestoFirestoreDto> = emptyList(),
    val followUpQuestion: String? = null,
    val createdAt: Long = 0,
)

fun MensajeFirestoreDto.toMensajeChatUi(): MensajeChatUi = MensajeChatUi(
    id = id,
    rol = if (role == "assistant") RolMensaje.ASESOR else RolMensaje.USUARIO,
    texto = text,
    outfits = outfits.map { OutfitPropuestoUi(titulo = it.title, prendas = it.items, porQue = it.why) },
    preguntaSeguimiento = followUpQuestion,
    fecha = createdAt,
)

fun MensajeChatUi.toMensajeFirestoreDto(): MensajeFirestoreDto = MensajeFirestoreDto(
    id = id,
    role = if (rol == RolMensaje.ASESOR) "assistant" else "user",
    text = texto,
    outfits = outfits.map { OutfitPropuestoFirestoreDto(title = it.titulo, items = it.prendas, why = it.porQue) },
    followUpQuestion = preguntaSeguimiento,
    createdAt = fecha,
)
