package com.orlo.app.data.repository

import android.util.Log
import com.orlo.app.data.datasource.TiendaRemoteDataSource
import com.orlo.app.data.dtos.toTiendaUi
import com.orlo.app.data.model.CategoriaPrenda
import com.orlo.app.data.model.TiendaUi
import javax.inject.Inject

class TiendaRepository @Inject constructor(
    private val tiendaRemoteDataSource: TiendaRemoteDataSource,
) {

    // Las tiendas que venden esa categoria; si ninguna, el backend responde todas
    suspend fun getTiendas(categoria: CategoriaPrenda?): Result<List<TiendaUi>> {
        return try {
            Result.success(tiendaRemoteDataSource.getTiendas(categoria?.codigo).map { it.toTiendaUi() })
        } catch (e: Exception) {
            Log.d("TiendaRepository", "getTiendas: ${e.message}")
            Result.failure(errorDe(e))
        }
    }
}
