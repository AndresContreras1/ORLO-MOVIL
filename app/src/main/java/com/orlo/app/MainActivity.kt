package com.orlo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.orlo.app.ui.theme.OrloTheme
import dagger.hilt.android.AndroidEntryPoint

// Donde inicia la aplicacion: una sola actividad con todo el NavHost adentro
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OrloTheme {
                OrloApp()
            }
        }
    }
}
