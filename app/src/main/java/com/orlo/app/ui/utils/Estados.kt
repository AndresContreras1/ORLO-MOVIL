package com.orlo.app.ui.utils

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.orlo.app.R
import com.orlo.app.ui.theme.OrloTheme

// Los tres casos de cada pantalla: cargando, error o contenido

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.testTag("loading"))
    }
}

// El error que ocupa la pantalla cuando no hay nada que mostrar. Todo error trae una accion para
// reintentar
@Composable
fun ErrorBox(
    @StringRes messageRes: Int,
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            Text(
                text = stringResource(messageRes),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("errorMessage"),
            )
            if (onRetry != null) {
                TextButton(onClick = onRetry, modifier = Modifier.testTag("btnRetry")) {
                    Text(text = stringResource(R.string.reintentar))
                }
            }
        }
    }
}

// El error debajo de un formulario: aparece y se desvanece cuando cambia el estado
@Composable
fun ErrorMessage(
    @StringRes messageRes: Int?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = messageRes != null,
        modifier = modifier,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Text(
            text = if (messageRes != null) stringResource(messageRes) else "",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.testTag("errorMessage"),
        )
    }
}

// Lo que se muestra cuando una lista esta vacia
@Composable
fun EmptyMessage(
    @StringRes messageRes: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(messageRes),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        modifier = modifier.padding(vertical = 24.dp, horizontal = 16.dp),
    )
}

@Composable
@Preview(showBackground = true)
fun ErrorBoxPreview() {
    OrloTheme {
        ErrorBox(messageRes = R.string.error_sin_conexion, onRetry = {})
    }
}
