package com.orlo.app.ui.screens.account

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.orlo.app.R
import com.orlo.app.data.local.LocalPreviewProvider
import com.orlo.app.ui.theme.OrloTheme
import com.orlo.app.ui.utils.AppOutlinedButton
import com.orlo.app.ui.utils.ConfirmDialog
import com.orlo.app.ui.utils.ErrorBox
import com.orlo.app.ui.utils.ErrorMessage
import com.orlo.app.ui.utils.FotoDePerfil
import com.orlo.app.ui.utils.LoadingBox
import com.orlo.app.ui.utils.SelectorDeFoto

// Pantalla 20: datos personales, suscripcion, aviso de IA externa, cerrar sesion y eliminar cuenta
@Composable
fun AccountScreen(
    accountViewModel: AccountViewModel,
    onBack: () -> Unit,
    onEditarDatos: () -> Unit,
    onPremium: () -> Unit,
    onSalir: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by accountViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val aviso = state.avisoRes
    LaunchedEffect(aviso) {
        if (aviso != null) {
            Toast.makeText(context, context.getString(aviso), Toast.LENGTH_SHORT).show()
            accountViewModel.avisoMostrado()
        }
    }

    // Carga al entrar y al volver de editar los datos
    LaunchedEffect(Unit) {
        accountViewModel.cargar()
    }

    LaunchedEffect(state.salir) {
        if (state.salir) onSalir()
    }

    if (state.confirmarBorrado) {
        ConfirmDialog(
            titulo = stringResource(R.string.borrar_cuenta_titulo),
            texto = stringResource(R.string.borrar_cuenta_texto),
            confirmar = stringResource(R.string.borrar_todo),
            onConfirmar = { accountViewModel.borrarCuenta() },
            onCancelar = { accountViewModel.cancelarBorrado() },
        )
    }

    Column(modifier = modifier.fillMaxSize().testTag("accountScreen")) {
        IconButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp, top = 8.dp)) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.atras))
        }
        val errorCarga = state.errorCargaRes
        if (state.isLoading) {
            LoadingBox()
        } else if (errorCarga != null) {
            ErrorBox(messageRes = errorCarga, onRetry = { accountViewModel.cargar() })
        } else {
            AccountScreenContent(
                state = state,
                onEditarDatos = onEditarDatos,
                onFoto = { accountViewModel.cambiarFoto(it) },
                onPremium = onPremium,
                onCerrarSesion = { accountViewModel.cerrarSesion() },
                onBorrarCuenta = { accountViewModel.pedirBorrado() },
            )
        }
    }
}

@Composable
fun AccountScreenContent(
    state: AccountState,
    onEditarDatos: () -> Unit,
    onFoto: (Uri) -> Unit,
    onPremium: () -> Unit,
    onCerrarSesion: () -> Unit,
    onBorrarCuenta: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = stringResource(R.string.cuenta_titulo), style = MaterialTheme.typography.headlineMedium)

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        FotoDePerfil(url = state.fotoUrl, tamano = 72.dp, modifier = Modifier.testTag("fotoPerfil"))
                        if (state.subiendoFoto) CircularProgressIndicator(modifier = Modifier.size(72.dp), strokeWidth = 3.dp)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(text = state.nombre, style = MaterialTheme.typography.titleMedium)
                        Text(text = state.correo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (state.estilo != null) {
                            Text(
                                text = stringResource(R.string.estilo_cuenta, state.estilo),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = stringResource(R.string.cantidad_prendas_cuenta, state.cantidadPrendas),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.testTag("cantidadPrendas"),
                        )
                        Text(
                            text = stringResource(R.string.lecciones_completadas_cuenta, state.leccionesCompletadas),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.testTag("leccionesCompletadas"),
                        )
                    }
                }
                SelectorDeFoto(onFoto = onFoto)
                TextButton(onClick = onEditarDatos, modifier = Modifier.testTag("btnEditarDatos")) {
                    Text(text = stringResource(R.string.ver_editar_mis_datos))
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = stringResource(R.string.suscripcion), style = MaterialTheme.typography.titleMedium)
                val cuenta = state.cuenta
                when {
                    cuenta == null -> Text(text = stringResource(R.string.suscripcion_no_disponible), style = MaterialTheme.typography.bodyMedium)
                    cuenta.premium -> {
                        Text(text = stringResource(R.string.suscripcion_premium), style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = stringResource(R.string.mensajes_de_hoy, cuenta.mensajesRestantes, cuenta.limiteDiario),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(text = stringResource(R.string.como_cancelar), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    else -> {
                        Text(text = stringResource(R.string.suscripcion_gratis), style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = onPremium) { Text(text = stringResource(R.string.ver_plan_premium)) }
                    }
                }
            }
        }

        Text(text = stringResource(R.string.privacidad), style = MaterialTheme.typography.titleMedium)
        Text(text = stringResource(R.string.privacidad_texto), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = stringResource(R.string.aviso_ia_externa), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        HorizontalDivider(thickness = 1.dp)
        ErrorMessage(messageRes = state.errorMessageRes)
        AppOutlinedButton(texto = stringResource(R.string.cerrar_sesion), onClick = onCerrarSesion, modifier = Modifier.testTag("btnCerrarSesion"))
        // La accion destructiva va con el color de error y al final, no con el acento
        TextButton(
            onClick = onBorrarCuenta,
            enabled = !state.borrando,
            modifier = Modifier.align(Alignment.CenterHorizontally).testTag("btnEliminarCuenta"),
        ) {
            Text(
                text = stringResource(if (state.borrando) R.string.eliminando_cuenta else R.string.eliminar_cuenta),
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 393, heightDp = 852)
fun AccountScreenPreview() {
    OrloTheme {
        AccountScreenContent(
            state = AccountState(
                isLoading = false,
                nombre = "Laura",
                correo = "laura@correo.com",
                cantidadPrendas = 12,
                estilo = "Elegante · clásico",
                leccionesCompletadas = 3,
                cuenta = LocalPreviewProvider.cuenta,
            ),
            onEditarDatos = {}, onFoto = {}, onPremium = {}, onCerrarSesion = {}, onBorrarCuenta = {},
        )
    }
}
