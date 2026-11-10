package com.orlo.app.ui.screens.wardrobe.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.orlo.app.data.model.CategoriaPrenda
import com.orlo.app.data.model.PrendaUi
import com.orlo.app.data.model.colorDePrenda
import com.orlo.app.ui.theme.OrloTheme
import com.orlo.app.ui.utils.OrloAsyncImage
import com.orlo.app.ui.utils.colorDeHex

// Una prenda en la cuadricula: su foto o, si no tiene, un cuadro con su color principal
@Composable
fun PrendaCard(
    prenda: PrendaUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column {
            if (prenda.fotoUrl != null) {
                OrloAsyncImage(
                    url = prenda.fotoUrl,
                    contentDescription = prenda.nombre,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1F),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1F)
                        .background(colorDePrenda(prenda.color)?.let { colorDeHex(it.hex) } ?: MaterialTheme.colorScheme.surfaceVariant),
                )
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = prenda.nombre.ifBlank { stringResource(prenda.categoria.etiqueta) },
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(prenda.categoria.etiqueta),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 160)
fun PrendaCardPreview() {
    OrloTheme {
        PrendaCard(prenda = PrendaUi(nombre = "Jean azul", categoria = CategoriaPrenda.ABAJO, color = "azul"), onClick = {})
    }
}
