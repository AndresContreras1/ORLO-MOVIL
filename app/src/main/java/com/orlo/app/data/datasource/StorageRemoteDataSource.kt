package com.orlo.app.data.datasource

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

// Sube y borra archivos en Firebase Storage. Solo ejecuta la operacion: si un archivo no existe, que
// hacer con ese error lo decide el repositorio
class StorageRemoteDataSource @Inject constructor(
    private val storage: FirebaseStorage
) {

    // Retorna la URL de descarga, que es la que se guarda en el documento de Firestore. Las reglas
    // de Storage solo aceptan imagenes: el tipo va en los metadatos
    suspend fun uploadImage(path: String, bytes: ByteArray): String {
        val ref = storage.reference.child(path)
        val metadatos = StorageMetadata.Builder().setContentType("image/jpeg").build()
        ref.putBytes(bytes, metadatos).await()
        return EmuladorFirebase.urlParaGuardar(ref.downloadUrl.await().toString())
    }

    suspend fun deleteImage(path: String): Unit {
        storage.reference.child(path).delete().await()
    }

    // Borra todas las fotos de una carpeta, por ejemplo las prendas de un usuario
    suspend fun deleteFolder(path: String): Unit {
        val lista = storage.reference.child(path).listAll().await()
        lista.items.forEach { it.delete().await() }
    }
}
