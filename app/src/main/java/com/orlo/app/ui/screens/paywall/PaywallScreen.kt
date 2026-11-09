package com.orlo.app.ui.screens.paywall

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.orlo.app.R
import com.orlo.app.ui.theme.OrloTheme
import com.orlo.app.ui.utils.AppButton
import com.orlo.app.ui.utils.AppOutlinedButton
import com.orlo.app.ui.utils.ScreenTitle

// Pantallas 6 y 7: que incluye premium, el precio, la renovacion y como cancelar, antes de cobrar.
// El cobro con la tienda de aplicaciones todavia no esta conectado: por ahora el acceso premium lo
// activa el equipo desde el panel, y el boton lo explica en vez de fallar
@Composable
fun PaywallScreen(
    onAhoraNo: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Estado solo de la vista: si el aviso esta abierto
    var mostrarAviso by remember { mutableStateOf(false) }

    PaywallScreenContent(
        onSuscribirme = { mostrarAviso = true },
        onAhoraNo = onAhoraNo,
        modifier = modifier,
    )

    if (mostrarAviso) {
        AlertDialog(
            onDismissRequest = { mostrarAviso = false },
            title = { Text(text = stringResource(R.string.pago_pronto_titulo)) },
            text = { Text(text = stringResource(R.string.pago_pronto_texto)) },
            confirmButton = {
                TextButton(onClick = { mostrarAviso = false }) { Text(text = stringResource(R.string.entendido)) }
            },
        )
    }
}

@Composable
fun PaywallScreenContent(
    onSuscribirme: () -> Unit,
    onAhoraNo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val beneficios = listOf(
        R.string.premium_beneficio_aprende,
        R.string.premium_beneficio_asesor,
        R.string.premium_beneficio_armario,
        R.string.premium_beneficio_explora,
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .testTag("paywallScreen"),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        ScreenTitle(
            titulo = stringResource(R.string.premium_titulo),
            subtitulo = stringResource(R.string.premium_subtitulo),
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            beneficios.forEach { beneficio ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(text = stringResource(beneficio), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        Text(text = stringResource(R.string.premium_precio), style = MaterialTheme.typography.headlineSmall)
        Text(
            text = stringResource(R.string.premium_renovacion),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AppButton(
            texto = stringResource(R.string.suscribirme),
            onClick = onSuscribirme,
            modifier = Modifier.testTag("btnSuscribirme"),
        )
        AppOutlinedButton(
            texto = stringResource(R.string.ahora_no),
            onClick = onAhoraNo,
            modifier = Modifier.testTag("btnAhoraNo"),
        )
    }
}

@Composable
@Preview(showBackground = true, widthDp = 393, heightDp = 852)
fun PaywallScreenPreview() {
    OrloTheme {
        PaywallScreenContent(onSuscribirme = {}, onAhoraNo = {})
    }
}
