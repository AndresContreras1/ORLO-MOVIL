package com.orlo.app.ui.screens.lessons.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.orlo.app.R
import com.orlo.app.data.local.LocalPreviewProvider
import com.orlo.app.data.model.EstadoLeccion
import com.orlo.app.data.model.LeccionResumenUi
import com.orlo.app.ui.theme.OrloTheme
import com.orlo.app.ui.utils.OrloAsyncImage

// Una capsula en la lista: portada, titulo, minutos y su estado (sin empezar, en curso, completada)
@Composable
fun LeccionCard(
    leccion: LeccionResumenUi,
    bloqueada: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OrloAsyncImage(
                url = leccion.portadaUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .clip(MaterialTheme.shapes.small),
            )
            Column(modifier = Modifier.weight(1F), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = leccion.titulo, style = MaterialTheme.typography.titleMedium)
                if (leccion.resumen.isNotBlank()) {
                    Text(
                        text = leccion.resumen,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (leccion.minutos > 0) {
                        Text(
                            text = stringResource(R.string.minutos, leccion.minutos),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (leccion.muestraGratis) {
                        AssistChip(onClick = onClick, label = { Text(text = stringResource(R.string.muestra_gratis)) })
                    }
                }
            }
            val (icono, descripcion) = when {
                bloqueada -> Icons.Outlined.Lock to R.string.premium
                leccion.estado == EstadoLeccion.COMPLETADA -> Icons.Filled.CheckCircle to R.string.estado_completada
                leccion.estado == EstadoLeccion.EN_CURSO -> Icons.Outlined.PlayCircle to R.string.estado_en_curso
                else -> Icons.Outlined.RadioButtonUnchecked to R.string.estado_sin_empezar
            }
            Icon(
                imageVector = icono,
                contentDescription = stringResource(descripcion),
                tint = if (leccion.estado == EstadoLeccion.COMPLETADA && !bloqueada) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun LeccionCardPreview() {
    OrloTheme {
        LeccionCard(
            leccion = LocalPreviewProvider.lecciones.first(),
            bloqueada = false, onClick = {},
        )
    }
}
