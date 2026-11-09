package com.orlo.app.ui.screens.explore.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.orlo.app.R
import com.orlo.app.data.local.LocalPreviewProvider
import com.orlo.app.data.model.ItemExploraUi
import com.orlo.app.ui.theme.OrloTheme
import com.orlo.app.ui.utils.OrloAsyncImage

// Una prenda del catalogo: foto, nombre, por que va con el estilo y el marcador para guardarla
@Composable
fun ItemExploraCard(
    item: ItemExploraUi,
    guardado: Boolean,
    onClick: () -> Unit,
    onGuardar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Box {
            OrloAsyncImage(
                url = item.imagenUrl,
                contentDescription = item.nombre,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.8F),
            )
            IconButton(
                onClick = onGuardar,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85F), CircleShape)
                    .size(36.dp)
                    .testTag("guardar_${item.id}"),
            ) {
                Icon(
                    imageVector = if (guardado) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = stringResource(if (guardado) R.string.quitar_de_guardados else R.string.guardar_para_despues),
                    tint = if (guardado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Column(modifier = Modifier.padding(10.dp)) {
            if (item.tendencia) {
                Text(
                    text = stringResource(R.string.tendencia),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = item.nombre,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (item.porQueTeQueda.isNotBlank()) {
                Text(
                    text = item.porQueTeQueda,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 180)
fun ItemExploraCardPreview() {
    OrloTheme {
        ItemExploraCard(item = LocalPreviewProvider.itemsExplora.last(), guardado = true, onClick = {}, onGuardar = {})
    }
}
