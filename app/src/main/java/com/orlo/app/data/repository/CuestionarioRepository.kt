package com.orlo.app.data.repository

import android.util.Log
import com.orlo.app.data.datasource.ContenidoRemoteDataSource
import com.orlo.app.data.dtos.PuntuarCuestionarioDto
import com.orlo.app.data.dtos.RespuestaDto
import com.orlo.app.data.dtos.toCuestionarioUi
import com.orlo.app.data.dtos.toEstiloUi
import com.orlo.app.data.dtos.toResultadoUi
import com.orlo.app.data.model.CuestionarioUi
import com.orlo.app.data.model.EstiloUi
import com.orlo.app.data.model.ResultadoUi
import javax.inject.Inject

// El cuestionario y su calculo viven en el backend: la tabla de puntos la editan los fundadores y
// el mismo calculo sirve para Android, iOS y web
class CuestionarioRepository @Inject constructor(
    private val contenidoRemoteDataSource: ContenidoRemoteDataSource,
) {

    suspend fun getCuestionario(): Result<CuestionarioUi> {
        return try {
            val cuestionario = contenidoRemoteDataSource.getCuestionario().toCuestionarioUi()
            // Un cuestionario sin preguntas no se puede responder: es como si no existiera
            if (cuestionario.preguntas.isEmpty()) {
                Result.failure(ContenidoNoEncontradoException())
            } else {
                Result.success(cuestionario)
            }
        } catch (e: Exception) {
            Log.d("CuestionarioRepository", "getCuestionario: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    // Las respuestas son el id de cada pregunta con las opciones elegidas
    suspend fun puntuar(respuestas: Map<Long, Set<Long>>, fecha: Long): Result<ResultadoUi> {
        if (respuestas.isEmpty() || respuestas.values.any { it.isEmpty() }) {
            return Result.failure(CuestionarioIncompletoException())
        }
        return try {
            val dto = PuntuarCuestionarioDto(
                answers = respuestas.map { (pregunta, opciones) ->
                    RespuestaDto(questionId = pregunta, optionIds = opciones.toList())
                }
            )
            val resultado = contenidoRemoteDataSource.puntuarCuestionario(dto).toResultadoUi(fecha)
            if (resultado.principal.code.isBlank()) {
                Result.failure(ErrorDelServidorException())
            } else {
                Result.success(resultado)
            }
        } catch (e: Exception) {
            Log.d("CuestionarioRepository", "puntuar: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    suspend fun getEstilos(): Result<List<EstiloUi>> {
        return try {
            Result.success(contenidoRemoteDataSource.getEstilos().map { it.toEstiloUi() })
        } catch (e: Exception) {
            Log.d("CuestionarioRepository", "getEstilos: ${e.message}")
            Result.failure(errorDe(e))
        }
    }
}
