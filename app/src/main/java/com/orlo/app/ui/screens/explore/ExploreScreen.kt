package com.orlo.app.ui.screens.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.orlo.app.ui.theme.OrloTheme
import com.orlo.app.ui.utils.AppOutlinedButton
import com.orlo.app.ui.utils.ScreenTitle

// Explora llega en la siguiente fase. La pestana existe desde ya para que la navegacion no cambie:
// cuenta que va a traer y lleva al armario, que es donde hoy se aprovecha la ropa que ya se tiene
@Composable
fun ExploreScreen(
    onIrAlArmario: () -> Unit,
    modifier: Modifier = Modifier
) {
    val proximas = listOf(
        R.string.explora_para_tu_estilo,
        R.string.explora_completa_armario,
        R.string.explora_tendencias,
        R.string.explora_complementos,
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .testTag("exploreScreen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenTitle(titulo = stringResource(R.string.explora_titulo), subtitulo = stringResource(R.string.explora_proximamente))
        proximas.forEach { texto ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Text(text = stringResource(texto), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(16.dp))
            }
        }
        AppOutlinedButton(texto = stringResource(R.string.ir_a_mi_armario), onClick = onIrAlArmario)
    }
}

@Composable
@Preview(showBackground = true, widthDp = 393, heightDp = 852)
fun ExploreScreenPreview() {
    OrloTheme {
        ExploreScreen(onIrAlArmario = {})
    }
}
