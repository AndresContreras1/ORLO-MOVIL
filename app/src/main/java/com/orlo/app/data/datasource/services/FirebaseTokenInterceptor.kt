package com.orlo.app.data.datasource.services

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

// Agrega el token de Firebase del usuario a cada peticion al backend: Authorization: Bearer <token>.
// El backend lo valida y asi sabe quien pide los datos, sin que la app envie el uid. Corre en el
// hilo de OkHttp (no en el principal), por eso puede esperar el token con Tasks.await
class FirebaseTokenInterceptor @Inject constructor(
    private val auth: FirebaseAuth
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val usuario = auth.currentUser
        val peticion = chain.request()

        // Sin sesion la peticion sale sin token y el backend responde 401
        if (usuario == null) return chain.proceed(peticion)

        val token = try {
            Tasks.await(usuario.getIdToken(false)).token
        } catch (e: Exception) {
            Log.d("FirebaseTokenInterceptor", "No se obtuvo el token: ${e.message}")
            null
        }

        // La peticion no se modifica: se crea una copia con el encabezado
        val conToken = if (token == null) peticion else peticion.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(conToken)
    }
}
