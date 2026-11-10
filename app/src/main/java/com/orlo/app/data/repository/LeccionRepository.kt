package com.orlo.app.data.repository

import android.util.Log
import com.orlo.app.data.datasource.AuthRemoteDataSource
import com.orlo.app.data.datasource.LeccionRemoteDataSource
import com.orlo.app.data.datasource.UsuarioRemoteDataSource
import com.orlo.app.data.dtos.ProgresoLeccionFirestoreDto
import com.orlo.app.data.dtos.toLeccionResumenUi
import com.orlo.app.data.dtos.toLeccionUi
import com.orlo.app.data.model.EstadoLeccion
import com.orlo.app.data.model.LeccionResumenUi
import com.orlo.app.data.model.LeccionUi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

// Une dos fuentes: las lecciones vienen del backend y el avance de cada una de Firestore
class LeccionRepository @Inject constructor(
    private val leccionRemoteDataSource: LeccionRemoteDataSource,
    private val usuarioRemoteDataSource: UsuarioRemoteDataSource,
    private val authRemoteDataSource: AuthRemoteDataSource,
) {

    // Las lecciones y el avance son independientes: se piden al tiempo. Si falla el avance, las
    // lecciones se muestran igual, todas sin empezar (el try va dentro de su propio async)
    suspend fun getLecciones(): Result<List<LeccionResumenUi>> {
        return try {
            val uid = authRemoteDataSource.uidOError()
            coroutineScope {
                val lecciones = async { leccionRemoteDataSource.getLecciones() }
                val progreso = async {
                    try {
                        usuarioRemoteDataSource.getProgresoLecciones(uid)
                            .associate { it.lessonId.toString() to EstadoLeccion.desdeCodigo(it.status) }
                    } catch (e: Exception) {
                        Log.d("LeccionRepository", "getLecciones (avance): ${e.message}")
                        emptyMap()
                    }
                }
                val avance = progreso.await()
                Result.success(
                    lecciones.await()
                        .map { it.toLeccionResumenUi(avance[it.id ?: ""] ?: EstadoLeccion.SIN_EMPEZAR) }
                        .sortedBy { it.orden }
                )
            }
        } catch (e: Exception) {
            Log.d("LeccionRepository", "getLecciones: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    suspend fun getLeccion(id: String): Result<LeccionUi> {
        return try {
            Result.success(leccionRemoteDataSource.getLeccion(id).toLeccionUi())
        } catch (e: Exception) {
            Log.d("LeccionRepository", "getLeccion: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    suspend fun getEstado(id: String): Result<EstadoLeccion> {
        return try {
            val progreso = usuarioRemoteDataSource.getProgresoLecciones(authRemoteDataSource.uidOError())
                .firstOrNull { it.lessonId.toString() == id }
            Result.success(EstadoLeccion.desdeCodigo(progreso?.status))
        } catch (e: Exception) {
            Log.d("LeccionRepository", "getEstado: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    suspend fun guardarEstado(id: String, estado: EstadoLeccion, fecha: Long): Result<Unit> {
        return try {
            // El backend numera las lecciones: en Firestore el avance guarda ese numero
            val lessonId = id.toLongOrNull() ?: throw DatosInvalidosException()
            usuarioRemoteDataSource.guardarProgresoLeccion(
                authRemoteDataSource.uidOError(),
                ProgresoLeccionFirestoreDto(lessonId = lessonId, status = estado.codigo, updatedAt = fecha)
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.d("LeccionRepository", "guardarEstado: ${e.message}")
            Result.failure(errorDe(e))
        }
    }
}
