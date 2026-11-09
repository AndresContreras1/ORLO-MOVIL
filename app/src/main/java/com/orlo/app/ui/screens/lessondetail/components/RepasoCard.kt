package com.orlo.app.ui.screens.lessondetail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.orlo.app.R
import com.orlo.app.data.model.RepasoUi
import com.orlo.app.ui.theme.OrloTheme

// La pregunta de repaso opcional: una sola opcion y una linea que dice si acerto, sin penalizar
@Composable
fun RepasoCard(
    repaso: RepasoUi,
    respuesta: Int?,
    onResponder: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stringResource(R.string.repaso), style = MaterialTheme.typography.labelMedium)
            Text(text = repaso.pregunta, style = MaterialTheme.typography.titleMedium)
            repaso.opciones.forEachIndexed { indice, opcion ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(selected = respuesta == indice, role = Role.RadioButton, onClick = { onResponder(indice) }),
                ) {
                    RadioButton(selected = respuesta == indice, onClick = null)
                    Text(text = opcion, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp))
                }
            }
            if (respuesta != null) {
                Text(
                    text = stringResource(
                        if (respuesta == repaso.indiceCorrecto) R.string.repaso_correcto else R.string.repaso_incorrecto,
                        repaso.opciones.getOrElse(repaso.indiceCorrecto) { "" },
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun RepasoCardPreview() {
    OrloTheme {
        RepasoCard(
            repaso = RepasoUi(pregunta = "¿Qué color combina con todo?", opciones = listOf("Neutro", "Fucsia"), indiceCorrecto = 0),
            respuesta = 0, onResponder = {},
        )
    }
}
