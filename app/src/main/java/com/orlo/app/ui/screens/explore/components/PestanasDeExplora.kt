package com.orlo.app.ui.screens.explore.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.orlo.app.R
import com.orlo.app.data.local.LocalPreviewProvider
import com.orlo.app.data.model.ItemExploraUi
import com.orlo.app.data.model.SugerenciaArmarioUi
import com.orlo.app.ui.theme.OrloTheme
import com.orlo.app.ui.utils.AppOutlinedButton
import com.orlo.app.ui.utils.EmptyMessage

// Las pestanas del catalogo (Para tu estilo, Tendencias, Complementos) y Guardados: una cuadricula
// de prendas con un aviso opcional arriba
@Composable
fun CuadriculaDeItems(
    items: List<ItemExploraUi>,
    guardados: Set<String>,
    vacio: Int,
    onItem: (ItemExploraUi) -> Unit,
    onGuardar: (ItemExploraUi) -> Unit,
    modifier: Modifier = Modifier,
    aviso: String? = null,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = modifier.fillMaxSize().testTag("cuadriculaExplora"),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (aviso != null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(text = aviso, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (items.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) { EmptyMessage(messageRes = vacio) }
        }
        items(items, key = { it.id }) { item ->
            ItemExploraCard(
                item = item,
                guardado = item.id in guardados,
                onClick = { onItem(item) },
                onGuardar = { onGuardar(item) },
                modifier = Modifier.testTag("item_${item.id}"),
            )
        }
    }
}

// Completa tu armario: cada tipo de prenda que falta, por que, y ejemplos del catalogo
@Composable
fun PestanaCompletaArmario(
    sugerencias: List<SugerenciaArmarioUi>,
    guardados: Set<String>,
    onItem: (ItemExploraUi) -> Unit,
    onGuardar: (ItemExploraUi) -> Unit,
    onIrAlArmario: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().testTag("pestanaCompletaArmario"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.explora_completa_aviso),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        // El backend no sugiere nada cuando el armario ya tiene de todo
        if (sugerencias.isEmpty()) {
            item { EmptyMessage(messageRes = R.string.explora_armario_completo) }
        }
        items(sugerencias, key = { it.categoria.codigo }) { sugerencia ->
            SugerenciaCard(
                sugerencia = sugerencia,
                guardados = guardados,
                onItem = onItem,
                onGuardar = onGuardar,
            )
        }
        // Las sugerencias salen de lo que hay en Mi armario: mientras mas prendas, mejores ideas
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.explora_sin_prendas_armario),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AppOutlinedButton(texto = stringResource(R.string.ir_a_mi_armario), onClick = onIrAlArmario)
            }
        }
    }
}

@Composable
fun SugerenciaCard(
    sugerencia: SugerenciaArmarioUi,
    guardados: Set<String>,
    onItem: (ItemExploraUi) -> Unit,
    onGuardar: (ItemExploraUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = sugerencia.titulo,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Text(
                text = sugerencia.razon,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(sugerencia.items, key = { it.id }) { item ->
                    ItemExploraCard(
                        item = item,
                        guardado = item.id in guardados,
                        onClick = { onItem(item) },
                        onGuardar = { onGuardar(item) },
                        modifier = Modifier.width(150.dp),
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 393, heightDp = 700)
fun CuadriculaDeItemsPreview() {
    OrloTheme {
        CuadriculaDeItems(
            items = LocalPreviewProvider.itemsExplora,
            guardados = setOf("17"),
            vacio = R.string.explora_sin_items,
            onItem = {},
            onGuardar = {},
            aviso = "Las tendencias son una opción, no una regla.",
        )
    }
}

@Composable
@Preview(showBackground = true, widthDp = 393, heightDp = 700)
fun PestanaCompletaArmarioPreview() {
    OrloTheme {
        PestanaCompletaArmario(
            sugerencias = LocalPreviewProvider.sugerencias,
            guardados = emptySet(),
            onItem = {},
            onGuardar = {},
            onIrAlArmario = {},
        )
    }
}
