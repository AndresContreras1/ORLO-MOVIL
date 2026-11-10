package com.orlo.app.data.datasource.services

import com.orlo.app.data.dtos.CuestionarioDto
import com.orlo.app.data.dtos.EstiloDto
import com.orlo.app.data.dtos.PuntuarCuestionarioDto
import com.orlo.app.data.dtos.ResultadoCuestionarioDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// Estilos y cuestionario del backend. Retornan el DTO directo: si el backend responde con error,
// Retrofit lanza HttpException y el repositorio la traduce
interface CuestionarioRetrofitService {

    @GET("api/v1/styles")
    suspend fun getEstilos(): List<EstiloDto>

    @GET("api/v1/quiz/active")
    suspend fun getCuestionario(): CuestionarioDto

    @POST("api/v1/quiz/score")
    suspend fun puntuar(@Body respuestas: PuntuarCuestionarioDto): ResultadoCuestionarioDto
}
