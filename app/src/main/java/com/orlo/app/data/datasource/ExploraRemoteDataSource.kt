package com.orlo.app.data.datasource

import com.orlo.app.data.dtos.CompletarArmarioDto
import com.orlo.app.data.dtos.ItemExploraDto
import com.orlo.app.data.dtos.SugerenciaArmarioDto

// Contrato del catalogo de Explora que editan los fundadores desde el panel
interface ExploraRemoteDataSource {
    suspend fun getItems(seccion: String, estilo: String?): List<ItemExploraDto>
    suspend fun getItem(id: String): ItemExploraDto
    suspend fun completarArmario(armario: CompletarArmarioDto): List<SugerenciaArmarioDto>
}
