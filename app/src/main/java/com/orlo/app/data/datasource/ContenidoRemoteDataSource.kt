package com.orlo.app.data.datasource

import com.orlo.app.data.dtos.CuestionarioDto
import com.orlo.app.data.dtos.EstiloDto
import com.orlo.app.data.dtos.LeccionDto
import com.orlo.app.data.dtos.LeccionResumenDto
import com.orlo.app.data.dtos.PuntuarCuestionarioDto
import com.orlo.app.data.dtos.ResultadoCuestionarioDto

// Contrato del contenido que editan los fundadores desde el panel: estilos, cuestionario y lecciones
interface ContenidoRemoteDataSource {

    suspend fun getEstilos(): List<EstiloDto>

    suspend fun getCuestionario(): CuestionarioDto

    suspend fun puntuarCuestionario(respuestas: PuntuarCuestionarioDto): ResultadoCuestionarioDto

    suspend fun getLecciones(): List<LeccionResumenDto>

    suspend fun getLeccion(id: Long): LeccionDto
}
