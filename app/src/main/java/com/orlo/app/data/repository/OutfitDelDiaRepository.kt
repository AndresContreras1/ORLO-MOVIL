package com.orlo.app.data.repository

import android.util.Log
import com.orlo.app.data.datasource.ArmarioRemoteDataSource
import com.orlo.app.data.datasource.AsesorRemoteDataSource
import com.orlo.app.data.datasource.AuthRemoteDataSource
import com.orlo.app.data.datasource.OutfitDelDiaRemoteDataSource
import com.orlo.app.data.datasource.UsuarioRemoteDataSource
import com.orlo.app.data.dtos.OutfitDelDiaPedidoDto
import com.orlo.app.data.dtos.toOutfitDelDiaFirestoreDto
import com.orlo.app.data.dtos.toOutfitDelDiaUi
import com.orlo.app.data.dtos.toPerfilAsesorDto
import com.orlo.app.data.dtos.toPerfilUi
import com.orlo.app.data.dtos.toPrendaAsesorDto
import com.orlo.app.data.dtos.toPrendaUi
import com.orlo.app.data.model.OutfitDelDiaUi
import com.orlo.app.data.model.PerfilUi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

// El outfit del dia: el asesor lo arma con el perfil y el armario, y queda guardado en Firestore con
// la fecha, para no pedirlo de nuevo cada vez que se abre el inicio
class OutfitDelDiaRepository @Inject constructor(
    private val asesorRemoteDataSource: AsesorRemoteDataSource,
    private val outfitDelDiaRemoteDataSource: OutfitDelDiaRemoteDataSource,
    private val usuarioRemoteDataSource: UsuarioRemoteDataSource,
    private val armarioRemoteDataSource: ArmarioRemoteDataSource,
    private val authRemoteDataSource: AuthRemoteDataSource,
) {

    // null si todavia no se ha pedido el de hoy
    suspend fun getDeHoy(fecha: String): Result<OutfitDelDiaUi?> {
        return try {
            Result.success(
                outfitDelDiaRemoteDataSource.getOutfitDelDia(authRemoteDataSource.uidOError(), fecha)?.toOutfitDelDiaUi()
            )
        } catch (e: Exception) {
            Log.d("OutfitDelDiaRepository", "getDeHoy: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    // Pedir otra opcion el mismo dia reemplaza la anterior. El backend permite 3 al dia (429)
    suspend fun pedir(fecha: String, ahora: Long): Result<OutfitDelDiaUi> {
        return try {
            val uid = authRemoteDataSource.uidOError()
            val (perfil, prendas) = coroutineScope {
                val perfil = async { usuarioRemoteDataSource.getUsuario(uid)?.toPerfilUi() }
                val prendas = async { armarioRemoteDataSource.getPrendas(uid).map { it.toPrendaUi() } }
                perfil.await() to prendas.await()
            }
            val outfit = asesorRemoteDataSource.outfitDelDia(
                OutfitDelDiaPedidoDto(
                    profile = (perfil ?: PerfilUi()).toPerfilAsesorDto(),
                    wardrobe = prendas.take(AsesorRepository.MAX_PRENDAS).map { it.toPrendaAsesorDto() },
                )
            ).toOutfitDelDiaUi(fecha = ahora)
            if (outfit.titulo.isBlank() || outfit.prendas.isEmpty()) throw ErrorDelServidorException()

            outfitDelDiaRemoteDataSource.guardarOutfitDelDia(uid, fecha, outfit.toOutfitDelDiaFirestoreDto())
            Result.success(outfit)
        } catch (e: Exception) {
            Log.d("OutfitDelDiaRepository", "pedir: ${e.message}")
            Result.failure(errorDe(e))
        }
    }
}
