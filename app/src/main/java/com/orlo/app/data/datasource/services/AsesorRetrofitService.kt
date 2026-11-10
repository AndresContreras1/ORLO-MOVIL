package com.orlo.app.data.datasource.services

import com.orlo.app.data.dtos.MensajeAsesorDto
import com.orlo.app.data.dtos.OutfitDelDiaDto
import com.orlo.app.data.dtos.OutfitDelDiaPedidoDto
import com.orlo.app.data.dtos.RespuestaAsesorDto
import retrofit2.http.Body
import retrofit2.http.POST

// El asesor con IA
interface AsesorRetrofitService {

    @POST("api/v1/advisor/messages")
    suspend fun enviarMensaje(@Body mensaje: MensajeAsesorDto): RespuestaAsesorDto

    // No descuenta mensajes del chat: tiene su propio tope diario
    @POST("api/v1/advisor/daily-outfit")
    suspend fun outfitDelDia(@Body pedido: OutfitDelDiaPedidoDto): OutfitDelDiaDto
}
