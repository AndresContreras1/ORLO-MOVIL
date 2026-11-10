package com.orlo.app.data.datasource.services

import com.orlo.app.data.dtos.TiendaDto
import retrofit2.http.GET
import retrofit2.http.Query

interface TiendaRetrofitService {

    @GET("api/v1/stores")
    suspend fun getTiendas(@Query("category") categoria: String?): List<TiendaDto>
}
