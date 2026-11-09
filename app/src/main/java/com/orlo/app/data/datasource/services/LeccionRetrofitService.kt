package com.orlo.app.data.datasource.services

import com.orlo.app.data.dtos.LeccionDto
import com.orlo.app.data.dtos.LeccionResumenDto
import retrofit2.http.GET
import retrofit2.http.Path

// Las capsulas de Aprende
interface LeccionRetrofitService {

    @GET("api/v1/lessons")
    suspend fun getLecciones(): List<LeccionResumenDto>

    @GET("api/v1/lessons/{id}")
    suspend fun getLeccion(@Path("id") id: String): LeccionDto
}
