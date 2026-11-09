package com.orlo.app.ui.screens.quiz.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.orlo.app.data.model.OpcionUi
import com.orlo.app.ui.theme.OrloTheme
import com.orlo.app.ui.utils.OrloAsyncImage

// Una opcion del cuestionario: la mayoria se responde tocando una imagen. La elegida se marca con
// el borde del color de acento
@Composable
fun OpcionCard(
    opcion: OpcionUi,
    seleccionada: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .semantics { selected = seleccionada },
        border = BorderStroke(
            width = if (seleccionada) 3.dp else 1.dp,
            color = if (seleccionada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionada) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column {
            if (!opcion.imagenUrl.isNullOrBlank()) {
                OrloAsyncImage(
                    url = opcion.imagenUrl,
                    contentDescription = opcion.texto,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.8F),
                )
            }
            Text(
                text = opcion.texto,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            )
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 180)
fun OpcionCardPreview() {
    OrloTheme {
        OpcionCard(opcion = OpcionUi(id = "1", texto = "Camisetas"), seleccionada = true, onClick = {})
    }
}
