package com.orlo.app.data.repository

import android.util.Log
import com.orlo.app.data.datasource.ArmarioRemoteDataSource
import com.orlo.app.data.datasource.AsesorRemoteDataSource
import com.orlo.app.data.datasource.AuthRemoteDataSource
import com.orlo.app.data.datasource.ConversacionRemoteDataSource
import com.orlo.app.data.datasource.UsuarioRemoteDataSource
import com.orlo.app.data.dtos.HistorialAsesorDto
import com.orlo.app.data.dtos.MensajeAsesorDto
import com.orlo.app.data.dtos.PerfilAsesorDto
import com.orlo.app.data.dtos.PrendaAsesorDto
import com.orlo.app.data.dtos.toConversacionUi
import com.orlo.app.data.dtos.toGuardarMensajeDto
import com.orlo.app.data.dtos.toMensajeChatUi
import com.orlo.app.data.dtos.toPerfilUi
import com.orlo.app.data.dtos.toPrendaUi
import com.orlo.app.data.dtos.toRespuestaAsesorUi
import com.orlo.app.data.model.AccionRapida
import com.orlo.app.data.model.ConversacionUi
import com.orlo.app.data.model.MensajeChatUi
import com.orlo.app.data.model.OpcionesDelPerfil
import com.orlo.app.data.model.PerfilUi
import com.orlo.app.data.model.PrendaUi
import com.orlo.app.data.model.RespuestaAsesorUi
import com.orlo.app.data.model.RolMensaje
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// La conversacion en la que quedo el mensaje y la respuesta del asesor
data class ResultadoPregunta(
    val conversacionId: String,
    val respuesta: RespuestaAsesorUi,
)

