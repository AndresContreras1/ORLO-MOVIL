package com.orlo.app.data.datasource

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Sube y borra archivos en Firebase Storage. La foto ya llega comprimida desde el repositorio
class StorageRemoteDataSource @Inject constructor(
    private val storage: FirebaseStorage
) {

    // Retorna la URL de descarga, que es la que se guarda en el documento de Firestore
    suspend fun uploadImage(path: String, bytes: ByteArray): String {
        val ref = storage.reference.child(path)
        ref.putBytes(bytes).await()
        return ref.downloadUrl.await().toString()
    }

    // Si el archivo ya no existe no es un error: el resultado es el mismo
    suspend fun deleteImage(path: String) {
        try {
            storage.reference.child(path).delete().await()
        } catch (e: StorageException) {
            if (e.errorCode != StorageException.ERROR_OBJECT_NOT_FOUND) throw e
        }
    }

    // Borra todas las fotos de una carpeta, por ejemplo las prendas de un usuario
    suspend fun deleteFolder(path: String) {
        val lista = storage.reference.child(path).listAll().await()
        lista.items.forEach { it.delete().await() }
    }
}
