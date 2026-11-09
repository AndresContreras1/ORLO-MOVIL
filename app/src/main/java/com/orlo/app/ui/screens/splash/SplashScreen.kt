package com.orlo.app.ui.screens.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.orlo.app.ui.utils.ErrorBox
import com.orlo.app.ui.utils.LoadingBox

// Sin contenido propio: decide a donde ir y avisa a la navegacion
@Composable
fun SplashScreen(
    splashViewModel: SplashViewModel,
    onBienvenida: () -> Unit,
    onPerfil: () -> Unit,
    onCuestionario: () -> Unit,
    onInicio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by splashViewModel.uiState.collectAsState()

    LaunchedEffect(state.destino) {
        when (state.destino) {
            DestinoInicial.BIENVENIDA -> onBienvenida()
            DestinoInicial.PERFIL -> onPerfil()
            DestinoInicial.CUESTIONARIO -> onCuestionario()
            DestinoInicial.INICIO -> onInicio()
            null -> Unit
        }
    }

    Box(modifier = modifier.fillMaxSize().testTag("splashScreen")) {
        val error = state.errorMessageRes
        if (error != null) {
            ErrorBox(messageRes = error, onRetry = { splashViewModel.decidirDestino() })
        } else {
            LoadingBox()
        }
    }
}
