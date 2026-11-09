package com.orlo.app.data.datasource.impl.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.orlo.app.data.datasource.ConversacionRemoteDataSource
import com.orlo.app.data.dtos.ConversacionFirestoreDto
import com.orlo.app.data.dtos.MensajeFirestoreDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ConversacionFirestoreDataSourceImpl @Inject constructor(
    private val db: FirebaseFirestore
) : ConversacionRemoteDataSource {

    private fun conversaciones(userId: String) =
        db.collection(USERS).document(userId).collection(CONVERSATIONS)

    override suspend fun crearConversacion(userId: String, titulo: String, fecha: Long): String {
        val ref = conversaciones(userId).document()
        ref.set(ConversacionFirestoreDto(id = ref.id, title = titulo, updatedAt = fecha)).await()
        return ref.id
    }

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

    override suspend fun agregarMensaje(
        userId: String,
        conversacionId: String,
        mensaje: MensajeFirestoreDto
    ) {
        val conversacionRef = conversaciones(userId).document(conversacionId)
        val mensajeRef = conversacionRef.collection(MESSAGES).document()

        db.batch()
            .set(mensajeRef, mensaje.copy(id = mensajeRef.id))
            .update(conversacionRef, mapOf("updatedAt" to mensaje.createdAt))
            .commit()
            .await()
    }

    // Primero los mensajes (la subcoleccion no se borra sola) y despues la conversacion
    override suspend fun borrarConversacion(userId: String, conversacionId: String) {
        val conversacionRef = conversaciones(userId).document(conversacionId)
        val mensajes = conversacionRef.collection(MESSAGES).get().await()
        mensajes.documents.chunked(499).forEach { parte ->
            val batch = db.batch()
            parte.forEach { batch.delete(it.reference) }
            batch.commit().await()
        }
        conversacionRef.delete().await()
    }
}
