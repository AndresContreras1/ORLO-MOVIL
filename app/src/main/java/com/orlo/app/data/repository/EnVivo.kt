package com.orlo.app.data.repository

import android.util.Log
import com.orlo.app.data.datasource.AuthRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

// Un flujo en vivo de Firestore como Result, igual que las consultas de una vez: cada cambio llega
// como exito y, si el listener falla, llega un fallo con el error ya traducido y el flujo termina
fun <T> Flow<T>.comoResultado(etiqueta: String): Flow<Result<T>> =
    map { Result.success(it) }
        .catch { e ->
            Log.d(etiqueta, e.message.toString())
            emit(Result.failure(errorDe(e)))
        }

// Escucha los datos del usuario con sesion. Sin sesion no hay a quien escuchar: llega un solo fallo
fun <T> AuthRemoteDataSource.enVivo(etiqueta: String, observar: (String) -> Flow<T>): Flow<Result<T>> {
    val uid = currentUser?.uid ?: return flowOf(Result.failure(SinSesionException()))
    return observar(uid).comoResultado(etiqueta)
}
