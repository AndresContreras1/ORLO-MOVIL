package com.orlo.app.data.datasource.impl.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.orlo.app.data.datasource.ArmarioRemoteDataSource
import com.orlo.app.data.dtos.GuardarOutfitDto
import com.orlo.app.data.dtos.GuardarPrendaDto
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

    override suspend fun guardarPrenda(userId: String, prendaId: String, prenda: GuardarPrendaDto): Unit {
        prendas(userId).document(prendaId).set(prenda).await()
    }

    override suspend fun marcarPrendaFavorita(userId: String, prendaId: String, favorita: Boolean): Unit {
        prendas(userId).document(prendaId).update(mapOf("favorite" to favorita)).await()
    }

    override suspend fun borrarPrenda(userId: String, prendaId: String): Unit {
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

    override suspend fun crearOutfit(userId: String, outfit: GuardarOutfitDto): String {
        val ref = outfits(userId).document()
        ref.set(outfit).await()
        return ref.id
    }

    override suspend fun actualizarOutfit(userId: String, outfitId: String, outfit: GuardarOutfitDto): Unit {
        outfits(userId).document(outfitId).set(outfit).await()
    }

    override suspend fun marcarOutfitFavorito(userId: String, outfitId: String, favorito: Boolean): Unit {
        outfits(userId).document(outfitId).update(mapOf("favorite" to favorito)).await()
    }

    override suspend fun borrarOutfit(userId: String, outfitId: String): Unit {
        outfits(userId).document(outfitId).delete().await()
    }
}
