package com.orlo.app.ui.screens.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.orlo.app.R
import com.orlo.app.data.model.EstiloUi
import com.orlo.app.data.model.ResultadoUi
import com.orlo.app.ui.screens.result.components.TarjetaDeEstilo
import com.orlo.app.ui.theme.OrloTheme
import com.orlo.app.ui.utils.AppButton
import com.orlo.app.ui.utils.ErrorBox
import com.orlo.app.ui.utils.LoadingBox
import com.orlo.app.ui.utils.OrloAsyncImage
import com.orlo.app.ui.utils.ScreenTitle

// Pantalla 5: el diagnostico. El resultado ya quedo guardado en el perfil al terminar el cuestionario
@Composable
fun ResultScreen(
    resultViewModel: ResultViewModel,
    onContinuar: (premium: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by resultViewModel.uiState.collectAsState()
    val resultado = state.resultado
    val error = state.errorMessageRes

    when {
        state.isLoading -> LoadingBox(modifier = modifier)
        error != null -> ErrorBox(messageRes = error, onRetry = { resultViewModel.cargarResultado() }, modifier = modifier)
        resultado != null -> ResultScreenContent(
            resultado = resultado,
            onContinuar = { onContinuar(state.premium) },
            modifier = modifier,
        )
    }
}

@Composable
fun ResultScreenContent(
    resultado: ResultadoUi,
    onContinuar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .testTag("resultScreen"),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        ScreenTitle(
            titulo = stringResource(R.string.resultado_titulo),
            subtitulo = stringResource(R.string.resultado_subtitulo),
        )
        TarjetaDeEstilo(resultado = resultado)

        if (resultado.secundario.descripcion.isNotBlank()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.tu_toque, resultado.secundario.nombre.lowercase()),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(text = resultado.secundario.descripcion, style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (resultado.principal.galeria.isNotEmpty()) {
            Text(text = stringResource(R.string.referencias_para_ti), style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(resultado.principal.galeria) { url ->
                    OrloAsyncImage(
                        url = url,
                        contentDescription = stringResource(R.string.referencia_de_estilo),
                        modifier = Modifier
                            .width(140.dp)
                            .height(180.dp)
                            .clip(MaterialTheme.shapes.medium),
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.resultado_guardado),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AppButton(
            texto = stringResource(R.string.continuar),
            onClick = onContinuar,
            modifier = Modifier.testTag("btnContinuarResultado"),
        )
    }
}

@Composable
@Preview(showBackground = true, widthDp = 393, heightDp = 852)
fun ResultScreenPreview() {
    OrloTheme {
        ResultScreenContent(
            resultado = ResultadoUi(
                principal = EstiloUi(nombre = "Urbano", descripcion = "Prendas cómodas con un toque actual.", paleta = listOf("#2B2622", "#3D5A80")),
                secundario = EstiloUi(nombre = "Creativo", descripcion = "Te gusta probar combinaciones distintas."),
            ),
            onContinuar = {},
        )
    }
}
