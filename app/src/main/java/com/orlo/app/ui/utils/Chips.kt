package com.orlo.app.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

// Opciones que se eligen tocando: una o varias segun quien lo use. Cada opcion es codigo y texto
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectorDeChips(
    opciones: List<Pair<String, String>>,
    seleccion: Set<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        opciones.forEach { (codigo, texto) ->
            FilterChip(
                selected = codigo in seleccion,
                onClick = { onToggle(codigo) },
                label = { Text(text = texto) },
                modifier = Modifier.testTag("chip_$codigo"),
            )
        }
    }
}
