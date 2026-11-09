package com.orlo.app.ui.screens.lessondetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.orlo.app.data.model.EjemploUi
import com.orlo.app.data.model.EstadoLeccion
import com.orlo.app.data.model.LeccionUi
import com.orlo.app.data.model.OpcionesDelPerfil
import com.orlo.app.data.model.PerfilUi
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
    leccionId: Long,
    onBack: () -> Unit,
    onPremium: () -> Unit,
    onCompletarPerfil: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by lessonDetailViewModel.uiState.collectAsState()

    LaunchedEffect(leccionId) {
        lessonDetailViewModel.cargar(leccionId)
    }

    Column(modifier = modifier.fillMaxSize()) {
        IconButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp, top = 8.dp)) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.atras))
        }

        val leccion = state.leccion
        val error = state.errorMessageRes
        when {
            state.isLoading -> LoadingBox()
            state.requierePremium -> PremiumGate(
                titulo = stringResource(R.string.leccion_premium_titulo),
                descripcion = stringResource(R.string.leccion_premium_texto),
                onVerPlanes = onPremium,
                modifier = Modifier.padding(20.dp),
            )
            leccion == null && error != null -> ErrorBox(
                messageRes = error,
                onRetry = { lessonDetailViewModel.cargar(leccionId) },
            )
            leccion != null -> LessonDetailScreenContent(
                leccion = leccion,
                estado = state.estado,
                perfil = state.perfil,
                respuestaRepaso = state.respuestaRepaso,
                errorMessageRes = error,
                guardando = state.guardando,
                onResponderRepaso = { lessonDetailViewModel.responderRepaso(it) },
                onCompletar = { lessonDetailViewModel.marcarCompletada() },
                onCompletarPerfil = onCompletarPerfil,
            )
        }
    }
}

@Composable
fun LessonDetailScreenContent(
    leccion: LeccionUi,
    estado: EstadoLeccion,
    perfil: PerfilUi?,
    respuestaRepaso: Int?,
    errorMessageRes: Int?,
    guardando: Boolean,
    onResponderRepaso: (Int) -> Unit,
    onCompletar: () -> Unit,
    onCompletarPerfil: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
            .testTag("lessonDetailScreen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = leccion.titulo, style = MaterialTheme.typography.headlineMedium)
        Text(text = leccion.ideaPrincipal, style = MaterialTheme.typography.bodyLarge)

        if (leccion.usaColorimetria) {
            Colorimetria(perfil = perfil, onCompletarPerfil = onCompletarPerfil)
        }

        leccion.ejemplos.forEach { ejemplo ->
            EjemploDeLeccion(ejemplo = ejemplo)
        }

        if (leccion.consejo.isNotBlank()) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.consejo_para_hoy),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(text = leccion.consejo, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        leccion.repaso?.let { repaso ->
            RepasoCard(repaso = repaso, respuesta = respuestaRepaso, onResponder = onResponderRepaso)
        }

        ErrorMessage(messageRes = errorMessageRes)
        if (estado == EstadoLeccion.COMPLETADA) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

@Composable
private fun EjemploDeLeccion(ejemplo: EjemploUi) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

// La capsula de colorimetria usa los colores del perfil. Si faltan, se explica en general y se
// invita a completarlos
@Composable
private fun Colorimetria(perfil: PerfilUi?, onCompletarPerfil: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (perfil != null && perfil.tieneColorimetria) {
                Text(text = stringResource(R.string.tus_colores), style = MaterialTheme.typography.titleMedium)
                val colores = listOfNotNull(
                    OpcionesDelPerfil.tonosDePiel.firstOrNull { it.codigo == perfil.tonoPiel }?.hex,
                    OpcionesDelPerfil.coloresDeCabello.firstOrNull { it.codigo == perfil.colorCabello }?.hex,
                    OpcionesDelPerfil.coloresDeOjos.firstOrNull { it.codigo == perfil.colorOjos }?.hex,
                )
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
            leccion = LeccionUi(
                id = 1, titulo = "Cómo combinar los colores", ideaPrincipal = "Los neutros combinan con casi todo.",
                ejemplos = listOf(EjemploUi(texto = "Jean azul con camiseta blanca")), consejo = "Prueba hoy un color nuevo con un neutro.",
                usaColorimetria = true,
            ),
            estado = EstadoLeccion.EN_CURSO, perfil = null, respuestaRepaso = null, errorMessageRes = null,
            guardando = false, onResponderRepaso = {}, onCompletar = {}, onCompletarPerfil = {},
        )
    }
}
