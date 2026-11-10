package com.orlo.app

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.tasks.await

// Las pruebas con emulador usan el Firebase CLI que corre en el computador (firebase emulators:start).
// Conectar dos veces lanza IllegalStateException: cada servicio va en su propio try, asi si uno ya
// estaba conectado los demas se conectan igual
object EmuladorDePrueba {

    private const val HOST = "10.0.2.2"

    fun conectar() {
        conectarUno { Firebase.auth.useEmulator(HOST, 9099) }
        conectarUno { Firebase.firestore.useEmulator(HOST, 8080) }
        conectarUno { Firebase.storage.useEmulator(HOST, 9199) }
    }

    private fun conectarUno(conectar: () -> Unit) {
        try {
            conectar()
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
