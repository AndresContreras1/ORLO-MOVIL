package com.orlo.app.data.repository

import android.util.Log
import com.orlo.app.data.datasource.AuthRemoteDataSource
import com.orlo.app.data.datasource.ContenidoRemoteDataSource
import com.orlo.app.data.datasource.UsuarioRemoteDataSource
import com.orlo.app.data.dtos.ProgresoLeccionFirestoreDto
import com.orlo.app.data.dtos.toLeccionResumenUi
import com.orlo.app.data.dtos.toLeccionUi
import com.orlo.app.data.model.EstadoLeccion
import com.orlo.app.data.model.LeccionResumenUi
import com.orlo.app.data.model.LeccionUi
import javax.inject.Inject

// Une dos fuentes: las lecciones vienen del backend y el avance de cada una de Firestore
class LeccionRepository @Inject constructor(
    private val contenidoRemoteDataSource: ContenidoRemoteDataSource,
    private val usuarioRemoteDataSource: UsuarioRemoteDataSource,
    private val authRemoteDataSource: AuthRemoteDataSource,
) {

    suspend fun getLecciones(): Result<List<LeccionResumenUi>> {
        return try {
            val uid = authRemoteDataSource.currentUser?.uid ?: throw SinSesionException()
            val lecciones = contenidoRemoteDataSource.getLecciones()

            // Si falla la lectura del avance se muestran las lecciones igual, todas sin empezar
            val progreso = try {
                usuarioRemoteDataSource.getProgresoLecciones(uid)
                    .associate { it.lessonId to EstadoLeccion.desdeCodigo(it.status) }
            } catch (e: Exception) {
                Log.d("LeccionRepository", "getLecciones (avance): ${e.message}")
                emptyMap()
            }

            Result.success(
                lecciones
                    .map { it.toLeccionResumenUi(progreso[it.id ?: 0] ?: EstadoLeccion.SIN_EMPEZAR) }
                    .sortedBy { it.orden }
            )
        } catch (e: Exception) {
            Log.d("LeccionRepository", "getLecciones: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    suspend fun getLeccion(id: Long): Result<LeccionUi> {
        return try {
            Result.success(contenidoRemoteDataSource.getLeccion(id).toLeccionUi())
        } catch (e: Exception) {
            Log.d("LeccionRepository", "getLeccion: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    suspend fun getEstado(id: Long): Result<EstadoLeccion> {
        return try {
            val uid = authRemoteDataSource.currentUser?.uid ?: throw SinSesionException()
            val progreso = usuarioRemoteDataSource.getProgresoLecciones(uid).firstOrNull { it.lessonId == id }
            Result.success(EstadoLeccion.desdeCodigo(progreso?.status))
        } catch (e: Exception) {
            Log.d("LeccionRepository", "getEstado: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    suspend fun guardarEstado(id: Long, estado: EstadoLeccion, fecha: Long): Result<Unit> {
        return try {
            val uid = authRemoteDataSource.currentUser?.uid ?: throw SinSesionException()
            usuarioRemoteDataSource.guardarProgresoLeccion(
                uid,
                ProgresoLeccionFirestoreDto(lessonId = id, status = estado.codigo, updatedAt = fecha)
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.d("LeccionRepository", "guardarEstado: ${e.message}")
            Result.failure(errorDe(e))
        }
    }
}
