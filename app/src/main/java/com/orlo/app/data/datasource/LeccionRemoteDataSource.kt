package com.orlo.app.data.datasource

import com.orlo.app.data.dtos.LeccionDto
import com.orlo.app.data.dtos.LeccionResumenDto

// Contrato de las capsulas de Aprende
interface LeccionRemoteDataSource {

    suspend fun getLecciones(): List<LeccionResumenDto>

    suspend fun getLeccion(id: String): LeccionDto
}
