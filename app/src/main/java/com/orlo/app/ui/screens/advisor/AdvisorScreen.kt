package com.orlo.app.ui.screens.advisor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.AddComment
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
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
import android.widget.Toast
import com.orlo.app.R
import com.orlo.app.data.model.AccionRapida
import com.orlo.app.data.model.CuentaUi
import com.orlo.app.data.model.MensajeChatUi
import com.orlo.app.data.model.RolMensaje
import com.orlo.app.ui.screens.advisor.components.BurbujaDeMensaje
import com.orlo.app.ui.theme.OrloTheme
import com.orlo.app.ui.utils.ErrorBox
import com.orlo.app.ui.utils.ErrorMessage
import com.orlo.app.ui.utils.LoadingBox
import com.orlo.app.ui.utils.PremiumGate
import com.orlo.app.ui.utils.ScreenTitle

@Composable
fun AdvisorScreen(
    advisorViewModel: AdvisorViewModel,
    conversacionId: String?,
    onPremium: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by advisorViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(conversacionId) {
        advisorViewModel.cargar(conversacionId)
    }

    // El aviso corto de "Outfit guardado en Mi armario"
    val aviso = state.avisoRes
    LaunchedEffect(aviso) {
        if (aviso != null) {
            Toast.makeText(context, context.getString(aviso), Toast.LENGTH_SHORT).show()
            advisorViewModel.avisoMostrado()
        }
    }

    val errorCarga = state.errorCargaRes
    when {
        state.isLoading -> LoadingBox(modifier = modifier)
        errorCarga != null -> ErrorBox(messageRes = errorCarga, onRetry = { advisorViewModel.cargar(conversacionId) }, modifier = modifier)
        !state.premium -> Column(modifier = modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            ScreenTitle(titulo = stringResource(R.string.asesor_titulo), subtitulo = stringResource(R.string.asesor_subtitulo))
            PremiumGate(
                titulo = stringResource(R.string.asesor_premium_titulo),
                descripcion = stringResource(R.string.asesor_premium_texto),
                onVerPlanes = onPremium,
            )
        }
        else -> {
            val textoCasual = stringResource(R.string.accion_mas_casual)
            val textoFormal = stringResource(R.string.accion_mas_formal)
            val textoOtra = stringResource(R.string.accion_otra_opcion)
            AdvisorScreenContent(
                state = state,
                onTextoChange = { advisorViewModel.updateTexto(it) },
                onEnviar = { advisorViewModel.enviar() },
                onEjemplo = { advisorViewModel.enviarEjemplo(it) },
                onAccion = { accion ->
                    val texto = when (accion) {
                        AccionRapida.MAS_CASUAL -> textoCasual
                        AccionRapida.MAS_FORMAL -> textoFormal
                        AccionRapida.OTRA_OPCION -> textoOtra
                    }
                    advisorViewModel.accionRapida(accion, texto)
                },
                onReintentar = { advisorViewModel.reintentar() },
                onNuevaConversacion = { advisorViewModel.nuevaConversacion() },
                onGuardarOutfit = { mensaje, indice -> advisorViewModel.guardarOutfit(mensaje, indice) },
                modifier = modifier,
            )
        }
    }
}

