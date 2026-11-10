package com.orlo.app.ui.screens.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.orlo.app.data.local.LocalPreviewProvider
import com.orlo.app.data.model.OutfitDelDiaUi
import com.orlo.app.ui.theme.OrloTheme
import com.orlo.app.ui.utils.AppButton
import com.orlo.app.ui.utils.ErrorMessage

// El outfit del dia en el inicio: si no se ha pedido, un boton; si ya esta, la propuesta del
// asesor con lo que hay en el armario y la opcion de pedir otra
@Composable
fun TarjetaOutfitDelDia(
    outfit: OutfitDelDiaUi?,
    pidiendo: Boolean,
    errorRes: Int?,
    onPedir: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth().testTag("tarjetaOutfitDelDia"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.outfit_del_dia),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            when {
                pidiendo -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text(text = stringResource(R.string.armando_outfit), style = MaterialTheme.typography.bodyMedium)
                }
                outfit == null -> {
                    Text(
                        text = stringResource(R.string.outfit_del_dia_texto),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    AppButton(
                        texto = stringResource(R.string.pedir_outfit_del_dia),
                        onClick = onPedir,
                        modifier = Modifier.testTag("btnOutfitDelDia"),
                    )
                }
                else -> {
                    Text(text = outfit.titulo, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(
                        text = outfit.prendas.joinToString("\n") { "• $it" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    if (outfit.porQue.isNotBlank()) {
                        Text(text = outfit.porQue, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    TextButton(onClick = onPedir, modifier = Modifier.testTag("btnOtraOpcionOutfit")) {
                        Text(text = stringResource(R.string.otra_opcion_outfit))
                    }
                }
            }
            ErrorMessage(messageRes = errorRes)
        }
    }
}

@Composable
@Preview(showBackground = true)
fun TarjetaOutfitDelDiaPreview() {
    OrloTheme {
        TarjetaOutfitDelDia(outfit = LocalPreviewProvider.outfitDelDia, pidiendo = false, errorRes = null, onPedir = {})
    }
}

@Composable
@Preview(showBackground = true)
fun TarjetaOutfitDelDiaSinPedirPreview() {
    OrloTheme {
        TarjetaOutfitDelDia(outfit = null, pidiendo = false, errorRes = null, onPedir = {})
    }
}
