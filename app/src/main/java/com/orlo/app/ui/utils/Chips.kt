package com.orlo.app.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

// Opciones que se eligen tocando: una o varias segun quien lo use. Cada opcion es codigo y texto.
// Una fila que se desliza de lado cuando las opciones no caben en el ancho
@Composable
fun SelectorDeChips(
    opciones: List<Pair<String, String>>,
    seleccion: Set<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(opciones, key = { it.first }) { (codigo, texto) ->
            FilterChip(
                selected = codigo in seleccion,
                onClick = { onToggle(codigo) },
                label = { Text(text = texto) },
                modifier = Modifier.testTag("chip_$codigo"),
            )
        }
    }
}
