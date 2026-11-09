package com.orlo.app.data.datasource.impl.retrofit

import com.orlo.app.data.datasource.CuestionarioRemoteDataSource
import com.orlo.app.data.datasource.services.CuestionarioRetrofitService
import com.orlo.app.data.dtos.CuestionarioDto
import com.orlo.app.data.dtos.EstiloDto
import com.orlo.app.data.dtos.PuntuarCuestionarioDto
import com.orlo.app.data.dtos.ResultadoCuestionarioDto
import javax.inject.Inject

class CuestionarioRetrofitDataSourceImpl @Inject constructor(
    private val service: CuestionarioRetrofitService,
) : CuestionarioRemoteDataSource {

    override suspend fun getEstilos(): List<EstiloDto> = service.getEstilos()

    override suspend fun getCuestionario(): CuestionarioDto = service.getCuestionario()

    override suspend fun puntuarCuestionario(respuestas: PuntuarCuestionarioDto): ResultadoCuestionarioDto =
        service.puntuar(respuestas)
}
