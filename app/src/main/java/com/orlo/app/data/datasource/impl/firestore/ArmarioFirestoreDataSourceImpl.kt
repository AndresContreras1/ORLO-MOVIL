package com.orlo.app.data.datasource.impl.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.orlo.app.data.datasource.ArmarioRemoteDataSource
import com.orlo.app.data.dtos.OutfitFirestoreDto
import com.orlo.app.data.dtos.PrendaFirestoreDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Prendas y outfits son subcolecciones del usuario: nadie mas puede leerlas (reglas de seguridad)
class ArmarioFirestoreDataSourceImpl @Inject constructor(
    private val db: FirebaseFirestore
) : ArmarioRemoteDataSource {

    private fun prendas(userId: String) = db.collection(USERS).document(userId).collection(GARMENTS)

    private fun outfits(userId: String) = db.collection(USERS).document(userId).collection(OUTFITS)

    override suspend fun getPrendas(userId: String): List<PrendaFirestoreDto> {
        val respuesta = prendas(userId).orderBy("createdAt", Query.Direction.DESCENDING).get().await()
        return respuesta.documents.mapNotNull { doc ->
            doc.toObject(PrendaFirestoreDto::class.java)?.copy(id = doc.id)
        }
    }

    override suspend fun getPrenda(userId: String, prendaId: String): PrendaFirestoreDto? {
        val respuesta = prendas(userId).document(prendaId).get().await()
        return respuesta.toObject(PrendaFirestoreDto::class.java)?.copy(id = respuesta.id)
    }

    override fun nuevoIdDePrenda(userId: String): String = prendas(userId).document().id

    override suspend fun guardarPrenda(userId: String, prenda: PrendaFirestoreDto): String {
        val ref = if (prenda.id.isBlank()) prendas(userId).document() else prendas(userId).document(prenda.id)
        ref.set(prenda.copy(id = ref.id)).await()
        return ref.id
    }

    override suspend fun borrarPrenda(userId: String, prendaId: String) {
        prendas(userId).document(prendaId).delete().await()
    }

    override suspend fun getOutfits(userId: String): List<OutfitFirestoreDto> {
        val respuesta = outfits(userId).orderBy("createdAt", Query.Direction.DESCENDING).get().await()
        return respuesta.documents.mapNotNull { doc ->
            doc.toObject(OutfitFirestoreDto::class.java)?.copy(id = doc.id)
        }
    }

    override suspend fun getOutfit(userId: String, outfitId: String): OutfitFirestoreDto? {
        val respuesta = outfits(userId).document(outfitId).get().await()
        return respuesta.toObject(OutfitFirestoreDto::class.java)?.copy(id = respuesta.id)
    }

    override suspend fun guardarOutfit(userId: String, outfit: OutfitFirestoreDto): String {
        val ref = if (outfit.id.isBlank()) outfits(userId).document() else outfits(userId).document(outfit.id)
        ref.set(outfit.copy(id = ref.id)).await()
        return ref.id
    }

    override suspend fun marcarFavorito(userId: String, outfitId: String, favorito: Boolean) {
        outfits(userId).document(outfitId).update(mapOf("favorite" to favorito)).await()
    }

    override suspend fun borrarOutfit(userId: String, outfitId: String) {
        outfits(userId).document(outfitId).delete().await()
    }
}
