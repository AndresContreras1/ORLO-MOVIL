package com.orlo.app.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// El titulo de una pantalla con una linea opcional debajo que explica para que sirve
@Composable
fun ScreenTitle(
    titulo: String,
    modifier: Modifier = Modifier,
    subtitulo: String? = null,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (!subtitulo.isNullOrBlank()) {
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// Lo que se ve al tocar una funcion premium sin serlo: que incluye y un boton para ver el plan.
// Nunca un error ni la funcion oculta
@Composable
fun PremiumGate(
    titulo: String,
    descripcion: String,
    onVerPlanes: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("premiumGate"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            AppButton(
                texto = stringResource(R.string.ver_plan_premium),
                onClick = onVerPlanes,
                modifier = Modifier.testTag("btnVerPremium"),
            )
        }
    }
}

// Confirmacion antes de una accion que no se puede deshacer, como borrar
@Composable
fun ConfirmDialog(
    titulo: String,
    texto: String,
    confirmar: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        modifier = modifier,
        title = { Text(text = titulo) },
        text = { Text(text = texto) },
        confirmButton = {
            TextButton(onClick = onConfirmar, modifier = Modifier.testTag("btnConfirmar")) {
                Text(text = confirmar, color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text(text = stringResource(R.string.cancelar))
            }
        },
    )
}

// Fecha corta para listas e historial, por ejemplo "9 nov 2026"
fun formatearFecha(fecha: Long): String =
    SimpleDateFormat("d MMM yyyy", Locale.forLanguageTag("es-CO")).format(Date(fecha))

@Composable
@Preview(showBackground = true)
fun PremiumGatePreview() {
    OrloTheme {
        PremiumGate(
            titulo = "Asesor IA",
            descripcion = "Pregunta qué ponerte y recibe opciones con tu estilo.",
            onVerPlanes = {},
        )
    }
}

@Composable
@Preview(showBackground = true)
fun ScreenTitlePreview() {
    OrloTheme {
        ScreenTitle(titulo = "Mi armario", subtitulo = "Tus prendas, outfits e historial")
    }
}

@Composable
@Preview(showBackground = true)
fun ConfirmDialogPreview() {
    OrloTheme {
        ConfirmDialog(
            titulo = "Borrar outfit",
            texto = "Se borra \"Lunes de clase\" de tu armario.",
            confirmar = "Borrar",
            onConfirmar = {},
            onCancelar = {},
        )
    }
}
