package com.orlo.app.data.datasource

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

// Solo consultas contra Firebase Authentication. La logica de que salio bien o mal vive en el
// repositorio
class AuthRemoteDataSource @Inject constructor(
    private val auth: FirebaseAuth
) {

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).await()
    }

    // La foto tambien queda en la cuenta de Authentication, para quien lea el usuario de Auth
    suspend fun actualizarFoto(photoUrl: String) {
        val cambio = UserProfileChangeRequest.Builder().setPhotoUri(Uri.parse(photoUrl)).build()
        auth.currentUser?.updateProfile(cambio)?.await()
    }

    // No lleva suspend: solo borra los datos de sesion del celular, no va a la red
    fun signOut() {
        auth.signOut()
    }
}