@Composable
fun AdvisorScreenContent(
    state: AdvisorState,
    onTextoChange: (String) -> Unit,
    onEnviar: () -> Unit,
    onEjemplo: (String) -> Unit,
    onAccion: (AccionRapida) -> Unit,
    onReintentar: () -> Unit,
    onNuevaConversacion: () -> Unit,
    onGuardarOutfit: (MensajeChatUi, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val ejemplos = listOf(
        stringResource(R.string.ejemplo_universidad),
        stringResource(R.string.ejemplo_pantalon),
        stringResource(R.string.ejemplo_salida),
        stringResource(R.string.ejemplo_elegante),
        stringResource(R.string.ejemplo_colores),
        stringResource(R.string.ejemplo_basico),
    )

    // Cada mensaje nuevo baja el chat hasta el final
    LaunchedEffect(state.mensajes.size, state.enviando) {
        val total = state.mensajes.size + if (state.enviando) 1 else 0
        if (total > 0) listState.animateScrollToItem(total)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .testTag("advisorScreen"),
    ) {
        Row(
            modifier = Modifier.padding(start = 20.dp, end = 8.dp, top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ScreenTitle(
                titulo = stringResource(R.string.asesor_titulo),
                subtitulo = state.cuenta?.let { stringResource(R.string.mensajes_restantes, it.mensajesRestantes) },
                modifier = Modifier.weight(1F),
            )
            IconButton(onClick = onNuevaConversacion, modifier = Modifier.testTag("btnNuevaConversacion")) {
                Icon(imageVector = Icons.Outlined.AddComment, contentDescription = stringResource(R.string.nueva_conversacion))
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1F),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.aviso_ia_externa),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (state.mensajes.isEmpty()) {
                item {
                    Text(text = stringResource(R.string.prueba_preguntar), style = MaterialTheme.typography.titleMedium)
                }
                items(ejemplos) { ejemplo ->
                    SuggestionChip(onClick = { onEjemplo(ejemplo) }, label = { Text(text = ejemplo) })
                }
            }
            itemsIndexed(state.mensajes, key = { indice, mensaje -> mensaje.id.ifBlank { "m$indice" } }) { indice, mensaje ->
                val guardados = state.outfitsGuardados
                    .filter { it.startsWith("${mensaje.id}-") }
                    .mapNotNull { it.substringAfterLast("-").toIntOrNull() }
                    .toSet()
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BurbujaDeMensaje(
                        mensaje = mensaje,
                        guardados = guardados,
                        onGuardarOutfit = { onGuardarOutfit(mensaje, it) },
                    )
                    // Los botones rapidos solo debajo de la ultima respuesta
                    val esUltima = indice == state.mensajes.lastIndex
                    if (esUltima && mensaje.rol == RolMensaje.ASESOR && !state.enviando) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(AccionRapida.entries) { accion ->
                                AssistChip(
                                    onClick = { onAccion(accion) },
                                    label = {
                                        Text(
                                            text = stringResource(
                                                when (accion) {
                                                    AccionRapida.MAS_CASUAL -> R.string.accion_mas_casual
                                                    AccionRapida.MAS_FORMAL -> R.string.accion_mas_formal
                                                    AccionRapida.OTRA_OPCION -> R.string.accion_otra_opcion
                                                }
                                            )
                                        )
                                    },
                                    modifier = Modifier.testTag("accion_${accion.name}"),
                                )
                            }
                        }
                    }
                }
            }
            if (state.enviando) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(modifier = Modifier.padding(4.dp), strokeWidth = 2.dp)
                        Text(text = stringResource(R.string.asesor_pensando), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            if (state.cercaDelLimite) {
                Text(
                    text = stringResource(R.string.aviso_cerca_del_limite, state.cuenta?.mensajesRestantes ?: 0),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            ErrorMessage(messageRes = state.errorMessageRes)
            if (state.pendiente != null && !state.enviando) {
                TextButton(onClick = onReintentar, modifier = Modifier.testTag("btnReintentarPregunta")) {
                    Text(text = stringResource(R.string.reintentar))
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = state.texto,
                    onValueChange = onTextoChange,
                    placeholder = {
                        Text(text = stringResource(if (state.sinMensajes) R.string.sin_mensajes_hoy else R.string.escribe_tu_duda))
                    },
                    enabled = !state.sinMensajes,
                    maxLines = 4,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .weight(1F)
                        .testTag("txtPregunta"),
                )
                IconButton(
                    onClick = onEnviar,
                    enabled = state.texto.isNotBlank() && !state.enviando && !state.sinMensajes,
                    modifier = Modifier.testTag("btnEnviar"),
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.enviar))
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 393, heightDp = 852)
fun AdvisorScreenPreview() {
    OrloTheme {
        AdvisorScreenContent(
            state = AdvisorState(isLoading = false, cuenta = CuentaUi(premium = true, limiteDiario = 20, mensajesRestantes = 2)),
            onTextoChange = {}, onEnviar = {}, onEjemplo = {}, onAccion = {}, onReintentar = {},
            onNuevaConversacion = {}, onGuardarOutfit = { _, _ -> },
        )
    }
}
