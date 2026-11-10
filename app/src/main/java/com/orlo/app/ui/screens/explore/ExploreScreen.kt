package com.orlo.app.ui.screens.explore

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import com.orlo.app.data.model.ItemExploraUi
import com.orlo.app.data.model.SeccionExplora
import com.orlo.app.data.model.comoItem
import com.orlo.app.ui.screens.explore.components.CuadriculaDeItems
import com.orlo.app.ui.screens.explore.components.PestanaCompletaArmario
import com.orlo.app.ui.theme.OrloTheme
import com.orlo.app.ui.utils.ErrorBox
import com.orlo.app.ui.utils.ErrorMessage
import com.orlo.app.ui.utils.LoadingBox
import com.orlo.app.ui.utils.ScreenTitle

// Explora: prendas de referencia para el estilo, lo que le falta al armario, tendencias,
// complementos y lo guardado. Cada prenda abre su detalle con ideas para combinarla y tiendas
@Composable
fun ExploreScreen(
    exploreViewModel: ExploreViewModel,
    onItem: (String) -> Unit,
    onIrAlArmario: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by exploreViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        exploreViewModel.cargar()
    }

    ExploreScreenContent(
        state = state,
        onSeccion = { exploreViewModel.cambiarSeccion(it) },
        onItem = { onItem(it.id) },
        onGuardar = { exploreViewModel.alternarGuardado(it) },
        onReintentar = { exploreViewModel.reintentar() },
        onIrAlArmario = onIrAlArmario,
        modifier = modifier,
    )
}

@Composable
fun ExploreScreenContent(
    state: ExploreState,
    onSeccion: (SeccionExplora) -> Unit,
    onItem: (ItemExploraUi) -> Unit,
    onGuardar: (ItemExploraUi) -> Unit,
    onReintentar: () -> Unit,
    onIrAlArmario: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pestanas = listOf(
        SeccionExplora.PARA_TU_ESTILO to R.string.pestana_para_tu_estilo,
        SeccionExplora.COMPLETA_TU_ARMARIO to R.string.pestana_completa_armario,
        SeccionExplora.TENDENCIAS to R.string.pestana_tendencias,
        SeccionExplora.COMPLEMENTOS to R.string.pestana_complementos,
        SeccionExplora.GUARDADOS to R.string.pestana_guardados,
    )

    Column(modifier = modifier.fillMaxSize().testTag("exploreScreen")) {
        ScreenTitle(
            titulo = stringResource(R.string.explora_titulo),
            subtitulo = stringResource(R.string.explora_subtitulo),
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
        )
        ScrollableTabRow(selectedTabIndex = state.seccion.ordinal, edgePadding = 16.dp) {
            pestanas.forEach { (seccion, texto) ->
                Tab(
                    selected = state.seccion == seccion,
                    onClick = { onSeccion(seccion) },
                    text = { Text(text = stringResource(texto)) },
                    modifier = Modifier.testTag("tab_${seccion.name}"),
                )
            }
        }
        ErrorMessage(messageRes = state.errorMessageRes, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))

        val errorCarga = state.errorCargaRes
        when {
            state.cargando -> LoadingBox()
            errorCarga != null -> ErrorBox(messageRes = errorCarga, onRetry = onReintentar)
            state.seccion == SeccionExplora.COMPLETA_TU_ARMARIO -> PestanaCompletaArmario(
                sugerencias = state.sugerencias.orEmpty(),
                guardados = state.idsGuardados,
                onItem = onItem,
                onGuardar = onGuardar,
                onIrAlArmario = onIrAlArmario,
            )
            state.seccion == SeccionExplora.GUARDADOS -> CuadriculaDeItems(
                items = state.guardados.map { it.comoItem() },
                guardados = state.idsGuardados,
                vacio = R.string.explora_sin_guardados,
                onItem = onItem,
                onGuardar = onGuardar,
            )
            else -> CuadriculaDeItems(
                items = state.itemsDeLaSeccion,
                guardados = state.idsGuardados,
                vacio = R.string.explora_sin_items,
                onItem = onItem,
                onGuardar = onGuardar,
                aviso = if (state.seccion == SeccionExplora.TENDENCIAS) stringResource(R.string.explora_tendencias_aviso) else null,
            )
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 393, heightDp = 852)
fun ExploreScreenPreview() {
    OrloTheme {
        ExploreScreenContent(
            state = ExploreState(
                items = mapOf(SeccionExplora.PARA_TU_ESTILO to LocalPreviewProvider.itemsExplora),
                guardados = LocalPreviewProvider.guardados,
            ),
            onSeccion = {}, onItem = {}, onGuardar = {}, onReintentar = {}, onIrAlArmario = {},
        )
    }
}
