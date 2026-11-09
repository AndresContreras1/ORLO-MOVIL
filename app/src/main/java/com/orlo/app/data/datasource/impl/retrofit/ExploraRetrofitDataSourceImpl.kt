package com.orlo.app.data.datasource.impl.retrofit

import com.orlo.app.data.datasource.ExploraRemoteDataSource
import com.orlo.app.data.datasource.services.ExploraRetrofitService
import com.orlo.app.data.dtos.CompletarArmarioDto
import com.orlo.app.data.dtos.ItemExploraDto
import com.orlo.app.data.dtos.SugerenciaArmarioDto
import javax.inject.Inject

class ExploraRetrofitDataSourceImpl @Inject constructor(
    private val service: ExploraRetrofitService,
) : ExploraRemoteDataSource {

    override suspend fun getItems(seccion: String, estilo: String?): List<ItemExploraDto> =
        service.getItems(seccion, estilo)

    override suspend fun getItem(id: String): ItemExploraDto = service.getItem(id)

    override suspend fun completarArmario(armario: CompletarArmarioDto): List<SugerenciaArmarioDto> =
        service.completarArmario(armario)
}