// Une cuatro fuentes para dar una sola respuesta al ViewModel: el perfil y las prendas (Firestore)
// arman lo que el asesor sabe del usuario, el backend responde y la conversacion queda en Firestore
class AsesorRepository @Inject constructor(
    private val asesorRemoteDataSource: AsesorRemoteDataSource,
    private val conversacionRemoteDataSource: ConversacionRemoteDataSource,
    private val usuarioRemoteDataSource: UsuarioRemoteDataSource,
    private val armarioRemoteDataSource: ArmarioRemoteDataSource,
    private val authRemoteDataSource: AuthRemoteDataSource,
) {

    // Primero el backend: si responde 403, 429 o 503 no se guarda nada en la conversacion. La
    // pregunta y la respuesta se guardan juntas despues
    suspend fun preguntar(
        conversacionId: String?,
        texto: String,
        accion: AccionRapida?,
        historial: List<MensajeChatUi>,
        fecha: Long,
    ): Result<ResultadoPregunta> {
        return try {
            val uid = authRemoteDataSource.uidOError()

            // El perfil y las prendas son independientes: se piden al tiempo. Si fallan, el asesor
            // responde igual con menos contexto
            val (perfil, prendas) = coroutineScope {
                val perfil = async { runCatching { usuarioRemoteDataSource.getUsuario(uid)?.toPerfilUi() }.getOrNull() }
                val prendas = async { runCatching { armarioRemoteDataSource.getPrendas(uid).map { it.toPrendaUi() } }.getOrNull() }
                perfil.await() to prendas.await()
            }

            val respuesta = asesorRemoteDataSource.enviarMensaje(
                MensajeAsesorDto(
                    message = texto.take(MAX_TEXTO),
                    quickAction = accion?.codigo,
                    profile = (perfil ?: PerfilUi()).toPerfilAsesorDto(),
                    wardrobe = (prendas ?: emptyList()).take(MAX_PRENDAS).map { it.toPrendaAsesorDto() },
                    // Solo los ultimos mensajes con texto: suficiente contexto y el backend no
                    // acepta textos vacios en el historial
                    history = historial.filter { it.texto.isNotBlank() }.takeLast(MAX_HISTORIAL).map {
                        HistorialAsesorDto(
                            role = if (it.rol == RolMensaje.ASESOR) "ASSISTANT" else "USER",
                            text = it.texto,
                        )
                    },
                )
            ).toRespuestaAsesorUi(fecha = fecha + 1)

            val pregunta = MensajeChatUi(rol = RolMensaje.USUARIO, texto = texto, fecha = fecha)
            val id = conversacionRemoteDataSource.guardarIntercambio(
                userId = uid,
                conversacionId = conversacionId,
                titulo = texto.take(MAX_TITULO),
                pregunta = pregunta.toGuardarMensajeDto(),
                respuesta = respuesta.mensaje.toGuardarMensajeDto(),
            )

            Result.success(ResultadoPregunta(conversacionId = id, respuesta = respuesta))
        } catch (e: Exception) {
            Log.d("AsesorRepository", "preguntar: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    suspend fun getConversaciones(): Result<List<ConversacionUi>> {
        return try {
            Result.success(
                conversacionRemoteDataSource.getConversaciones(authRemoteDataSource.uidOError()).map { it.toConversacionUi() }
            )
        } catch (e: Exception) {
            Log.d("AsesorRepository", "getConversaciones: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    fun observarConversaciones(): Flow<Result<List<ConversacionUi>>> =
        authRemoteDataSource.enVivo("AsesorRepository") { uid ->
            conversacionRemoteDataSource.observarConversaciones(uid).map { lista -> lista.map { it.toConversacionUi() } }
        }

    suspend fun getMensajes(conversacionId: String): Result<List<MensajeChatUi>> {
        return try {
            Result.success(
                conversacionRemoteDataSource.getMensajes(authRemoteDataSource.uidOError(), conversacionId)
                    .map { it.toMensajeChatUi() }
            )
        } catch (e: Exception) {
            Log.d("AsesorRepository", "getMensajes: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    suspend fun borrarConversacion(conversacionId: String): Result<Unit> {
        return try {
            conversacionRemoteDataSource.borrarConversacion(authRemoteDataSource.uidOError(), conversacionId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.d("AsesorRepository", "borrarConversacion: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    // Lo que el asesor sabe del usuario, en palabras: el primer nombre y las preferencias. Nada de
    // correo, apellidos ni identificadores
    private fun PerfilUi.toPerfilAsesorDto(): PerfilAsesorDto = PerfilAsesorDto(
        name = nombre.trim().split(" ").firstOrNull().orEmpty(),
        ageRange = OpcionesDelPerfil.paraAsesor(OpcionesDelPerfil.rangosDeEdad, rangoEdad),
        garmentPronoun = OpcionesDelPerfil.paraAsesor(OpcionesDelPerfil.tratoPrendas, tratoPrendas),
        primaryStyle = resultadoActual?.principal?.nombre,
        secondaryStyle = resultadoActual?.secundario?.nombre,
        skinTone = OpcionesDelPerfil.colorParaAsesor(OpcionesDelPerfil.tonosDePiel, tonoPiel),
        hairColor = OpcionesDelPerfil.colorParaAsesor(OpcionesDelPerfil.coloresDeCabello, colorCabello),
        eyeColor = OpcionesDelPerfil.colorParaAsesor(OpcionesDelPerfil.coloresDeOjos, colorOjos),
        climate = OpcionesDelPerfil.paraAsesor(OpcionesDelPerfil.climas, clima),
        budget = OpcionesDelPerfil.paraAsesor(OpcionesDelPerfil.presupuestos, presupuesto),
        occasions = ocasiones.mapNotNull { OpcionesDelPerfil.paraAsesor(OpcionesDelPerfil.ocasiones, it) },
    )

    private fun PrendaUi.toPrendaAsesorDto(): PrendaAsesorDto = PrendaAsesorDto(
        name = nombre.ifBlank { categoria.paraAsesor },
        category = categoria.paraAsesor,
        color = color,
    )

    companion object {
        const val MAX_HISTORIAL = 10
        const val MAX_TITULO = 40
        // Limites que acepta el backend
        const val MAX_TEXTO = 1000
        const val MAX_PRENDAS = 80
    }
}
