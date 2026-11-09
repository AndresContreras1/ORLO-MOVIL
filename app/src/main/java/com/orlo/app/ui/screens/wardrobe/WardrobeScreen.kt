package com.orlo.app.ui.screens.wardrobe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.CircleShape
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
import com.orlo.app.data.local.LocalPreviewProvider
import com.orlo.app.data.model.CategoriaPrenda
import com.orlo.app.data.model.OutfitUi
import com.orlo.app.data.model.PrendaUi
import com.orlo.app.ui.screens.wardrobe.components.PestanaHistorial
import com.orlo.app.ui.screens.wardrobe.components.PestanaMiEstilo
import com.orlo.app.ui.screens.wardrobe.components.PestanaOutfits
import com.orlo.app.ui.screens.wardrobe.components.PestanaPrendas
import com.orlo.app.ui.theme.OrloTheme
import com.orlo.app.ui.utils.ConfirmDialog
import com.orlo.app.ui.utils.ErrorBox
import com.orlo.app.ui.utils.ErrorMessage
import com.orlo.app.ui.utils.LoadingBox
import com.orlo.app.ui.utils.PremiumGate
import com.orlo.app.ui.utils.ScreenTitle

@Composable
fun WardrobeScreen(
    wardrobeViewModel: WardrobeViewModel,
    onNuevaPrenda: () -> Unit,
    onPrenda: (String) -> Unit,
    onNuevoOutfit: () -> Unit,
    onOutfit: (String) -> Unit,
    onConversacion: (String) -> Unit,
    onEditarPreferencias: () -> Unit,
    onRehacerCuestionario: () -> Unit,
    onPremium: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by wardrobeViewModel.uiState.collectAsState()

    // Recarga al volver de un formulario
    LaunchedEffect(Unit) {
        wardrobeViewModel.cargar()
    }

    state.outfitPorBorrar?.let { outfit ->
        ConfirmDialog(
            titulo = stringResource(R.string.borrar_outfit_titulo),
            texto = stringResource(R.string.borrar_outfit_texto, outfit.nombre),
            confirmar = stringResource(R.string.borrar),
            onConfirmar = { wardrobeViewModel.borrarOutfit() },
            onCancelar = { wardrobeViewModel.cancelarBorrarOutfit() },
        )
    }

    val errorCarga = state.errorCargaRes
    when {
        state.isLoading -> LoadingBox(modifier = modifier)
        errorCarga != null -> ErrorBox(messageRes = errorCarga, onRetry = { wardrobeViewModel.cargar() }, modifier = modifier)
        !state.premium -> Column(modifier = modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            ScreenTitle(titulo = stringResource(R.string.armario_titulo), subtitulo = stringResource(R.string.armario_subtitulo))
            PremiumGate(
                titulo = stringResource(R.string.armario_premium_titulo),
                descripcion = stringResource(R.string.armario_premium_texto),
                onVerPlanes = onPremium,
            )
        }
        else -> {
            val sufijoCopia = stringResource(R.string.copia)
            WardrobeScreenContent(
                state = state,
                onPestana = { wardrobeViewModel.cambiarPestana(it) },
                onFiltroCategoria = { wardrobeViewModel.filtrarCategoria(it) },
                onFiltroColor = { wardrobeViewModel.filtrarColor(it) },
                onNuevaPrenda = onNuevaPrenda,
                onPrenda = { onPrenda(it.id) },
                onNuevoOutfit = onNuevoOutfit,
                onOutfit = { onOutfit(it.id) },
                onFavorito = { wardrobeViewModel.marcarFavorito(it) },
                onAbrirMenu = { wardrobeViewModel.abrirMenu(it.id) },
                onCerrarMenu = { wardrobeViewModel.cerrarMenu() },
                onDuplicar = { wardrobeViewModel.duplicar(it, sufijoCopia) },
                onBorrarOutfit = { wardrobeViewModel.pedirBorrarOutfit(it) },
                onConversacion = onConversacion,
                onEditarPreferencias = onEditarPreferencias,
                onRehacerCuestionario = onRehacerCuestionario,
                modifier = modifier,
            )
        }
    }
}

