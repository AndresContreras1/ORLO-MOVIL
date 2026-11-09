package com.orlo.app.data.datasource

import com.orlo.app.data.dtos.ConversacionFirestoreDto
import com.orlo.app.data.dtos.GuardarMensajeDto
import com.orlo.app.data.dtos.MensajeFirestoreDto
import kotlinx.coroutines.flow.Flow

// Contrato de las conversaciones con el asesor: se guardan para volver a ellas y se pueden borrar
interface ConversacionRemoteDataSource {

    suspend fun getConversaciones(userId: String): List<ConversacionFirestoreDto>
    fun observarConversaciones(userId: String): Flow<List<ConversacionFirestoreDto>>

    suspend fun getMensajes(userId: String, conversacionId: String): List<MensajeFirestoreDto>

    // La pregunta y la respuesta se guardan juntas en un batch, y si la conversacion no existe se
    // crea en el mismo batch con ese titulo. Retorna el id de la conversacion
    suspend fun guardarIntercambio(
        userId: String,
        conversacionId: String?,
        titulo: String,
        pregunta: GuardarMensajeDto,
        respuesta: GuardarMensajeDto,
    ): String

    suspend fun borrarConversacion(userId: String, conversacionId: String): Unit
}
