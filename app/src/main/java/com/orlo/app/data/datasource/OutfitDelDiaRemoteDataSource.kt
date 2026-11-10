package com.orlo.app.data.datasource

import com.orlo.app.data.dtos.OutfitDelDiaFirestoreDto

// Contrato del outfit del dia guardado: uno por fecha (AAAA-MM-DD). Retorna null si no hay
interface OutfitDelDiaRemoteDataSource {
    suspend fun getOutfitDelDia(userId: String, fecha: String): OutfitDelDiaFirestoreDto?
    suspend fun guardarOutfitDelDia(userId: String, fecha: String, outfit: OutfitDelDiaFirestoreDto): Unit
}
