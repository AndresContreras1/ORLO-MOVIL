package com.orlo.app.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.orlo.app.ui.theme.OrloTheme

// Convierte "#RRGGBB" en un Color de Compose; si el texto no es un color valido queda gris
fun colorDeHex(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (e: IllegalArgumentException) {
    Color.Gray
}

// La paleta sugerida de un estilo: una fila de circulos
@Composable
fun PaletaDeColores(
    colores: List<String>,
    modifier: Modifier = Modifier,
    tamano: Dp = 32.dp,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        colores.forEach { hex ->
            Box(
                modifier = Modifier
                    .size(tamano)
                    .clip(CircleShape)
                    .background(colorDeHex(hex))
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
        }
    }
}

// Un circulo de color que se puede elegir, con su nombre debajo. Se elige tocando, sin escribir
@Composable
fun CirculoDeColor(
    hex: String,
    etiqueta: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = colorDeHex(hex)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(72.dp)
            .clip(MaterialTheme.shapes.small)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics {
                selected = seleccionado
                contentDescription = etiqueta
            },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color)
                .border(
                    width = if (seleccionado) 3.dp else 1.dp,
                    color = if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    shape = CircleShape,
                ),
        ) {
            if (seleccionado) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    // El check se ve sobre colores claros y oscuros
                    tint = if (color.luminance() > 0.5f) Color.Black else Color.White,
                )
            }
        }
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

// Una fila de circulos que salta de linea cuando no caben
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectorDeColor(
    opciones: List<Triple<String, String, String>>,
    seleccion: String,
    onSeleccion: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Cada opcion es codigo, color y etiqueta. Tocar la elegida la quita
        opciones.forEach { (codigo, hex, etiqueta) ->
            CirculoDeColor(
                hex = hex,
                etiqueta = etiqueta,
                seleccionado = codigo == seleccion,
                onClick = { onSeleccion(if (codigo == seleccion) "" else codigo) },
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun PaletaDeColoresPreview() {
    OrloTheme {
        PaletaDeColores(colores = listOf("#2B2622", "#E2725B", "#D9C3A5", "#3D5A80"))
    }
}
