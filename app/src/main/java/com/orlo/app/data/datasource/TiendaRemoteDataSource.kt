package com.orlo.app.data.datasource

import com.orlo.app.data.dtos.TiendaDto

// Contrato de las tiendas sugeridas
interface TiendaRemoteDataSource {
    suspend fun getTiendas(categoria: String?): List<TiendaDto>
}
