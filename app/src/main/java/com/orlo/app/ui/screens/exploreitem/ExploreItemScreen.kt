package com.orlo.app.ui.screens.exploreitem

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.orlo.app.R
import com.orlo.app.data.local.LocalPreviewProvider
import com.orlo.app.data.model.IdeaCombinarUi
import com.orlo.app.data.model.ItemExploraUi
import com.orlo.app.data.model.TiendaUi
import com.orlo.app.ui.theme.OrloTheme
import com.orlo.app.ui.utils.AppButton
import com.orlo.app.ui.utils.AppOutlinedButton
import com.orlo.app.ui.utils.EmptyMessage
import com.orlo.app.ui.utils.ErrorBox
import com.orlo.app.ui.utils.ErrorMessage
import com.orlo.app.ui.utils.LoadingBox
import com.orlo.app.ui.utils.OrloAsyncImage

@Composable
fun ExploreItemScreen(
    exploreItemViewModel: ExploreItemViewModel,
    itemId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by exploreItemViewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(itemId) {
        exploreItemViewModel.cargar(itemId)
    }

    Column(modifier = modifier.fillMaxSize().testTag("exploreItemScreen")) {
        IconButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp, top = 8.dp)) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.atras))
        }
        val item = state.item
        val errorCarga = state.errorCargaRes
        when {
            state.isLoading -> LoadingBox()
            errorCarga != null -> ErrorBox(messageRes = errorCarga, onRetry = { exploreItemViewModel.cargar(itemId) })
            item != null -> ExploreItemScreenContent(
                item = item,
                tiendas = state.tiendas,
                guardado = state.guardado,
                errorMessageRes = state.errorMessageRes,
                onGuardar = { exploreItemViewModel.alternarGuardado() },
                // Abrir la tienda es una accion de la vista: va al navegador del celular
                onTienda = { uriHandler.openUri(it.url) },
            )
        }
    }
}

@Composable
fun ExploreItemScreenContent(
    item: ItemExploraUi,
    tiendas: List<TiendaUi>,
    guardado: Boolean,
    errorMessageRes: Int?,
    onGuardar: () -> Unit,
    onTienda: (TiendaUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OrloAsyncImage(
                    url = item.imagenUrl,
                    contentDescription = item.nombre,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1F)
                        .clip(MaterialTheme.shapes.large),
                )
                if (item.credito.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.foto_de, item.credito),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        item {
            Text(text = item.nombre, style = MaterialTheme.typography.headlineSmall)
        }
        if (item.porQueTeQueda.isNotBlank()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = stringResource(R.string.por_que_te_queda), style = MaterialTheme.typography.titleMedium)
                    Text(text = item.porQueTeQueda, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        item {
            ErrorMessage(messageRes = errorMessageRes)
            if (guardado) {
                AppOutlinedButton(
                    texto = stringResource(R.string.quitar_de_guardados),
                    onClick = onGuardar,
                    modifier = Modifier.testTag("btnGuardarItem"),
                )
            } else {
                AppButton(
                    texto = stringResource(R.string.guardar_para_despues),
                    onClick = onGuardar,
                    modifier = Modifier.testTag("btnGuardarItem"),
                )
            }
        }
        if (item.ideas.isNotEmpty()) {
            item { Text(text = stringResource(R.string.como_combinarla), style = MaterialTheme.typography.titleMedium) }
            items(item.ideas, key = { it.contexto }) { idea ->
                IdeaCard(idea = idea)
            }
        }
        item { Text(text = stringResource(R.string.donde_encontrarla), style = MaterialTheme.typography.titleMedium) }
        if (tiendas.isEmpty()) {
            item { EmptyMessage(messageRes = R.string.sin_tiendas) }
        }
        items(tiendas, key = { it.id }) { tienda ->
            TiendaCard(tienda = tienda, onClick = { onTienda(tienda) })
        }
    }
}

// Una forma de combinarla segun la ocasion: casual, universidad o elegante
@Composable
fun IdeaCard(
    idea: IdeaCombinarUi,
    modifier: Modifier = Modifier,
) {
    val contexto = when (idea.contexto) {
        "casual" -> stringResource(R.string.contexto_casual)
        "universidad" -> stringResource(R.string.contexto_universidad)
        "elegante" -> stringResource(R.string.contexto_elegante)
        else -> idea.contexto
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = contexto, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(text = idea.prendas.joinToString(" · "), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun TiendaCard(
    tienda: TiendaUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().testTag("tienda_${tienda.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1F), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = tienda.nombre, style = MaterialTheme.typography.titleSmall)
                if (tienda.tipos.isNotEmpty()) {
                    Text(
                        text = tienda.tipos.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (tienda.afiliada) {
                    Text(text = stringResource(R.string.tienda_afiliada), style = MaterialTheme.typography.labelSmall)
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                contentDescription = stringResource(R.string.abrir_tienda, tienda.nombre),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 393, heightDp = 1200)
fun ExploreItemScreenPreview() {
    OrloTheme {
        ExploreItemScreenContent(
            item = LocalPreviewProvider.itemsExplora.first(),
            tiendas = LocalPreviewProvider.tiendas,
            guardado = false,
            errorMessageRes = null,
            onGuardar = {},
            onTienda = {},
        )
    }
}

@Composable
@Preview(showBackground = true, widthDp = 393, heightDp = 1200, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun ExploreItemScreenOscuroPreview() {
    OrloTheme {
        ExploreItemScreenContent(
            item = LocalPreviewProvider.itemsExplora.first(),
            tiendas = emptyList(),
            guardado = true,
            errorMessageRes = null,
            onGuardar = {},
            onTienda = {},
        )
    }
}
