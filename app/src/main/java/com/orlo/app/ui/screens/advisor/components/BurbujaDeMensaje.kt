package com.orlo.app.ui.screens.advisor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.orlo.app.R
import com.orlo.app.data.model.MensajeChatUi
import com.orlo.app.data.model.OutfitPropuestoUi
import com.orlo.app.data.model.RolMensaje
import com.orlo.app.ui.theme.OrloTheme

// Un mensaje del chat: la pregunta a la derecha con el color de acento, la respuesta a la izquierda
// con sus outfits
@Composable
fun BurbujaDeMensaje(
    mensaje: MensajeChatUi,
    guardados: Set<Int>,
    onGuardarOutfit: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val esUsuario = mensaje.rol == RolMensaje.USUARIO

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = if (esUsuario) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (mensaje.texto.isNotBlank()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (esUsuario) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        contentColor = if (esUsuario) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Text(
                        text = mensaje.texto,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
            mensaje.outfits.forEachIndexed { indice, outfit ->
                OutfitPropuestoCard(
                    outfit = outfit,
                    guardado = indice in guardados,
                    onGuardar = { onGuardarOutfit(indice) },
                    modifier = Modifier.testTag("outfitPropuesto_$indice"),
                )
            }
            mensaje.preguntaSeguimiento?.let { pregunta ->
                Text(text = pregunta, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// Una opcion de outfit con sus prendas, el porque y el boton "Guardar outfit"
@Composable
fun OutfitPropuestoCard(
    outfit: OutfitPropuestoUi,
    guardado: Boolean,
    onGuardar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = outfit.titulo, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            outfit.prendas.forEach { prenda ->
                Text(text = "• $prenda", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            if (outfit.porQue.isNotBlank()) {
                Text(text = outfit.porQue, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            TextButton(onClick = onGuardar, enabled = !guardado, modifier = Modifier.align(Alignment.End)) {
                Icon(
                    imageVector = if (guardado) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = null,
                )
                Text(
                    text = stringResource(if (guardado) R.string.outfit_guardado_corto else R.string.guardar_outfit),
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun BurbujaDeMensajePreview() {
    OrloTheme {
        BurbujaDeMensaje(
            mensaje = MensajeChatUi(
                rol = RolMensaje.ASESOR,
                texto = "Aquí van dos ideas para la universidad:",
                outfits = listOf(OutfitPropuestoUi("Cómodo y fresco", listOf("Jean azul", "Camiseta blanca", "Tenis"), "Fácil de combinar")),
            ),
            guardados = emptySet(), onGuardarOutfit = {},
        )
    }
}
