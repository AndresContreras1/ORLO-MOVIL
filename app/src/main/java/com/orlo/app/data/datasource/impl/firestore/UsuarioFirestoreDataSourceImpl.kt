package com.orlo.app.data.datasource.impl.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.orlo.app.data.datasource.UsuarioRemoteDataSource
import com.orlo.app.data.dtos.ProgresoLeccionFirestoreDto
import com.orlo.app.data.dtos.RegistrarUsuarioDto
import com.orlo.app.data.dtos.ResultadoFirestoreDto
import com.orlo.app.data.dtos.UsuarioFirestoreDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Nombres de las colecciones: los mismos en todos los data sources y en las reglas de seguridad
const val USERS = "users"
const val RESULTS = "results"
const val LESSON_PROGRESS = "lessonProgress"
const val GARMENTS = "garments"
const val OUTFITS = "outfits"
const val CONVERSATIONS = "conversations"
const val MESSAGES = "messages"

class UsuarioFirestoreDataSourceImpl @Inject constructor(
    private val db: FirebaseFirestore
) : UsuarioRemoteDataSource {

    // El documento del usuario usa el UID de Authentication como id
    override suspend fun registrarUsuario(userId: String, usuario: RegistrarUsuarioDto) {
        db.collection(USERS).document(userId).set(usuario).await()
    }

    override suspend fun getUsuario(userId: String): UsuarioFirestoreDto? {
        val respuesta = db.collection(USERS).document(userId).get().await()
        val usuario = respuesta.toObject(UsuarioFirestoreDto::class.java) ?: return null
        return usuario.copy(id = respuesta.id)
    }

    // merge: si el documento todavia no existe se crea con estos campos
    override suspend fun actualizarPerfil(userId: String, campos: Map<String, Any>) {
        db.collection(USERS).document(userId).set(campos, SetOptions.merge()).await()
    }

    // El historial y el resultado actual se escriben juntos: o quedan los dos o ninguno
    override suspend fun guardarResultado(userId: String, resultado: ResultadoFirestoreDto): String {
        val usuarioRef = db.collection(USERS).document(userId)
        val resultadoRef = usuarioRef.collection(RESULTS).document()
        val conId = resultado.copy(id = resultadoRef.id)

        db.batch()
            .set(resultadoRef, conId)
            .set(usuarioRef, mapOf("currentResult" to conId), SetOptions.merge())
            .commit()
            .await()

        return resultadoRef.id
    }

    override suspend fun getResultados(userId: String): List<ResultadoFirestoreDto> {
        val respuesta = db.collection(USERS).document(userId).collection(RESULTS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
        return respuesta.documents.mapNotNull { doc ->
            doc.toObject(ResultadoFirestoreDto::class.java)?.copy(id = doc.id)
        }
    }

    override suspend fun getProgresoLecciones(userId: String): List<ProgresoLeccionFirestoreDto> {
        val respuesta = db.collection(USERS).document(userId).collection(LESSON_PROGRESS).get().await()
        return respuesta.documents.mapNotNull { it.toObject(ProgresoLeccionFirestoreDto::class.java) }
    }

    // El id del documento es el id de la leccion: una sola entrada de avance por leccion
    override suspend fun guardarProgresoLeccion(userId: String, progreso: ProgresoLeccionFirestoreDto) {
        db.collection(USERS).document(userId).collection(LESSON_PROGRESS)
            .document(progreso.lessonId.toString())
            .set(progreso)
            .await()
    }

    // Borrar un documento no borra sus subcolecciones: se borran una por una y al final el usuario.
    // Los mensajes estan dentro de cada conversacion, un nivel mas abajo
    override suspend fun borrarDatosDelUsuario(userId: String) {
        val usuarioRef = db.collection(USERS).document(userId)

        val conversaciones = usuarioRef.collection(CONVERSATIONS).get().await()
        conversaciones.documents.forEach { conversacion ->
            borrarColeccion(conversacion.reference.collection(MESSAGES).get().await().documents.map { it.reference })
        }

        listOf(RESULTS, LESSON_PROGRESS, GARMENTS, OUTFITS, CONVERSATIONS).forEach { nombre ->
            borrarColeccion(usuarioRef.collection(nombre).get().await().documents.map { it.reference })
        }

        usuarioRef.delete().await()
    }

    // Un batch admite hasta 500 operaciones: se borra por partes
    private suspend fun borrarColeccion(referencias: List<com.google.firebase.firestore.DocumentReference>) {
        referencias.chunked(500).forEach { parte ->
            val batch = db.batch()
            parte.forEach { batch.delete(it) }
            batch.commit().await()
        }
    }
}
