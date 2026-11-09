package com.orlo.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.orlo.app.R
import com.orlo.app.data.model.CuentaUi
import com.orlo.app.data.model.EstiloUi
import com.orlo.app.data.model.ResultadoUi
import com.orlo.app.ui.screens.home.components.TarjetaDeSeccion
import com.orlo.app.ui.screens.result.components.TarjetaDeEstilo
import com.orlo.app.ui.theme.OrloTheme
import com.orlo.app.ui.utils.AppButton
import com.orlo.app.ui.utils.ErrorBox
import com.orlo.app.ui.utils.LoadingBox

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    onAprende: () -> Unit,
    onAsesor: () -> Unit,
    onExplora: () -> Unit,
    onArmario: () -> Unit,
    onCuenta: () -> Unit,
    onPremium: () -> Unit,
    onCuestionario: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by homeViewModel.uiState.collectAsState()

    // Recarga al volver a la pestana
    LaunchedEffect(Unit) {
        homeViewModel.cargar()
    }

    val error = state.errorMessageRes
    when {
        state.isLoading -> LoadingBox(modifier = modifier)
        error != null -> ErrorBox(messageRes = error, onRetry = { homeViewModel.cargar() }, modifier = modifier)
        else -> HomeScreenContent(
            nombre = state.nombre,
            resultado = state.resultado,
            cuenta = state.cuenta,
            onAprende = onAprende,
            onAsesor = onAsesor,
            onExplora = onExplora,
            onArmario = onArmario,
            onCuenta = onCuenta,
            onPremium = onPremium,
            onCuestionario = onCuestionario,
            modifier = modifier,
        )
    }
}

@Composable
fun HomeScreenContent(
    nombre: String,
    resultado: ResultadoUi?,
    cuenta: CuentaUi?,
    onAprende: () -> Unit,
    onAsesor: () -> Unit,
    onExplora: () -> Unit,
    onArmario: () -> Unit,
    onCuenta: () -> Unit,
    onPremium: () -> Unit,
    onCuestionario: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Mientras no se sepa si es premium no se ponen candados
    val bloqueada = cuenta != null && !cuenta.premium

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .testTag("homeScreen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1F)) {
                Text(
                    text = stringResource(R.string.hola_nombre, nombre),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = stringResource(R.string.inicio_subtitulo),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onCuenta, modifier = Modifier.testTag("btnCuenta")) {
                Icon(imageVector = Icons.Outlined.AccountCircle, contentDescription = stringResource(R.string.cuenta_titulo))
            }
        }

        if (resultado != null) {
            TarjetaDeEstilo(resultado = resultado, mostrarDescripcion = false)
        } else {
            AppButton(texto = stringResource(R.string.descubrir_mi_estilo), onClick = onCuestionario)
        }

        if (bloqueada) {
            Card(
                onClick = onPremium,
                modifier = Modifier.fillMaxWidth().testTag("bannerPremium"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = stringResource(R.string.inicio_premium_titulo), style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = stringResource(R.string.inicio_premium_texto),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Text(text = stringResource(R.string.que_sigue), style = MaterialTheme.typography.titleLarge)

        TarjetaDeSeccion(
            icono = Icons.Filled.School,
            titulo = stringResource(R.string.nav_aprende),
            descripcion = stringResource(R.string.inicio_aprende),
            // Aprende tiene una leccion de muestra gratis: no lleva candado
            bloqueada = false,
            onClick = onAprende,
        )
        TarjetaDeSeccion(
            icono = Icons.AutoMirrored.Filled.Chat,
            titulo = stringResource(R.string.nav_asesor),
            descripcion = if (cuenta != null && cuenta.premium) {
                stringResource(R.string.inicio_asesor_mensajes, cuenta.mensajesRestantes)
            } else {
                stringResource(R.string.inicio_asesor)
            },
            bloqueada = bloqueada,
            onClick = onAsesor,
        )
        TarjetaDeSeccion(
            icono = Icons.Filled.Checkroom,
            titulo = stringResource(R.string.nav_armario),
            descripcion = stringResource(R.string.inicio_armario),
            bloqueada = bloqueada,
            onClick = onArmario,
        )
        TarjetaDeSeccion(
            icono = Icons.Filled.Explore,
            titulo = stringResource(R.string.nav_explora),
            descripcion = stringResource(R.string.inicio_explora),
            bloqueada = bloqueada,
            onClick = onExplora,
        )
    }
}

@Composable
@Preview(showBackground = true, widthDp = 393, heightDp = 852)
fun HomeScreenPreview() {
    OrloTheme {
        HomeScreenContent(
            nombre = "Laura",
            resultado = ResultadoUi(principal = EstiloUi(nombre = "Urbano", paleta = listOf("#2B2622", "#3D5A80")), secundario = EstiloUi(nombre = "Creativo")),
            cuenta = CuentaUi(premium = false),
            onAprende = {}, onAsesor = {}, onExplora = {}, onArmario = {}, onCuenta = {}, onPremium = {}, onCuestionario = {},
        )
    }
}
