package com.orlo.app.data.dtos

import com.orlo.app.data.model.CategoriaPrenda
import com.orlo.app.data.model.GuardadoUi
import com.orlo.app.data.model.IdeaCombinarUi
import com.orlo.app.data.model.ItemExploraUi
import com.orlo.app.data.model.OutfitDelDiaUi
import com.orlo.app.data.model.SugerenciaArmarioUi
import com.orlo.app.data.model.TiendaUi

// Lo que responde el backend en Explora. Los ids llegan como numero y Gson los lee como texto
data class ItemExploraDto(
    val id: String? = null,
    val name: String? = null,
    val imageUrl: String? = null,
    val imageCredit: String? = null,
    val category: String? = null,
    val color: String? = null,
    val styles: List<String>? = null,
    val whyItFits: String? = null,
    val trend: Boolean? = null,
    val accessory: Boolean? = null,
    // Solo en el detalle
    val combineIdeas: List<IdeaCombinarDto>? = null,
)

data class IdeaCombinarDto(
    val context: String? = null,
    val items: List<String>? = null,
)

// Lo que la app sabe del armario para "Completa tu armario": solo categoria y color
data class CompletarArmarioDto(
    val primaryStyle: String?,
    val wardrobe: List<PrendaArmarioDto>,
)

data class PrendaArmarioDto(
    val category: String,
    val color: String,
)

data class SugerenciaArmarioDto(
    val category: String? = null,
    val title: String? = null,
    val reason: String? = null,
    val items: List<ItemExploraDto>? = null,
)

data class TiendaDto(
    val id: String? = null,
    val name: String? = null,
    val logoUrl: String? = null,
    val clothingTypes: List<String>? = null,
    val url: String? = null,
    val affiliate: Boolean? = null,
)

data class OutfitDelDiaPedidoDto(
    val profile: PerfilAsesorDto,
    val wardrobe: List<PrendaAsesorDto>,
)

data class OutfitDelDiaDto(
    val title: String? = null,
    val items: List<String>? = null,
    val why: String? = null,
)

// Firestore: users/{uid}/saved/{itemId}. El id del documento es el id de la prenda del catalogo
data class GuardadoFirestoreDto(
    val itemId: Long = 0,
    val name: String = "",
    val imageUrl: String = "",
    val category: String = "",
    val savedAt: Long = 0,
)

// Firestore: users/{uid}/dailyOutfits/{AAAA-MM-DD}
data class OutfitDelDiaFirestoreDto(
    val title: String = "",
    val items: List<String> = emptyList(),
    val why: String = "",
    val createdAt: Long = 0,
)

fun ItemExploraDto.toItemExploraUi(): ItemExploraUi = ItemExploraUi(
    id = id.orEmpty(),
    nombre = name.orEmpty(),
    imagenUrl = imageUrl,
    credito = imageCredit.orEmpty(),
    categoria = CategoriaPrenda.desdeCodigo(category),
    color = color.orEmpty(),
    estilos = styles.orEmpty(),
    porQueTeQueda = whyItFits.orEmpty(),
    tendencia = trend ?: false,
    accesorio = accessory ?: false,
    ideas = combineIdeas.orEmpty()
        .filter { !it.items.isNullOrEmpty() }
        .map { IdeaCombinarUi(contexto = it.context.orEmpty(), prendas = it.items.orEmpty()) },
)

fun SugerenciaArmarioDto.toSugerenciaArmarioUi(): SugerenciaArmarioUi = SugerenciaArmarioUi(
    categoria = CategoriaPrenda.desdeCodigo(category),
    titulo = title.orEmpty(),
    razon = reason.orEmpty(),
    items = items.orEmpty().map { it.toItemExploraUi() },
)

fun TiendaDto.toTiendaUi(): TiendaUi = TiendaUi(
    id = id.orEmpty(),
    nombre = name.orEmpty(),
    logoUrl = logoUrl,
    tipos = clothingTypes.orEmpty(),
    url = url.orEmpty(),
    afiliada = affiliate ?: false,
)

fun OutfitDelDiaDto.toOutfitDelDiaUi(fecha: Long): OutfitDelDiaUi = OutfitDelDiaUi(
    titulo = title.orEmpty(),
    prendas = items.orEmpty(),
    porQue = why.orEmpty(),
    fecha = fecha,
)

fun OutfitDelDiaUi.toOutfitDelDiaFirestoreDto(): OutfitDelDiaFirestoreDto = OutfitDelDiaFirestoreDto(
    title = titulo,
    items = prendas,
    why = porQue,
    createdAt = fecha,
)

fun OutfitDelDiaFirestoreDto.toOutfitDelDiaUi(): OutfitDelDiaUi = OutfitDelDiaUi(
    titulo = title,
    prendas = items,
    porQue = why,
    fecha = createdAt,
)

fun GuardadoFirestoreDto.toGuardadoUi(): GuardadoUi = GuardadoUi(
    itemId = itemId.toString(),
    nombre = name,
    imagenUrl = imageUrl.ifBlank { null },
    categoria = CategoriaPrenda.desdeCodigo(category),
    fecha = savedAt,
)

fun ItemExploraUi.toGuardadoFirestoreDto(fecha: Long): GuardadoFirestoreDto = GuardadoFirestoreDto(
    itemId = id.toLongOrNull() ?: 0,
    name = nombre,
    imageUrl = imagenUrl.orEmpty(),
    category = categoria.codigo,
    savedAt = fecha,
)
