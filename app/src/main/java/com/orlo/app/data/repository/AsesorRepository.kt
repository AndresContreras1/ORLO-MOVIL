package com.orlo.app.data.repository

import android.util.Log
import com.orlo.app.data.datasource.AsesorRemoteDataSource
import com.orlo.app.data.datasource.AuthRemoteDataSource
import com.orlo.app.data.datasource.ConversacionRemoteDataSource
import com.orlo.app.data.dtos.HistorialAsesorDto
import com.orlo.app.data.dtos.MensajeAsesorDto
import com.orlo.app.data.dtos.PerfilAsesorDto
import com.orlo.app.data.dtos.PrendaAsesorDto
import com.orlo.app.data.dtos.toConversacionUi
import com.orlo.app.data.dtos.toMensajeChatUi
import com.orlo.app.data.dtos.toMensajeFirestoreDto
import com.orlo.app.data.dtos.toRespuestaAsesorUi
import com.orlo.app.data.model.AccionRapida
import com.orlo.app.data.model.ConversacionUi
import com.orlo.app.data.model.MensajeChatUi
import com.orlo.app.data.model.RespuestaAsesorUi
import com.orlo.app.data.model.RolMensaje
import javax.inject.Inject

// Lo que el asesor recibe del usuario en cada pregunta, ya en texto: lo arma el ViewModel con el
// perfil y el armario. El repositorio solo lo traduce al DTO del backend
data class ContextoAsesor(
    val nombre: String,
    val rangoEdad: String? = null,
    val tratoPrendas: String? = null,
    val estiloPrincipal: String? = null,
    val estiloSecundario: String? = null,
    val tonoPiel: String? = null,
    val colorCabello: String? = null,
    val colorOjos: String? = null,
    val clima: String? = null,
    val presupuesto: String? = null,
    val ocasiones: List<String> = emptyList(),
    // Cada prenda como nombre, categoria y color
    val prendas: List<Triple<String, String, String>> = emptyList(),
)

// La conversacion en la que quedo el mensaje y la respuesta del asesor
data class ResultadoPregunta(
    val conversacionId: String,
    val respuesta: RespuestaAsesorUi,
)

class AsesorRepository @Inject constructor(
    private val asesorRemoteDataSource: AsesorRemoteDataSource,
    private val conversacionRemoteDataSource: ConversacionRemoteDataSource,
    private val authRemoteDataSource: AuthRemoteDataSource,
) {

    // Guarda la pregunta, pide la respuesta al backend y guarda la respuesta. Si la conversacion no
    // existe todavia, la crea con el inicio de la pregunta como titulo
    suspend fun preguntar(
        conversacionId: String?,
        texto: String,
        accion: AccionRapida?,
        contexto: ContextoAsesor,
        historial: List<MensajeChatUi>,
        fecha: Long,
    ): Result<ResultadoPregunta> {
        return try {
            val uid = authRemoteDataSource.currentUser?.uid ?: throw SinSesionException()

            // Primero el backend: si responde 403 o 429 no se guarda nada en la conversacion
            val respuesta = asesorRemoteDataSource.enviarMensaje(
                MensajeAsesorDto(
                    message = texto,
                    quickAction = accion?.codigo,
                    profile = contexto.toPerfilAsesorDto(),
                    wardrobe = contexto.prendas.map { (nombre, categoria, color) ->
                        PrendaAsesorDto(name = nombre, category = categoria, color = color)
                    },
                    // Solo los ultimos mensajes: suficiente contexto sin enviar toda la conversacion
                    history = historial.takeLast(MAX_HISTORIAL).map {
                        HistorialAsesorDto(
                            role = if (it.rol == RolMensaje.ASESOR) "ASSISTANT" else "USER",
                            text = it.texto,
                        )
                    },
                )
            ).toRespuestaAsesorUi(fecha = fecha + 1)

            val id = conversacionId ?: conversacionRemoteDataSource.crearConversacion(
                userId = uid,
                titulo = texto.take(MAX_TITULO),
                fecha = fecha,
            )

            val pregunta = MensajeChatUi(rol = RolMensaje.USUARIO, texto = texto, fecha = fecha)
            conversacionRemoteDataSource.agregarMensaje(uid, id, pregunta.toMensajeFirestoreDto())
            conversacionRemoteDataSource.agregarMensaje(uid, id, respuesta.mensaje.toMensajeFirestoreDto())

            Result.success(ResultadoPregunta(conversacionId = id, respuesta = respuesta))
        } catch (e: Exception) {
            Log.d("AsesorRepository", "preguntar: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    suspend fun getConversaciones(): Result<List<ConversacionUi>> {
        return try {
            val uid = authRemoteDataSource.currentUser?.uid ?: throw SinSesionException()
            Result.success(conversacionRemoteDataSource.getConversaciones(uid).map { it.toConversacionUi() })
        } catch (e: Exception) {
            Log.d("AsesorRepository", "getConversaciones: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    suspend fun getMensajes(conversacionId: String): Result<List<MensajeChatUi>> {
        return try {
            val uid = authRemoteDataSource.currentUser?.uid ?: throw SinSesionException()
            Result.success(
                conversacionRemoteDataSource.getMensajes(uid, conversacionId).map { it.toMensajeChatUi() }
            )
        } catch (e: Exception) {
            Log.d("AsesorRepository", "getMensajes: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    suspend fun borrarConversacion(conversacionId: String): Result<Unit> {
        return try {
            val uid = authRemoteDataSource.currentUser?.uid ?: throw SinSesionException()
            conversacionRemoteDataSource.borrarConversacion(uid, conversacionId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.d("AsesorRepository", "borrarConversacion: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    private fun ContextoAsesor.toPerfilAsesorDto(): PerfilAsesorDto = PerfilAsesorDto(
        name = nombre,
        ageRange = rangoEdad,
        garmentPronoun = tratoPrendas,
        primaryStyle = estiloPrincipal,
        secondaryStyle = estiloSecundario,
        skinTone = tonoPiel,
        hairColor = colorCabello,
        eyeColor = colorOjos,
        climate = clima,
        budget = presupuesto,
        occasions = ocasiones,
    )

    companion object {
        const val MAX_HISTORIAL = 10
        const val MAX_TITULO = 40
    }
}
