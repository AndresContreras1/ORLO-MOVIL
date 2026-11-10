package com.orlo.app.data.datasource.impl.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.orlo.app.data.datasource.ConversacionRemoteDataSource
import com.orlo.app.data.dtos.ConversacionFirestoreDto
import com.orlo.app.data.dtos.GuardarMensajeDto
import com.orlo.app.data.dtos.MensajeFirestoreDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ConversacionFirestoreDataSourceImpl @Inject constructor(
    private val db: FirebaseFirestore
) : ConversacionRemoteDataSource {

    private fun conversaciones(userId: String) =
        db.collection(USERS).document(userId).collection(CONVERSATIONS)

    override suspend fun getConversaciones(userId: String): List<ConversacionFirestoreDto> {
        val respuesta = conversaciones(userId)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .get()
            .await()
        return respuesta.documents.mapNotNull { doc ->
            doc.toObject(ConversacionFirestoreDto::class.java)?.copy(id = doc.id)
        }
    }

    override suspend fun getMensajes(userId: String, conversacionId: String): List<MensajeFirestoreDto> {
        val respuesta = conversaciones(userId).document(conversacionId).collection(MESSAGES)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .get()
            .await()
        return respuesta.documents.mapNotNull { doc ->
            doc.toObject(MensajeFirestoreDto::class.java)?.copy(id = doc.id)
        }
    }

    // O se guardan la conversacion y los dos mensajes, o nada: una pregunta nunca queda sin respuesta
    override suspend fun guardarIntercambio(
        userId: String,
        conversacionId: String?,
        titulo: String,
        pregunta: GuardarMensajeDto,
        respuesta: GuardarMensajeDto,
    ): String {
        val conversacionRef = if (conversacionId == null) {
            conversaciones(userId).document()
        } else {
            conversaciones(userId).document(conversacionId)
        }
        val mensajes = conversacionRef.collection(MESSAGES)
        val batch = db.batch()

        if (conversacionId == null) {
            batch.set(conversacionRef, mapOf("title" to titulo, "updatedAt" to respuesta.createdAt))
        } else {
            batch.update(conversacionRef, mapOf("updatedAt" to respuesta.createdAt))
        }
        batch.set(mensajes.document(), pregunta)
        batch.set(mensajes.document(), respuesta)
        batch.commit().await()

        return conversacionRef.id
    }

    // Primero los mensajes (la subcoleccion no se borra sola) y despues la conversacion
    override suspend fun borrarConversacion(userId: String, conversacionId: String): Unit {
        val conversacionRef = conversaciones(userId).document(conversacionId)
        val mensajes = conversacionRef.collection(MESSAGES).get().await()
        mensajes.documents.chunked(MAX_BATCH - 1).forEach { parte ->
            val batch = db.batch()
            parte.forEach { batch.delete(it.reference) }
            batch.commit().await()
        }
        conversacionRef.delete().await()
    }
}
