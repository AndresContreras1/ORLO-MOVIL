package com.orlo.app.data.datasource

import com.orlo.app.data.dtos.GuardarOutfitDto
import com.orlo.app.data.dtos.GuardarPrendaDto
import com.orlo.app.data.dtos.OutfitFirestoreDto
import com.orlo.app.data.dtos.PrendaFirestoreDto

// Contrato del armario digital: prendas y outfits de un usuario. Crear o actualizar lo decide el
// repositorio; el data source solo ejecuta la consulta
interface ArmarioRemoteDataSource {

    suspend fun getPrendas(userId: String): List<PrendaFirestoreDto>

    suspend fun getPrenda(userId: String, prendaId: String): PrendaFirestoreDto?

    // Genera el id antes de subir la foto, para que la ruta de Storage lleve el id de la prenda
    fun nuevoIdDePrenda(userId: String): String

    // Escribe la prenda con ese id (la crea o la reemplaza)
    suspend fun guardarPrenda(userId: String, prendaId: String, prenda: GuardarPrendaDto): Unit

    suspend fun marcarPrendaFavorita(userId: String, prendaId: String, favorita: Boolean): Unit

    suspend fun borrarPrenda(userId: String, prendaId: String): Unit

    suspend fun getOutfits(userId: String): List<OutfitFirestoreDto>

    suspend fun getOutfit(userId: String, outfitId: String): OutfitFirestoreDto?

    // Retorna el id del outfit nuevo
    suspend fun crearOutfit(userId: String, outfit: GuardarOutfitDto): String

    suspend fun actualizarOutfit(userId: String, outfitId: String, outfit: GuardarOutfitDto): Unit

    suspend fun marcarOutfitFavorito(userId: String, outfitId: String, favorito: Boolean): Unit

    suspend fun borrarOutfit(userId: String, outfitId: String): Unit
}
