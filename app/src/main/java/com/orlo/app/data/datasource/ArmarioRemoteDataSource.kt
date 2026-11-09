package com.orlo.app.data.datasource

import com.orlo.app.data.dtos.OutfitFirestoreDto
import com.orlo.app.data.dtos.PrendaFirestoreDto

// Contrato del armario digital: prendas y outfits de un usuario
interface ArmarioRemoteDataSource {

    suspend fun getPrendas(userId: String): List<PrendaFirestoreDto>

    suspend fun getPrenda(userId: String, prendaId: String): PrendaFirestoreDto?

    // Sin id crea la prenda; con id la reemplaza. Retorna el id
    suspend fun guardarPrenda(userId: String, prenda: PrendaFirestoreDto): String

    // Genera el id antes de subir la foto, para que la ruta de Storage lleve el id de la prenda
    fun nuevoIdDePrenda(userId: String): String

    suspend fun borrarPrenda(userId: String, prendaId: String)

    suspend fun getOutfits(userId: String): List<OutfitFirestoreDto>

    suspend fun getOutfit(userId: String, outfitId: String): OutfitFirestoreDto?

    suspend fun guardarOutfit(userId: String, outfit: OutfitFirestoreDto): String

    suspend fun marcarFavorito(userId: String, outfitId: String, favorito: Boolean)

    suspend fun borrarOutfit(userId: String, outfitId: String)
}