@Composable
fun WardrobeScreenContent(
    state: WardrobeState,
    onPestana: (PestanaArmario) -> Unit,
    onFiltroCategoria: (CategoriaPrenda) -> Unit,
    onFiltroColor: (String) -> Unit,
    onNuevaPrenda: () -> Unit,
    onPrenda: (PrendaUi) -> Unit,
    onNuevoOutfit: () -> Unit,
    onOutfit: (OutfitUi) -> Unit,
    onFavorito: (OutfitUi) -> Unit,
    onAbrirMenu: (OutfitUi) -> Unit,
    onCerrarMenu: () -> Unit,
    onDuplicar: (OutfitUi) -> Unit,
    onBorrarOutfit: (OutfitUi) -> Unit,
    onConversacion: (String) -> Unit,
    onEditarPreferencias: () -> Unit,
    onRehacerCuestionario: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pestanas = listOf(
        PestanaArmario.PRENDAS to R.string.pestana_prendas,
        PestanaArmario.OUTFITS to R.string.pestana_outfits,
        PestanaArmario.MI_ESTILO to R.string.pestana_mi_estilo,
        PestanaArmario.HISTORIAL to R.string.pestana_historial,
    )

    Box(modifier = modifier.fillMaxSize().testTag("wardrobeScreen")) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenTitle(titulo = stringResource(R.string.armario_titulo), modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp))
            ScrollableTabRow(selectedTabIndex = state.pestana.ordinal, edgePadding = 16.dp) {
                pestanas.forEach { (pestana, texto) ->
                    Tab(
                        selected = state.pestana == pestana,
                        onClick = { onPestana(pestana) },
                        text = { Text(text = stringResource(texto)) },
                        modifier = Modifier.testTag("tab_${pestana.name}"),
                    )
                }
            }
            ErrorMessage(messageRes = state.errorMessageRes, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            when (state.pestana) {
                PestanaArmario.PRENDAS -> PestanaPrendas(
                    prendas = state.prendasFiltradas,
                    hayPrendas = state.prendas.isNotEmpty(),
                    filtroCategoria = state.filtroCategoria,
                    filtroColor = state.filtroColor,
                    onFiltroCategoria = onFiltroCategoria,
                    onFiltroColor = onFiltroColor,
                    onPrenda = onPrenda,
                )
                PestanaArmario.OUTFITS -> PestanaOutfits(
                    outfits = state.outfits,
                    nombresDePrendas = { state.nombresDePrendas(it) },
                    onOutfit = onOutfit,
                    onFavorito = onFavorito,
                    outfitConMenu = state.outfitConMenu,
                    onAbrirMenu = onAbrirMenu,
                    onCerrarMenu = onCerrarMenu,
                    onDuplicar = onDuplicar,
                    onBorrar = onBorrarOutfit,
                )
                PestanaArmario.MI_ESTILO -> PestanaMiEstilo(
                    perfil = state.perfil,
                    onEditarPreferencias = onEditarPreferencias,
                    onRehacerCuestionario = onRehacerCuestionario,
                )
                PestanaArmario.HISTORIAL -> PestanaHistorial(
                    historial = state.historial,
                    onOutfit = onOutfit,
                    onConversacion = onConversacion,
                )
            }
        }

        // El + agrega en la pestana donde se esta: una prenda o un outfit
        if (state.pestana == PestanaArmario.PRENDAS || state.pestana == PestanaArmario.OUTFITS) {
            FloatingActionButton(
                onClick = if (state.pestana == PestanaArmario.PRENDAS) onNuevaPrenda else onNuevoOutfit,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .testTag("fabAgregar"),
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(
                        if (state.pestana == PestanaArmario.PRENDAS) R.string.agregar_prenda else R.string.crear_outfit
                    ),
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 393, heightDp = 852)
fun WardrobeScreenPreview() {
    OrloTheme {
        WardrobeScreenContent(
            state = WardrobeState(
                isLoading = false,
                cuenta = LocalPreviewProvider.cuenta,
                prendas = LocalPreviewProvider.prendas,
                outfits = LocalPreviewProvider.outfits,
            ),
            onPestana = {}, onFiltroCategoria = {}, onFiltroColor = {}, onNuevaPrenda = {}, onPrenda = {},
            onNuevoOutfit = {}, onOutfit = {}, onFavorito = {}, onAbrirMenu = {}, onCerrarMenu = {},
            onDuplicar = {}, onBorrarOutfit = {},
            onConversacion = {}, onEditarPreferencias = {}, onRehacerCuestionario = {},
        )
    }
}
