package com.orlo.app.data.datasource.services

import com.orlo.app.data.dtos.MensajeAsesorDto
import com.orlo.app.data.dtos.RespuestaAsesorDto
import retrofit2.http.Body
import retrofit2.http.POST

// El asesor con IA
interface AsesorRetrofitService {

    @POST("api/v1/advisor/messages")
    suspend fun enviarMensaje(@Body mensaje: MensajeAsesorDto): RespuestaAsesorDto
}
