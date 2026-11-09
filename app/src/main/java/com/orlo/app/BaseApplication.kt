package com.orlo.app

import android.app.Application
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import com.orlo.app.data.datasource.EmuladorFirebase
import com.orlo.app.data.datasource.EmuladorFirebase.HOST_DESDE_ANDROID
import com.orlo.app.notificaciones.OrloMessagingService
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient

// Punto de entrada de Hilt: aqui se crean los objetos del modulo al arrancar la app
@HiltAndroidApp
class BaseApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        OrloMessagingService.crearCanal(this)

        // En modo debug la app usa el emulador de Firebase (Firebase CLI) que corre en el computador:
        // desde el emulador de Android el computador es 10.0.2.2. BuildConfig es el de la app
        if (BuildConfig.DEBUG) {
            Firebase.auth.useEmulator(HOST_DESDE_ANDROID, 9099)
            Firebase.firestore.useEmulator(HOST_DESDE_ANDROID, 8080)
            Firebase.storage.useEmulator(HOST_DESDE_ANDROID, 9199)
            Log.d("BaseApplication", "Usando el emulador de Firebase")
        }
    }

    // El cargador de imagenes de Coil para toda la app. Las fotos del catalogo vienen de Wikimedia,
    // que rechaza las descargas sin un User-Agent propio. En debug las fotos del emulador de Storage
    // se piden al computador (ver EmuladorFirebase)
    override fun newImageLoader(): ImageLoader {
        val cliente = OkHttpClient.Builder().apply {
            addInterceptor { chain ->
                chain.proceed(chain.request().newBuilder().header("User-Agent", "ORLO/${BuildConfig.VERSION_NAME} (Android)").build())
            }
            if (BuildConfig.DEBUG) addInterceptor(EmuladorFirebase.interceptorDeFotos)
        }.build()
        return ImageLoader.Builder(this)
            .okHttpClient(cliente)
            .crossfade(true)
            .build()
    }
}
