package com.orlo.app.ui.screens.wardrobe.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.orlo.app.R
import com.orlo.app.data.model.OrigenOutfit
import com.orlo.app.data.model.OutfitUi
import com.orlo.app.ui.theme.OrloTheme

// Un outfit guardado: nombre, ocasion, prendas, de donde salio, favorito y el menu para duplicar o
// borrar
@Composable
fun OutfitCard(
    outfit: OutfitUi,
    prendas: List<String>,
    onClick: () -> Unit,
    onFavorito: () -> Unit,
    menuAbierto: Boolean,
    onAbrirMenu: () -> Unit,
    onCerrarMenu: () -> Unit,
    onDuplicar: () -> Unit,
    onBorrar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 12.dp)) {
            Column(modifier = Modifier.weight(1F).padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = outfit.nombre.ifBlank { stringResource(R.string.outfit_sin_nombre) }, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(outfit.ocasion.etiqueta) + " · " + stringResource(
                        if (outfit.origen == OrigenOutfit.ASESOR) R.string.origen_asesor else R.string.origen_manual
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (prendas.isNotEmpty()) {
                    Text(text = prendas.joinToString(", "), style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onFavorito) {
                Icon(
                    imageVector = if (outfit.favorito) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = stringResource(if (outfit.favorito) R.string.quitar_favorito else R.string.marcar_favorito),
                    tint = if (outfit.favorito) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box(contentAlignment = Alignment.TopEnd) {
                IconButton(onClick = onAbrirMenu) {
                    Icon(imageVector = Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.mas_opciones))
                }
                // El estado del menu lo tiene el ViewModel: la tarjeta solo avisa
                DropdownMenu(expanded = menuAbierto, onDismissRequest = onCerrarMenu) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.duplicar)) },
                        onClick = onDuplicar,
                    )
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.borrar)) },
                        onClick = onBorrar,
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun OutfitCardPreview() {
    OrloTheme {
        OutfitCard(
            outfit = OutfitUi(nombre = "Lunes de clase", favorito = true),
            prendas = listOf("Jean azul", "Camiseta blanca"),
            onClick = {}, onFavorito = {}, menuAbierto = false, onAbrirMenu = {}, onCerrarMenu = {},
            onDuplicar = {}, onBorrar = {},
        )
    }
}
