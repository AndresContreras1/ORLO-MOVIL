package com.orlo.app.data.datasource.impl.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.orlo.app.data.datasource.OutfitDelDiaRemoteDataSource
import com.orlo.app.data.dtos.OutfitDelDiaFirestoreDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class OutfitDelDiaFirestoreDataSourceImpl @Inject constructor(
    private val db: FirebaseFirestore
) : OutfitDelDiaRemoteDataSource {

    private fun outfitsDelDia(userId: String) = db.collection(USERS).document(userId).collection(DAILY_OUTFITS)

    override suspend fun getOutfitDelDia(userId: String, fecha: String): OutfitDelDiaFirestoreDto? =
        outfitsDelDia(userId).document(fecha).get().await().toObject(OutfitDelDiaFirestoreDto::class.java)

    // Pedir otra opcion el mismo dia reemplaza la anterior
    override suspend fun guardarOutfitDelDia(userId: String, fecha: String, outfit: OutfitDelDiaFirestoreDto): Unit {
        outfitsDelDia(userId).document(fecha).set(outfit).await()
    }
}
