package com.orlo.app.data.repository

import android.net.Uri
import android.util.Log
import com.orlo.app.data.datasource.AuthRemoteDataSource
import com.orlo.app.data.datasource.ImagenLocalDataSource
import com.orlo.app.data.datasource.StorageRemoteDataSource
import com.orlo.app.data.datasource.UsuarioRemoteDataSource
import com.orlo.app.data.dtos.RegistrarUsuarioDto
import com.orlo.app.data.dtos.toActualizarPerfilDto
import com.orlo.app.data.dtos.toPerfilUi
import com.orlo.app.data.dtos.toResultadoFirestoreDto
import com.orlo.app.data.dtos.toResultadoUi
import com.orlo.app.data.model.PerfilUi
import com.orlo.app.data.model.ResultadoUi
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// El perfil del usuario y sus resultados del cuestionario. Todo se lee y se escribe con el uid de la
// sesion: la app nunca pide datos de otro usuario
class UsuarioRepository @Inject constructor(
    private val usuarioRemoteDataSource: UsuarioRemoteDataSource,
    private val storageRemoteDataSource: StorageRemoteDataSource,
    private val imagenLocalDataSource: ImagenLocalDataSource,
    private val authRemoteDataSource: AuthRemoteDataSource,
) {

    // Se llama justo despues de crear la cuenta en Authentication: el documento usa el mismo uid
    suspend fun registrarUsuario(nombre: String, fecha: Long): Result<Unit> {
        return try {
            usuarioRemoteDataSource.registrarUsuario(
                userId = authRemoteDataSource.uidOError(),
                usuario = RegistrarUsuarioDto(name = nombre.trim(), acceptedTermsAt = fecha, createdAt = fecha)
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.d("UsuarioRepository", "registrarUsuario: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    // Si la cuenta existe pero todavia no tiene documento (se registro y se cerro la app), el
    // repositorio lo informa como ContenidoNoEncontradoException: el splash lo manda a completar el
    // perfil y el formulario empieza vacio
    suspend fun getPerfil(): Result<PerfilUi> {
        return try {
            val usuario = usuarioRemoteDataSource.getUsuario(authRemoteDataSource.uidOError())
                ?: throw ContenidoNoEncontradoException()
            Result.success(usuario.toPerfilUi())
        } catch (e: Exception) {
            Log.d("UsuarioRepository", "getPerfil: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    // En vivo: el nombre, la foto, el estilo y el contador de prendas que mantiene la Cloud Function
    fun observarPerfil(): Flow<Result<PerfilUi>> =
        authRemoteDataSource.enVivo("UsuarioRepository") { uid ->
            usuarioRemoteDataSource.observarUsuario(uid).map { it?.toPerfilUi() ?: throw ContenidoNoEncontradoException() }
        }

    // Una sola foto por usuario: la nueva reemplaza la anterior en la misma ruta. La URL lleva la
    // fecha para que la app no muestre la foto vieja que tenia guardada
    suspend fun cambiarFoto(foto: Uri, fecha: Long): Result<String> {
        return try {
            val uid = authRemoteDataSource.uidOError()
            val bytes = imagenLocalDataSource.comprimir(foto) ?: throw ErrorAlSubirImagenException()
            val url = storageRemoteDataSource.uploadImage(rutaFotoDePerfil(uid), bytes) + "&v=$fecha"
            usuarioRemoteDataSource.actualizarFoto(uid, url)
            Result.success(url)
        } catch (e: Exception) {
            Log.d("UsuarioRepository", "cambiarFoto: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    suspend fun guardarPerfil(perfil: PerfilUi): Result<Unit> {
        return try {
            usuarioRemoteDataSource.actualizarPerfil(authRemoteDataSource.uidOError(), perfil.toActualizarPerfilDto())
            Result.success(Unit)
        } catch (e: Exception) {
            Log.d("UsuarioRepository", "guardarPerfil: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    // Queda en el historial y pasa a ser el resultado actual. Retorna el resultado con su id
    suspend fun guardarResultado(resultado: ResultadoUi): Result<ResultadoUi> {
        return try {
            val id = usuarioRemoteDataSource.guardarResultado(
                authRemoteDataSource.uidOError(),
                resultado.toResultadoFirestoreDto()
            )
            Result.success(resultado.copy(id = id))
        } catch (e: Exception) {
            Log.d("UsuarioRepository", "guardarResultado: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    suspend fun getResultados(): Result<List<ResultadoUi>> {
        return try {
            Result.success(
                usuarioRemoteDataSource.getResultados(authRemoteDataSource.uidOError()).map { it.toResultadoUi() }
            )
        } catch (e: Exception) {
            Log.d("UsuarioRepository", "getResultados: ${e.message}")
            Result.failure(errorDe(e))
        }
    }
}

const val CARPETA_FOTOS_PERFIL = "profileImages"

fun rutaFotoDePerfil(uid: String) = "$CARPETA_FOTOS_PERFIL/$uid.jpg"
