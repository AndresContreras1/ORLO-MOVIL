package com.orlo.app.ui.screens.result.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.orlo.app.data.model.EstiloUi
import com.orlo.app.data.model.ResultadoUi
import com.orlo.app.ui.theme.OrloTheme
import com.orlo.app.ui.utils.PaletaDeColores

// El estilo del usuario en una tarjeta: nombre, descripcion corta y paleta sugerida. Se usa en el
// resultado, en Inicio y en Mi estilo
@Composable
fun TarjetaDeEstilo(
    resultado: ResultadoUi,
    modifier: Modifier = Modifier,
    mostrarDescripcion: Boolean = true,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = stringResource(R.string.tu_estilo),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = resultado.nombreCompleto,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            if (mostrarDescripcion && resultado.principal.descripcion.isNotBlank()) {
                Text(
                    text = resultado.principal.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            if (resultado.principal.paleta.isNotEmpty()) {
                PaletaDeColores(colores = resultado.principal.paleta, modifier = Modifier.testTag("paletaDelEstilo"))
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun TarjetaDeEstiloPreview() {
    OrloTheme {
        TarjetaDeEstilo(
            resultado = ResultadoUi(
                principal = EstiloUi(
                    nombre = "Urbano", descripcion = "Prendas cómodas con un toque actual.",
                    paleta = listOf("#2B2622", "#3D5A80", "#D9C3A5"),
                ),
                secundario = EstiloUi(nombre = "Creativo"),
            )
        )
    }
}
