package com.orlo.app.data.datasource.impl.firestore

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.orlo.app.data.datasource.UsuarioRemoteDataSource
import com.orlo.app.data.dtos.ActualizarPerfilDto
import com.orlo.app.data.dtos.ProgresoLeccionFirestoreDto
import com.orlo.app.data.dtos.RegistrarUsuarioDto
import com.orlo.app.data.dtos.ResultadoFirestoreDto
import com.orlo.app.data.dtos.UsuarioFirestoreDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UsuarioFirestoreDataSourceImpl @Inject constructor(
    private val db: FirebaseFirestore
) : UsuarioRemoteDataSource {

    private fun usuario(userId: String) = db.collection(USERS).document(userId)

    // El documento del usuario usa el UID de Authentication como id
    override suspend fun registrarUsuario(userId: String, usuario: RegistrarUsuarioDto): Unit {
        usuario(userId).set(usuario).await()
    }

    override suspend fun getUsuario(userId: String): UsuarioFirestoreDto? {
        val respuesta = usuario(userId).get().await()
        val usuario = respuesta.toObject(UsuarioFirestoreDto::class.java) ?: return null
        return usuario.copy(id = respuesta.id)
    }

    // merge y no update: si el usuario se registro y el documento no alcanzo a crearse, el perfil
    // lo crea; y los campos que no son del perfil (resultado actual, fechas) no se tocan
    override suspend fun actualizarPerfil(userId: String, perfil: ActualizarPerfilDto): Unit {
        usuario(userId).set(perfil, SetOptions.merge()).await()
    }

    // El historial y el resultado actual se escriben juntos: o quedan los dos o ninguno
    override suspend fun guardarResultado(userId: String, resultado: ResultadoFirestoreDto): String {
        val resultadoRef = usuario(userId).collection(RESULTS).document()
        val conId = resultado.copy(id = resultadoRef.id)

        db.batch()
            .set(resultadoRef, conId)
            .set(usuario(userId), mapOf("currentResult" to conId), SetOptions.merge())
            .commit()
            .await()

        return resultadoRef.id
    }

    override suspend fun getResultados(userId: String): List<ResultadoFirestoreDto> {
        val respuesta = usuario(userId).collection(RESULTS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
        return respuesta.documents.mapNotNull { doc ->
            doc.toObject(ResultadoFirestoreDto::class.java)?.copy(id = doc.id)
        }
    }

    override suspend fun getProgresoLecciones(userId: String): List<ProgresoLeccionFirestoreDto> {
        val respuesta = usuario(userId).collection(LESSON_PROGRESS).get().await()
        return respuesta.documents.mapNotNull { it.toObject(ProgresoLeccionFirestoreDto::class.java) }
    }

    override suspend fun guardarProgresoLeccion(userId: String, progreso: ProgresoLeccionFirestoreDto): Unit {
        usuario(userId).collection(LESSON_PROGRESS)
            .document(progreso.lessonId.toString())
            .set(progreso)
            .await()
    }

    // Borrar un documento no borra sus subcolecciones: se borran una por una y al final el usuario.
    // Los mensajes estan dentro de cada conversacion, un nivel mas abajo
    override suspend fun borrarDatosDelUsuario(userId: String): Unit {
        val usuarioRef = usuario(userId)

        val conversaciones = usuarioRef.collection(CONVERSATIONS).get().await()
        conversaciones.documents.forEach { conversacion ->
            borrarEnLotes(conversacion.reference.collection(MESSAGES).get().await().documents.map { it.reference })
        }

        listOf(RESULTS, LESSON_PROGRESS, GARMENTS, OUTFITS, CONVERSATIONS).forEach { nombre ->
            borrarEnLotes(usuarioRef.collection(nombre).get().await().documents.map { it.reference })
        }

        usuarioRef.delete().await()
    }

    private suspend fun borrarEnLotes(referencias: List<DocumentReference>) {
        referencias.chunked(MAX_BATCH).forEach { parte ->
            val batch = db.batch()
            parte.forEach { batch.delete(it) }
            batch.commit().await()
        }
    }
}
