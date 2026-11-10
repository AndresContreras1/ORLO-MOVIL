package com.orlo.app.data.datasource

import okhttp3.Interceptor
import okhttp3.Response

// En debug la app usa el emulador de Firebase que corre en el computador. Desde el emulador de
// Android el computador es 10.0.2.2, pero la web y el seed lo ven como 127.0.0.1. Las URLs de las
// fotos se guardan siempre con 127.0.0.1, para que la app y la web vean las mismas fotos, y la app
// las cambia a 10.0.2.2 solo al descargarlas
object EmuladorFirebase {

    const val HOST_DESDE_ANDROID = "10.0.2.2"
    private const val HOST_DESDE_EL_COMPUTADOR = "127.0.0.1"

    // La URL que entrega Storage desde el celular, como la guardan los demas clientes
    fun urlParaGuardar(url: String): String =
        url.replace("//$HOST_DESDE_ANDROID:", "//$HOST_DESDE_EL_COMPUTADOR:")

    // Para el cliente de imagenes: las fotos del emulador se piden al computador
    val interceptorDeFotos = Interceptor { chain ->
        val pedido = chain.request()
        val host = pedido.url.host
        val respuesta: Response = if (host == HOST_DESDE_EL_COMPUTADOR || host == "localhost") {
            chain.proceed(pedido.newBuilder().url(pedido.url.newBuilder().host(HOST_DESDE_ANDROID).build()).build())
        } else {
            chain.proceed(pedido)
        }
        respuesta
    }
}
