package com.orlo.app.data.datasource.services

import com.orlo.app.data.dtos.CompletarArmarioDto
import com.orlo.app.data.dtos.ItemExploraDto
import com.orlo.app.data.dtos.SugerenciaArmarioDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ExploraRetrofitService {

    @GET("api/v1/explore/items")
    suspend fun getItems(@Query("section") seccion: String, @Query("style") estilo: String?): List<ItemExploraDto>

    @GET("api/v1/explore/items/{id}")
    suspend fun getItem(@Path("id") id: String): ItemExploraDto

    @POST("api/v1/explore/complete-wardrobe")
    suspend fun completarArmario(@Body armario: CompletarArmarioDto): List<SugerenciaArmarioDto>
}
