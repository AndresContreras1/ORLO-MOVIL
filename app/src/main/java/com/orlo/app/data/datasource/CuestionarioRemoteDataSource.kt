package com.orlo.app.data.datasource

import com.orlo.app.data.dtos.CuestionarioDto
import com.orlo.app.data.dtos.EstiloDto
import com.orlo.app.data.dtos.PuntuarCuestionarioDto
import com.orlo.app.data.dtos.ResultadoCuestionarioDto

// Contrato de los estilos y el cuestionario que editan los fundadores desde el panel
interface CuestionarioRemoteDataSource {

    suspend fun getEstilos(): List<EstiloDto>

    suspend fun getCuestionario(): CuestionarioDto

    suspend fun puntuarCuestionario(respuestas: PuntuarCuestionarioDto): ResultadoCuestionarioDto
}
