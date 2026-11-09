package com.orlo.app.data.datasource

import com.orlo.app.data.dtos.GuardadoFirestoreDto
import kotlinx.coroutines.flow.Flow

// Contrato de las prendas de Explora que el usuario guarda para despues
interface GuardadoRemoteDataSource {
    fun observarGuardados(userId: String): Flow<List<GuardadoFirestoreDto>>
    suspend fun guardar(userId: String, itemId: String, guardado: GuardadoFirestoreDto): Unit
    suspend fun quitar(userId: String, itemId: String): Unit
}
