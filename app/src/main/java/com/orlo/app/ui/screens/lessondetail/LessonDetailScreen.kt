package com.orlo.app.ui.screens.lessondetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.orlo.app.R
import com.orlo.app.data.local.LocalPreviewProvider
import com.orlo.app.data.model.EjemploUi
import com.orlo.app.data.model.EstadoLeccion
import com.orlo.app.data.model.LeccionUi
import com.orlo.app.ui.screens.lessondetail.components.RepasoCard
import com.orlo.app.ui.theme.OrloTheme
import com.orlo.app.ui.utils.AppButton
import com.orlo.app.ui.utils.ErrorBox
import com.orlo.app.ui.utils.ErrorMessage
import com.orlo.app.ui.utils.LoadingBox
import com.orlo.app.ui.utils.OrloAsyncImage
import com.orlo.app.ui.utils.PaletaDeColores
import com.orlo.app.ui.utils.PremiumGate

@Composable
fun LessonDetailScreen(
    lessonDetailViewModel: LessonDetailViewModel,
    leccionId: String,
    onBack: () -> Unit,
    onPremium: () -> Unit,
    onCompletarPerfil: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by lessonDetailViewModel.uiState.collectAsState()

    LaunchedEffect(leccionId) {
        lessonDetailViewModel.cargar(leccionId)
    }

    Column(modifier = modifier.fillMaxSize().testTag("lessonDetailScreen")) {
        IconButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp, top = 8.dp)) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.atras))
        }

        val leccion = state.leccion
        val errorCarga = state.errorCargaRes
        when {
            state.isLoading -> LoadingBox()
            state.requierePremium -> PremiumGate(
                titulo = stringResource(R.string.leccion_premium_titulo),
                descripcion = stringResource(R.string.leccion_premium_texto),
                onVerPlanes = onPremium,
                modifier = Modifier.padding(20.dp),
            )
            errorCarga != null -> ErrorBox(messageRes = errorCarga, onRetry = { lessonDetailViewModel.cargar(leccionId) })
            leccion != null -> LessonDetailScreenContent(
                leccion = leccion,
                estado = state.estado,
                coloresDelPerfil = state.coloresDelPerfil,
                respuestaRepaso = state.respuestaRepaso,
                errorMessageRes = state.errorMessageRes,
                guardando = state.guardando,
                onResponderRepaso = { lessonDetailViewModel.responderRepaso(it) },
                onCompletar = { lessonDetailViewModel.marcarCompletada() },
                onCompletarPerfil = onCompletarPerfil,
            )
        }
    }
}

// Los ejemplos son una lista que viene del backend: van en una LazyColumn con item { } para las
// partes fijas e items(...) para los ejemplos
@Composable
fun LessonDetailScreenContent(
    leccion: LeccionUi,
    estado: EstadoLeccion,
    coloresDelPerfil: List<String>,
    respuestaRepaso: Int?,
    errorMessageRes: Int?,
    guardando: Boolean,
    onResponderRepaso: (Int) -> Unit,
    onCompletar: () -> Unit,
    onCompletarPerfil: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(text = leccion.titulo, style = MaterialTheme.typography.headlineMedium)
        }
        item {
            Text(text = leccion.ideaPrincipal, style = MaterialTheme.typography.bodyLarge)
        }
        if (leccion.usaColorimetria) {
            item {
                Colorimetria(colores = coloresDelPerfil, onCompletarPerfil = onCompletarPerfil)
            }
        }
        items(leccion.ejemplos) { ejemplo ->
            EjemploDeLeccion(ejemplo = ejemplo)
        }
        if (leccion.consejo.isNotBlank()) {
            item {
                ConsejoParaHoy(consejo = leccion.consejo)
            }
        }
        leccion.repaso?.let { repaso ->
            item {
                RepasoCard(repaso = repaso, respuesta = respuestaRepaso, onResponder = onResponderRepaso)
            }
        }
        item {
            ErrorMessage(messageRes = errorMessageRes)
        }
        item {
            if (estado == EstadoLeccion.COMPLETADA) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag("leccionCompletada"),
                ) {
                    Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(text = stringResource(R.string.leccion_completada), style = MaterialTheme.typography.titleMedium)
                }
            } else {
                AppButton(
                    texto = stringResource(R.string.marcar_completada),
                    onClick = onCompletar,
                    cargando = guardando,
                    modifier = Modifier.testTag("btnCompletarLeccion"),
                )
            }
        }
    }
}

@Composable
fun EjemploDeLeccion(
    ejemplo: EjemploUi,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (!ejemplo.imagenUrl.isNullOrBlank()) {
            OrloAsyncImage(
                url = ejemplo.imagenUrl,
                contentDescription = ejemplo.texto,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4F / 3F)
                    .clip(MaterialTheme.shapes.medium),
            )
        }
        Text(text = ejemplo.texto, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ConsejoParaHoy(
    consejo: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.consejo_para_hoy),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(text = consejo, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

// La capsula de colorimetria usa los colores del perfil. Si faltan, se explica en general y se
// invita a completarlos
@Composable
fun Colorimetria(
    colores: List<String>,
    onCompletarPerfil: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (colores.isNotEmpty()) {
                Text(text = stringResource(R.string.tus_colores), style = MaterialTheme.typography.titleMedium)
                PaletaDeColores(colores = colores)
                Text(text = stringResource(R.string.tus_colores_texto), style = MaterialTheme.typography.bodySmall)
            } else {
                Text(text = stringResource(R.string.colorimetria_sin_datos), style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onCompletarPerfil) { Text(text = stringResource(R.string.completar_mis_datos)) }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 393, heightDp = 1200)
fun LessonDetailScreenPreview() {
    OrloTheme {
        LessonDetailScreenContent(
            leccion = LocalPreviewProvider.leccion,
            estado = EstadoLeccion.EN_CURSO, coloresDelPerfil = listOf("#C99A72", "#4A3022", "#5E3B22"),
            respuestaRepaso = 0, errorMessageRes = null, guardando = false,
            onResponderRepaso = {}, onCompletar = {}, onCompletarPerfil = {},
        )
    }
}

@Composable
@Preview(showBackground = true, widthDp = 393, heightDp = 600)
fun LessonDetailScreenOscuroPreview() {
    OrloTheme(darkTheme = true) {
        LessonDetailScreenContent(
            leccion = LocalPreviewProvider.leccion,
            estado = EstadoLeccion.COMPLETADA, coloresDelPerfil = emptyList(),
            respuestaRepaso = null, errorMessageRes = null, guardando = false,
            onResponderRepaso = {}, onCompletar = {}, onCompletarPerfil = {},
        )
    }
}
