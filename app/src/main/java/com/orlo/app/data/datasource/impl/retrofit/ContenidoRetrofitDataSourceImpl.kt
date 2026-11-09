package com.orlo.app.data.datasource.impl.retrofit

import com.orlo.app.data.datasource.ContenidoRemoteDataSource
import com.orlo.app.data.datasource.services.CuestionarioRetrofitService
import com.orlo.app.data.datasource.services.EstiloRetrofitService
import com.orlo.app.data.datasource.services.LeccionRetrofitService
import com.orlo.app.data.dtos.CuestionarioDto
import com.orlo.app.data.dtos.EstiloDto
import com.orlo.app.data.dtos.LeccionDto
import com.orlo.app.data.dtos.LeccionResumenDto
import com.orlo.app.data.dtos.PuntuarCuestionarioDto
import com.orlo.app.data.dtos.ResultadoCuestionarioDto
import javax.inject.Inject

class ContenidoRetrofitDataSourceImpl @Inject constructor(
    private val estiloService: EstiloRetrofitService,
    private val cuestionarioService: CuestionarioRetrofitService,
    private val leccionService: LeccionRetrofitService,
) : ContenidoRemoteDataSource {

    override suspend fun getEstilos(): List<EstiloDto> = estiloService.getEstilos()

    override suspend fun getCuestionario(): CuestionarioDto = cuestionarioService.getCuestionario()

    override suspend fun puntuarCuestionario(respuestas: PuntuarCuestionarioDto): ResultadoCuestionarioDto =
        cuestionarioService.puntuar(respuestas)

    override suspend fun getLecciones(): List<LeccionResumenDto> = leccionService.getLecciones()

    override suspend fun getLeccion(id: Long): LeccionDto = leccionService.getLeccion(id)
}
