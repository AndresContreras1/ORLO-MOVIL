package com.orlo.app

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.tasks.await

// Las pruebas con emulador usan el Firebase CLI que corre en el computador (firebase emulators:start).
// Conectar dos veces lanza IllegalStateException: por eso va dentro de try/catch
object EmuladorDePrueba {

    fun conectar() {
        try {
            Firebase.auth.useEmulator("10.0.2.2", 9099)
            Firebase.firestore.useEmulator("10.0.2.2", 8080)
            Firebase.storage.useEmulator("10.0.2.2", 9199)
        } catch (e: IllegalStateException) {
            // Ya estaba conectado por otra prueba
        }
    }

    // Un correo distinto en cada corrida: las pruebas no dependen de lo que dejo la anterior
    fun correoNuevo(): String = "prueba${System.currentTimeMillis()}@orlo.test"

    // Crea una cuenta de prueba e inicia sesion; retorna el uid
    suspend fun crearUsuario(correo: String = correoNuevo()): String {
        Firebase.auth.createUserWithEmailAndPassword(correo, "123456").await()
        return Firebase.auth.currentUser!!.uid
    }

    suspend fun borrarUsuarioActual() {
        Firebase.auth.currentUser?.delete()?.await()
        Firebase.auth.signOut()
    }
}
