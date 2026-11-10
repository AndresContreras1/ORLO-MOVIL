package com.orlo.app.data.datasource

import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Sube y borra archivos en Firebase Storage. Solo ejecuta la operacion: si un archivo no existe, que
// hacer con ese error lo decide el repositorio
class StorageRemoteDataSource @Inject constructor(
    private val storage: FirebaseStorage
) {

    // Retorna la URL de descarga, que es la que se guarda en el documento de Firestore
    suspend fun uploadImage(path: String, bytes: ByteArray): String {
        val ref = storage.reference.child(path)
        ref.putBytes(bytes).await()
        return ref.downloadUrl.await().toString()
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
