package com.orlo.app.data.datasource.services

import com.orlo.app.data.dtos.CuentaDto
import com.orlo.app.data.dtos.CuestionarioDto
import com.orlo.app.data.dtos.EstiloDto
import com.orlo.app.data.dtos.LeccionDto
import com.orlo.app.data.dtos.LeccionResumenDto
import com.orlo.app.data.dtos.MensajeAsesorDto
import com.orlo.app.data.dtos.PuntuarCuestionarioDto
import com.orlo.app.data.dtos.ResultadoCuestionarioDto
import com.orlo.app.data.dtos.RespuestaAsesorDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// Un servicio por recurso del backend. Retornan el DTO directo: si el backend responde con error,
// Retrofit lanza HttpException y el repositorio la traduce

interface EstiloRetrofitService {
    @GET("api/v1/styles")
    suspend fun getEstilos(): List<EstiloDto>
}

interface CuestionarioRetrofitService {
    @GET("api/v1/quiz/active")
    suspend fun getCuestionario(): CuestionarioDto

    @POST("api/v1/quiz/score")
    suspend fun puntuar(@Body respuestas: PuntuarCuestionarioDto): ResultadoCuestionarioDto
}

interface LeccionRetrofitService {
    @GET("api/v1/lessons")
    suspend fun getLecciones(): List<LeccionResumenDto>

    @GET("api/v1/lessons/{id}")
    suspend fun getLeccion(@Path("id") id: Long): LeccionDto
}

interface AsesorRetrofitService {
    @POST("api/v1/advisor/messages")
    suspend fun enviarMensaje(@Body mensaje: MensajeAsesorDto): RespuestaAsesorDto
}

interface CuentaRetrofitService {
    @GET("api/v1/me")
    suspend fun getCuenta(): CuentaDto

    // El backend responde 204 sin cuerpo: por eso retorna Unit
    @DELETE("api/v1/me")
    suspend fun borrarCuenta()
}
