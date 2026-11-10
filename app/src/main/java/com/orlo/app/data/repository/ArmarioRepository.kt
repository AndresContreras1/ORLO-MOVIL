package com.orlo.app.data.repository

import android.net.Uri
import android.util.Log
import com.orlo.app.data.datasource.ArmarioRemoteDataSource
import com.orlo.app.data.datasource.AuthRemoteDataSource
import com.orlo.app.data.datasource.ImagenLocalDataSource
import com.orlo.app.data.datasource.StorageRemoteDataSource
import com.orlo.app.data.dtos.toGuardarOutfitDto
import com.orlo.app.data.dtos.toGuardarPrendaDto
import com.orlo.app.data.dtos.toOutfitUi
import com.orlo.app.data.dtos.toPrendaUi
import com.orlo.app.data.model.OcasionOutfit
import com.orlo.app.data.model.OrigenOutfit
import com.orlo.app.data.model.OutfitPropuestoUi
import com.orlo.app.data.model.OutfitUi
import com.orlo.app.data.model.PrendaUi
import com.google.firebase.storage.StorageException
import javax.inject.Inject

// El armario digital: prendas con foto y outfits. Las fotos se comprimen y suben a Storage; la URL
// queda guardada en el documento de la prenda. Si es crear o actualizar lo decide este repositorio
class ArmarioRepository @Inject constructor(
    private val armarioRemoteDataSource: ArmarioRemoteDataSource,
    private val storageRemoteDataSource: StorageRemoteDataSource,
    private val imagenLocalDataSource: ImagenLocalDataSource,
    private val authRemoteDataSource: AuthRemoteDataSource,
) {

    private fun rutaFoto(uid: String, prendaId: String) = "$CARPETA_PRENDAS/$uid/$prendaId.jpg"

    suspend fun getPrendas(): Result<List<PrendaUi>> {
        return try {
            Result.success(armarioRemoteDataSource.getPrendas(authRemoteDataSource.uidOError()).map { it.toPrendaUi() })
        } catch (e: Exception) {
            Log.d("ArmarioRepository", "getPrendas: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    suspend fun getPrenda(prendaId: String): Result<PrendaUi> {
        return try {
            val prenda = armarioRemoteDataSource.getPrenda(authRemoteDataSource.uidOError(), prendaId)
                ?: throw ContenidoNoEncontradoException()
            Result.success(prenda.toPrendaUi())
        } catch (e: Exception) {
            Log.d("ArmarioRepository", "getPrenda: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    // fotoNueva es la foto elegida (galeria, archivo o camara); null deja la foto que ya tenia
    suspend fun guardarPrenda(prenda: PrendaUi, fotoNueva: Uri?, fecha: Long): Result<PrendaUi> {
        return try {
            val uid = authRemoteDataSource.uidOError()
            val id = prenda.id.ifBlank { armarioRemoteDataSource.nuevoIdDePrenda(uid) }

            val fotoUrl = if (fotoNueva != null) {
                val bytes = imagenLocalDataSource.comprimir(fotoNueva) ?: throw ErrorAlSubirImagenException()
                storageRemoteDataSource.uploadImage(rutaFoto(uid, id), bytes)
            } else {
                prenda.fotoUrl
            }

            val guardada = prenda.copy(
                id = id,
                fotoUrl = fotoUrl,
                fecha = if (prenda.fecha == 0L) fecha else prenda.fecha,
            )
            armarioRemoteDataSource.guardarPrenda(uid, id, guardada.toGuardarPrendaDto())
            Result.success(guardada)
        } catch (e: Exception) {
            Log.d("ArmarioRepository", "guardarPrenda: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    suspend fun marcarPrendaFavorita(prendaId: String, favorita: Boolean): Result<Unit> {
        return try {
            armarioRemoteDataSource.marcarPrendaFavorita(authRemoteDataSource.uidOError(), prendaId, favorita)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.d("ArmarioRepository", "marcarPrendaFavorita: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    // Se borra tambien su foto, para no dejar archivos sueltos en Storage. Si la foto ya no existia,
    // el resultado es el mismo: se sigue con el documento
    suspend fun borrarPrenda(prenda: PrendaUi): Result<Unit> {
        return try {
            val uid = authRemoteDataSource.uidOError()
            if (prenda.fotoUrl != null) {
                try {
                    storageRemoteDataSource.deleteImage(rutaFoto(uid, prenda.id))
                } catch (e: StorageException) {
                    if (e.errorCode != StorageException.ERROR_OBJECT_NOT_FOUND) throw e
                }
            }
            armarioRemoteDataSource.borrarPrenda(uid, prenda.id)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.d("ArmarioRepository", "borrarPrenda: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    suspend fun getOutfits(): Result<List<OutfitUi>> {
        return try {
            Result.success(armarioRemoteDataSource.getOutfits(authRemoteDataSource.uidOError()).map { it.toOutfitUi() })
        } catch (e: Exception) {
            Log.d("ArmarioRepository", "getOutfits: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    suspend fun getOutfit(outfitId: String): Result<OutfitUi> {
        return try {
            val outfit = armarioRemoteDataSource.getOutfit(authRemoteDataSource.uidOError(), outfitId)
                ?: throw ContenidoNoEncontradoException()
            Result.success(outfit.toOutfitUi())
        } catch (e: Exception) {
            Log.d("ArmarioRepository", "getOutfit: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    // Sin id es un outfit nuevo; con id se actualiza el existente
    suspend fun guardarOutfit(outfit: OutfitUi, fecha: Long): Result<OutfitUi> {
        return try {
            val uid = authRemoteDataSource.uidOError()
            val conFecha = outfit.copy(fecha = if (outfit.fecha == 0L) fecha else outfit.fecha)
            val id = if (conFecha.id.isBlank()) {
                armarioRemoteDataSource.crearOutfit(uid, conFecha.toGuardarOutfitDto())
            } else {
                armarioRemoteDataSource.actualizarOutfit(uid, conFecha.id, conFecha.toGuardarOutfitDto())
                conFecha.id
            }
            Result.success(conFecha.copy(id = id))
        } catch (e: Exception) {
            Log.d("ArmarioRepository", "guardarOutfit: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    // El boton "Guardar outfit" del asesor: el outfit queda como texto, con su porque
    suspend fun guardarOutfitDelAsesor(propuesto: OutfitPropuestoUi, fecha: Long): Result<OutfitUi> =
        guardarOutfit(
            outfit = OutfitUi(
                nombre = propuesto.titulo,
                ocasion = OcasionOutfit.OTRA,
                prendasTexto = propuesto.prendas,
                porQue = propuesto.porQue,
                origen = OrigenOutfit.ASESOR,
            ),
            fecha = fecha,
        )

    // Una copia nueva, que se puede editar sin tocar el original
    suspend fun duplicarOutfit(outfit: OutfitUi, sufijo: String, fecha: Long): Result<OutfitUi> =
        guardarOutfit(
            outfit = outfit.copy(id = "", nombre = "${outfit.nombre} $sufijo".trim().take(40), favorito = false, fecha = fecha),
            fecha = fecha,
        )

    suspend fun marcarOutfitFavorito(outfitId: String, favorito: Boolean): Result<Unit> {
        return try {
            armarioRemoteDataSource.marcarOutfitFavorito(authRemoteDataSource.uidOError(), outfitId, favorito)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.d("ArmarioRepository", "marcarOutfitFavorito: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    suspend fun borrarOutfit(outfitId: String): Result<Unit> {
        return try {
            armarioRemoteDataSource.borrarOutfit(authRemoteDataSource.uidOError(), outfitId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.d("ArmarioRepository", "borrarOutfit: ${e.message}")
            Result.failure(errorDe(e))
        }
    }
}
