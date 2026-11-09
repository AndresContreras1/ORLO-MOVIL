package com.orlo.app.data.repository

import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.orlo.app.data.datasource.AuthRemoteDataSource
import javax.inject.Inject

// Unifica la autenticacion y expone solo lo que la capa de UI necesita
class AuthRepository @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource
) {

    val usuarioId: String?
        get() = authRemoteDataSource.currentUser?.uid

    val correo: String
        get() = authRemoteDataSource.currentUser?.email ?: ""

    val haySesion: Boolean
        get() = authRemoteDataSource.currentUser != null

    suspend fun signIn(email: String, password: String): Result<Unit> {
        return try {
            authRemoteDataSource.signIn(email = email.trim(), password = password)
            Result.success(Unit)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Log.d("AuthRepository", "signIn: ${e.message}")
            Result.failure(CredencialesInvalidasException())
        } catch (e: FirebaseAuthInvalidUserException) {
            Log.d("AuthRepository", "signIn: ${e.message}")
            Result.failure(CorreoNoRegistradoException())
        } catch (e: FirebaseNetworkException) {
            Log.d("AuthRepository", "signIn: ${e.message}")
            Result.failure(SinConexionException())
        } catch (e: FirebaseTooManyRequestsException) {
            Log.d("AuthRepository", "signIn: ${e.message}")
            Result.failure(DemasiadosIntentosException())
        } catch (e: Exception) {
            Log.d("AuthRepository", "signIn: ${e.message}")
            Result.failure(ErrorDeInicioSesionException())
        }
    }

    suspend fun signUp(email: String, password: String): Result<Unit> {
        return try {
            authRemoteDataSource.signUp(email = email.trim(), password = password)
            Result.success(Unit)
        } catch (e: FirebaseAuthUserCollisionException) {
            Log.d("AuthRepository", "signUp: ${e.message}")
            Result.failure(CorreoYaRegistradoException())
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Log.d("AuthRepository", "signUp: ${e.message}")
            Result.failure(CredencialesInvalidasException())
        } catch (e: FirebaseNetworkException) {
            Log.d("AuthRepository", "signUp: ${e.message}")
            Result.failure(SinConexionException())
        } catch (e: FirebaseTooManyRequestsException) {
            Log.d("AuthRepository", "signUp: ${e.message}")
            Result.failure(DemasiadosIntentosException())
        } catch (e: Exception) {
            Log.d("AuthRepository", "signUp: ${e.message}")
            Result.failure(ErrorDeRegistroException())
        }
    }

    fun signOut() {
        authRemoteDataSource.signOut()
    }
}
