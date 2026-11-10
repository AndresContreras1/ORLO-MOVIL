package com.orlo.app.data.datasource

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Firebase Cloud Messaging: la app se suscribe a un tema y el backend le envia a todo el tema, sin
// guardar el token de cada celular
class MensajeriaRemoteDataSource @Inject constructor(
    private val messaging: FirebaseMessaging
) {

    suspend fun suscribirse(tema: String): Unit {
        messaging.subscribeToTopic(tema).await()
    }
}
