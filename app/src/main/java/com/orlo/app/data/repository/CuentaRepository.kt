package com.orlo.app.data.repository

import android.util.Log
import com.orlo.app.data.datasource.AsesorRemoteDataSource
import com.orlo.app.data.datasource.AuthRemoteDataSource
import com.orlo.app.data.datasource.StorageRemoteDataSource
import com.orlo.app.data.datasource.UsuarioRemoteDataSource
import com.orlo.app.data.dtos.toCuentaUi
import com.orlo.app.data.model.CuentaUi
import javax.inject.Inject

// La cuenta en el backend (premium y mensajes del asesor) y el borrado completo de la cuenta
class CuentaRepository @Inject constructor(
    private val asesorRemoteDataSource: AsesorRemoteDataSource,
    private val usuarioRemoteDataSource: UsuarioRemoteDataSource,
    private val storageRemoteDataSource: StorageRemoteDataSource,
    private val authRemoteDataSource: AuthRemoteDataSource,
) {

    suspend fun getCuenta(): Result<CuentaUi> {
        return try {
            Result.success(asesorRemoteDataSource.getCuenta().toCuentaUi())
        } catch (e: Exception) {
            Log.d("CuentaRepository", "getCuenta: ${e.message}")
            Result.failure(errorDe(e))
        }
    }

    // Derecho de supresion: primero los datos que la app puede borrar (Firestore y las fotos),
    // despues el backend borra su registro y la cuenta de Authentication, y al final se cierra la
    // sesion del celular. El orden importa: sin la cuenta ya no hay permiso para borrar lo demas
    suspend fun eliminarCuenta(): Result<Unit> {
        return try {
            val uid = authRemoteDataSource.currentUser?.uid ?: throw SinSesionException()
            usuarioRemoteDataSource.borrarDatosDelUsuario(uid)
            storageRemoteDataSource.deleteFolder("$CARPETA_PRENDAS/$uid")
            asesorRemoteDataSource.borrarCuenta()
            authRemoteDataSource.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.d("CuentaRepository", "eliminarCuenta: ${e.message}")
            Result.failure(errorDe(e))
        }
    }
}

// Las fotos de las prendas de cada usuario van en su propia carpeta
const val CARPETA_PRENDAS = "garmentImages"
