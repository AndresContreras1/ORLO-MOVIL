package com.orlo.app.data.repository

import android.util.Log
import com.orlo.app.data.datasource.ArmarioRemoteDataSource
import com.orlo.app.data.datasource.AuthRemoteDataSource
import com.orlo.app.data.datasource.ExploraRemoteDataSource
import com.orlo.app.data.datasource.GuardadoRemoteDataSource
import com.orlo.app.data.datasource.UsuarioRemoteDataSource
import com.orlo.app.data.dtos.CompletarArmarioDto
import com.orlo.app.data.dtos.PrendaArmarioDto
import com.orlo.app.data.dtos.toGuardadoFirestoreDto
import com.orlo.app.data.dtos.toGuardadoUi
import com.orlo.app.data.dtos.toItemExploraUi
import com.orlo.app.data.dtos.toSugerenciaArmarioUi
import com.orlo.app.data.model.GuardadoUi
import com.orlo.app.data.model.ItemExploraUi
import com.orlo.app.data.model.SeccionExplora
import com.orlo.app.data.model.SugerenciaArmarioUi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// Explora une el catalogo del backend con lo que el usuario tiene en Firestore: su estilo para
// "Para tu estilo", su armario para "Completa tu armario" y sus guardados
class ExploraRepository @Inject constructor(
    private val exploraRemoteDataSource: ExploraRemoteDataSource,
    private val guardadoRemoteDataSource: GuardadoRemoteDataSource,
    private val usuarioRemoteDataSource: UsuarioRemoteDataSource,
    private val armarioRemoteDataSource: ArmarioRemoteDataSource,
    private val authRemoteDataSource: AuthRemoteDataSource,
) {

    // "Para tu estilo" se filtra con el estilo principal del cuestionario; las demas no
    suspend fun getSeccion(seccion: SeccionExplora): Result<List<ItemExploraUi>> {
        val codigo = seccion.codigo ?: return Result.failure(DatosInvalidosException())
        return try {
            val estilo = if (seccion == SeccionExplora.PARA_TU_ESTILO) {
                usuarioRemoteDataSource.getUsuario(authRemoteDataSource.uidOError())
                    ?.currentResult?.primary?.code?.takeIf { it.isNotBlank() }
            } else {
                null
            }
            Result.success(exploraRemoteDataSource.getItems(codigo, estilo).map { it.toItemExploraUi() })
        } catch (e: Exception) {
            Log.d("ExploraRepository", "getSeccion: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    suspend fun getItem(id: String): Result<ItemExploraUi> {
        return try {
            Result.success(exploraRemoteDataSource.getItem(id).toItemExploraUi())
        } catch (e: Exception) {
            Log.d("ExploraRepository", "getItem: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    // Al backend solo va la categoria y el color de cada prenda, y el estilo principal
    suspend fun completarArmario(): Result<List<SugerenciaArmarioUi>> {
        return try {
            val uid = authRemoteDataSource.uidOError()
            val (usuario, prendas) = coroutineScope {
                val usuario = async { usuarioRemoteDataSource.getUsuario(uid) }
                val prendas = async { armarioRemoteDataSource.getPrendas(uid) }
                usuario.await() to prendas.await()
            }
            val pedido = CompletarArmarioDto(
                primaryStyle = usuario?.currentResult?.primary?.code?.takeIf { it.isNotBlank() },
                wardrobe = prendas.take(MAX_PRENDAS).map { PrendaArmarioDto(category = it.category, color = it.color) },
            )
            Result.success(exploraRemoteDataSource.completarArmario(pedido).map { it.toSugerenciaArmarioUi() })
        } catch (e: Exception) {
            Log.d("ExploraRepository", "completarArmario: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    // En vivo: guardar o quitar desde el detalle se ve al volver a la pestana sin recargar
    fun observarGuardados(): Flow<Result<List<GuardadoUi>>> =
        authRemoteDataSource.enVivo("ExploraRepository") { uid ->
            guardadoRemoteDataSource.observarGuardados(uid).map { lista -> lista.map { it.toGuardadoUi() } }
        }

    suspend fun guardar(item: ItemExploraUi, fecha: Long): Result<Unit> {
        return try {
            if (item.id.toLongOrNull() == null) throw DatosInvalidosException()
            guardadoRemoteDataSource.guardar(authRemoteDataSource.uidOError(), item.id, item.toGuardadoFirestoreDto(fecha))
            Result.success(Unit)
        } catch (e: Exception) {
            Log.d("ExploraRepository", "guardar: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    suspend fun quitar(itemId: String): Result<Unit> {
        return try {
            guardadoRemoteDataSource.quitar(authRemoteDataSource.uidOError(), itemId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.d("ExploraRepository", "quitar: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    companion object {
        // Limite que acepta el backend
        const val MAX_PRENDAS = 200
    }
}
