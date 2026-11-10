package com.orlo.app.data.datasource.impl.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.orlo.app.data.datasource.GuardadoRemoteDataSource
import com.orlo.app.data.dtos.GuardadoFirestoreDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GuardadoFirestoreDataSourceImpl @Inject constructor(
    private val db: FirebaseFirestore
) : GuardadoRemoteDataSource {

    private fun guardados(userId: String) = db.collection(USERS).document(userId).collection(SAVED)

    override fun observarGuardados(userId: String): Flow<List<GuardadoFirestoreDto>> = callbackFlow {
        val listener = guardados(userId)
            .orderBy("savedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.mapNotNull { it.toObject(GuardadoFirestoreDto::class.java) }.orEmpty())
            }
        awaitClose { listener.remove() }
    }

    // El id del documento es el de la prenda: guardar dos veces la misma no la duplica
    override suspend fun guardar(userId: String, itemId: String, guardado: GuardadoFirestoreDto): Unit {
        guardados(userId).document(itemId).set(guardado).await()
    }

    override suspend fun quitar(userId: String, itemId: String): Unit {
        guardados(userId).document(itemId).delete().await()
    }
}
