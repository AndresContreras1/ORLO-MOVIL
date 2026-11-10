package com.orlo.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import com.orlo.app.notificaciones.OrloMessagingService
import com.orlo.app.ui.theme.OrloTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // La leccion que trae la notificacion que se toco; la navegacion la abre y la limpia
    private val leccionPendiente = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        leerLeccion(intent)
        setContent {
            OrloTheme {
                OrloApp(
                    leccionPendiente = leccionPendiente.value,
                    onLeccionAbierta = { leccionPendiente.value = null },
                )
            }
        }
    }

    // Con la app abierta, tocar la notificacion llega aqui en vez de crear otra actividad
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        leerLeccion(intent)
    }

    private fun leerLeccion(intent: Intent?) {
        intent?.getStringExtra(OrloMessagingService.EXTRA_LECCION)?.let { leccionPendiente.value = it }
    }
}
