package com.orlo.app.data.datasource

import com.orlo.app.data.dtos.ProgresoLeccionFirestoreDto
import com.orlo.app.data.dtos.RegistrarUsuarioDto
import com.orlo.app.data.dtos.ResultadoFirestoreDto
import com.orlo.app.data.dtos.UsuarioFirestoreDto

// Contrato de los datos del usuario: perfil, resultados del cuestionario y avance en Aprende.
// Retorna DTOs; null cuando el documento no existe
interface UsuarioRemoteDataSource {

    suspend fun registrarUsuario(userId: String, usuario: RegistrarUsuarioDto)

    suspend fun getUsuario(userId: String): UsuarioFirestoreDto?

    suspend fun actualizarPerfil(userId: String, campos: Map<String, Any>)

    // Guarda el resultado en el historial y lo deja como resultado actual; retorna el id
    suspend fun guardarResultado(userId: String, resultado: ResultadoFirestoreDto): String

    suspend fun getResultados(userId: String): List<ResultadoFirestoreDto>

    suspend fun getProgresoLecciones(userId: String): List<ProgresoLeccionFirestoreDto>

    suspend fun guardarProgresoLeccion(userId: String, progreso: ProgresoLeccionFirestoreDto)

    // Borra el documento del usuario y todas sus subcolecciones (derecho de supresion)
    suspend fun borrarDatosDelUsuario(userId: String)
}
