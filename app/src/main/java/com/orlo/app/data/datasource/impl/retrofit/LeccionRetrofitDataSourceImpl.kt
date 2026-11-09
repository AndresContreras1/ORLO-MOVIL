package com.orlo.app.data.datasource.impl.retrofit

import com.orlo.app.data.datasource.LeccionRemoteDataSource
import com.orlo.app.data.datasource.services.LeccionRetrofitService
import com.orlo.app.data.dtos.LeccionDto
import com.orlo.app.data.dtos.LeccionResumenDto
import javax.inject.Inject

class LeccionRetrofitDataSourceImpl @Inject constructor(
    private val service: LeccionRetrofitService,
) : LeccionRemoteDataSource {

    override suspend fun getLecciones(): List<LeccionResumenDto> = service.getLecciones()

    override suspend fun getLeccion(id: String): LeccionDto = service.getLeccion(id)
}
