package com.orlo.app.data.datasource.impl.retrofit

import com.orlo.app.data.datasource.TiendaRemoteDataSource
import com.orlo.app.data.datasource.services.TiendaRetrofitService
import com.orlo.app.data.dtos.TiendaDto
import javax.inject.Inject

class TiendaRetrofitDataSourceImpl @Inject constructor(
    private val service: TiendaRetrofitService,
) : TiendaRemoteDataSource {

    override suspend fun getTiendas(categoria: String?): List<TiendaDto> = service.getTiendas(categoria)
}
