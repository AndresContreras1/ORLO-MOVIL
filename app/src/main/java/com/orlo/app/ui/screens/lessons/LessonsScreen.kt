package com.orlo.app.ui.screens.lessons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.orlo.app.R
import com.orlo.app.data.local.LocalPreviewProvider
import com.orlo.app.data.model.EstadoLeccion
import com.orlo.app.data.model.LeccionResumenUi
import com.orlo.app.ui.screens.lessons.components.LeccionCard
import com.orlo.app.ui.theme.OrloTheme
import com.orlo.app.ui.utils.EmptyMessage
import com.orlo.app.ui.utils.ErrorBox
import com.orlo.app.ui.utils.LoadingBox
import com.orlo.app.ui.utils.ScreenTitle

@Composable
fun LessonsScreen(
    lessonsViewModel: LessonsViewModel,
    onLeccion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by lessonsViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        lessonsViewModel.cargar()
    }

    val error = state.errorMessageRes
    when {
        state.isLoading -> LoadingBox(modifier = modifier)
        error != null -> ErrorBox(messageRes = error, onRetry = { lessonsViewModel.cargar() }, modifier = modifier)
        else -> LessonsScreenContent(
            lecciones = state.lecciones,
            completadas = state.completadas,
            premium = state.premium,
            onLeccion = onLeccion,
            modifier = modifier,
        )
    }
}

@Composable
fun LessonsScreenContent(
    lecciones: List<LeccionResumenUi>,
    completadas: Int,
    premium: Boolean,
    onLeccion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("lessonsScreen"),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ScreenTitle(
                    titulo = stringResource(R.string.aprende_titulo),
                    subtitulo = stringResource(R.string.aprende_subtitulo),
                )
                if (lecciones.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.avance_de, completadas, lecciones.size),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.testTag("avanceLecciones"),
                    )
                    LinearProgressIndicator(
                        progress = { completadas.toFloat() / lecciones.size },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        if (lecciones.isEmpty()) {
            item { EmptyMessage(messageRes = R.string.sin_lecciones) }
        }
        items(lecciones, key = { it.id }) { leccion ->
            LeccionCard(
                leccion = leccion,
                bloqueada = !premium && !leccion.muestraGratis,
                onClick = { onLeccion(leccion.id) },
                modifier = Modifier.testTag("leccion_${leccion.id}"),
            )
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 393, heightDp = 852)
fun LessonsScreenPreview() {
    OrloTheme {
        LessonsScreenContent(
            lecciones = LocalPreviewProvider.lecciones,
            completadas = 1, premium = false, onLeccion = {},
        )
    }
}
