package com.orlo.app.ui.screens.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.orlo.app.R
import com.orlo.app.data.model.CuestionarioUi
import com.orlo.app.data.model.OpcionUi
import com.orlo.app.data.model.PreguntaUi
import com.orlo.app.ui.screens.quiz.components.OpcionCard
import com.orlo.app.ui.theme.OrloTheme
import com.orlo.app.ui.utils.AppButton
import com.orlo.app.ui.utils.EmptyMessage
import com.orlo.app.ui.utils.ErrorBox
import com.orlo.app.ui.utils.ErrorMessage
import com.orlo.app.ui.utils.LoadingBox

@Composable
fun QuizScreen(
    quizViewModel: QuizViewModel,
    onResultado: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by quizViewModel.uiState.collectAsState()

    LaunchedEffect(state.navigate) {
        if (state.navigate) onResultado()
    }

    val errorCarga = state.errorCargaRes
    when {
        state.isLoading -> LoadingBox(modifier = modifier)
        errorCarga != null -> ErrorBox(
            messageRes = errorCarga,
            onRetry = { quizViewModel.cargarCuestionario() },
            modifier = modifier,
        )
        else -> QuizScreenContent(
            state = state,
            onOpcion = { quizViewModel.seleccionarOpcion(it) },
            onSiguiente = { quizViewModel.siguiente() },
            onAtras = { quizViewModel.atras() },
            modifier = modifier,
        )
    }
}

@Composable
fun QuizScreenContent(
    state: QuizState,
    onOpcion: (String) -> Unit,
    onSiguiente: () -> Unit,
    onAtras: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Si el cuestionario llega sin preguntas se explica, en vez de dejar la pantalla en blanco
    val pregunta = state.preguntaActual
    if (pregunta == null) {
        EmptyMessage(messageRes = R.string.sin_cuestionario, modifier = modifier.testTag("quizScreen"))
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("quizScreen"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Barra de progreso, el numero de la pregunta y la flecha para volver
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onAtras, enabled = state.indice > 0, modifier = Modifier.testTag("btnAtras")) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.atras))
            }
            Column(modifier = Modifier.weight(1F)) {
                Text(
                    text = stringResource(R.string.pregunta_de, state.indice + 1, state.total),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                LinearProgressIndicator(
                    progress = { state.progreso },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .testTag("progresoCuestionario"),
                )
            }
        }

        Text(text = pregunta.texto, style = MaterialTheme.typography.headlineSmall)
        val ayuda = if (pregunta.multiple) stringResource(R.string.puedes_elegir_varias) else pregunta.ayuda
        if (ayuda.isNotBlank()) {
            Text(text = ayuda, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1F),
        ) {
            items(pregunta.opciones, key = { it.id }) { opcion ->
                OpcionCard(
                    opcion = opcion,
                    seleccionada = opcion.id in state.seleccionActual,
                    onClick = { onOpcion(opcion.id) },
                    modifier = Modifier.testTag("opcion_${opcion.id}"),
                )
            }
        }

        ErrorMessage(messageRes = state.errorMessageRes)
        AppButton(
            texto = stringResource(if (state.esUltima) R.string.ver_mi_resultado else R.string.siguiente),
            onClick = onSiguiente,
            cargando = state.enviando,
            modifier = Modifier.testTag("btnSiguiente"),
        )
    }
}

@Composable
@Preview(showBackground = true, widthDp = 393, heightDp = 852)
fun QuizScreenPreview() {
    OrloTheme {
        QuizScreenContent(
            state = QuizState(
                isLoading = false,
                cuestionario = CuestionarioUi(
                    version = 1,
                    preguntas = listOf(
                        PreguntaUi(
                            id = "1", texto = "¿Qué prendas usas más en la semana?", multiple = true,
                            opciones = listOf(OpcionUi("1", "Camisetas"), OpcionUi("2", "Jeans"), OpcionUi("3", "Vestidos")),
                        )
                    ),
                ),
            ),
            onOpcion = {}, onSiguiente = {}, onAtras = {},
        )
    }
}
