package com.orlo.app.ui.screens.profilesetup.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.orlo.app.ui.theme.OrloTheme

// Un dato del perfil: el titulo, una linea que explica para que se pide y el selector
@Composable
fun SeccionDelPerfil(
    titulo: String,
    paraQue: String,
    modifier: Modifier = Modifier,
    contenido: @Composable () -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = titulo, style = MaterialTheme.typography.titleMedium)
        Text(
            text = paraQue,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        contenido()
    }
}

@Composable
@Preview(showBackground = true)
fun SeccionDelPerfilPreview() {
    OrloTheme {
        SeccionDelPerfil(titulo = "Clima habitual", paraQue = "Para sugerirte prendas según el clima.") {
            Text(text = "Cálido · Templado · Frío")
        }
    }
}
