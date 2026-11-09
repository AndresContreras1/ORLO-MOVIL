package com.orlo.app.data.datasource

import com.orlo.app.data.dtos.ConversacionFirestoreDto
import com.orlo.app.data.dtos.MensajeFirestoreDto

// Contrato de las conversaciones con el asesor: se guardan para volver a ellas y se pueden borrar
interface ConversacionRemoteDataSource {

    suspend fun crearConversacion(userId: String, titulo: String, fecha: Long): String

    suspend fun getConversaciones(userId: String): List<ConversacionFirestoreDto>

    suspend fun getMensajes(userId: String, conversacionId: String): List<MensajeFirestoreDto>

    // Guarda el mensaje y actualiza la fecha de la conversacion en un solo batch
    suspend fun agregarMensaje(userId: String, conversacionId: String, mensaje: MensajeFirestoreDto)

    suspend fun borrarConversacion(userId: String, conversacionId: String)
}
