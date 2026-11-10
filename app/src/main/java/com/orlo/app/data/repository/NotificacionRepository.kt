package com.orlo.app.data.repository

import android.util.Log
import com.orlo.app.data.datasource.MensajeriaRemoteDataSource
import javax.inject.Inject

// El tema al que el backend avisa cada leccion nueva que se publica en el panel
const val TEMA_LECCIONES = "lecciones"

class NotificacionRepository @Inject constructor(
    private val mensajeriaRemoteDataSource: MensajeriaRemoteDataSource,
) {

    suspend fun suscribirALecciones(): Result<Unit> {
        return try {
            mensajeriaRemoteDataSource.suscribirse(TEMA_LECCIONES)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.d("NotificacionRepository", "suscribirALecciones: ${e.message}")
            Result.failure(errorDe(e))
        }
    }
}
