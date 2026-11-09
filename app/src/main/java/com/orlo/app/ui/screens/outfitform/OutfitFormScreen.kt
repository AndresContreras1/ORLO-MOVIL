package com.orlo.app.ui.screens.outfitform

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.orlo.app.R
import com.orlo.app.data.model.OcasionOutfit
import com.orlo.app.ui.screens.wardrobe.components.PrendaCard
import com.orlo.app.ui.utils.AppButton
import com.orlo.app.ui.utils.AppTextField
import com.orlo.app.ui.utils.EmptyMessage
import com.orlo.app.ui.utils.ErrorMessage
import com.orlo.app.ui.utils.LoadingBox
import com.orlo.app.ui.utils.SelectorDeChips

@Composable
fun OutfitFormScreen(
    outfitFormViewModel: OutfitFormViewModel,
    outfitId: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by outfitFormViewModel.uiState.collectAsState()

    LaunchedEffect(outfitId) {
        outfitFormViewModel.cargar(outfitId)
    }

    LaunchedEffect(state.navigateBack) {
        if (state.navigateBack) onBack()
    }

    val ocasiones = OcasionOutfit.entries.map { it.codigo to stringResource(it.etiqueta) }

    Column(modifier = modifier.fillMaxSize()) {
        IconButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp, top = 8.dp)) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.atras))
        }
        if (state.isLoading) {
            LoadingBox()
            return@Column
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 140.dp),
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .testTag("outfitFormScreen"),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = stringResource(if (state.esNuevo) R.string.nuevo_outfit else R.string.editar_outfit),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    AppTextField(
                        value = state.outfit.nombre,
                        onValueChange = { outfitFormViewModel.updateNombre(it) },
                        label = stringResource(R.string.nombre_del_outfit),
                        modifier = Modifier.testTag("txtNombreOutfit"),
                    )
                    Text(text = stringResource(R.string.ocasion), style = MaterialTheme.typography.titleMedium)
                    SelectorDeChips(
                        opciones = ocasiones,
                        seleccion = setOf(state.outfit.ocasion.codigo),
                        onToggle = { outfitFormViewModel.updateOcasion(OcasionOutfit.desdeCodigo(it)) },
                    )
                    // Un outfit del asesor trae sus prendas como texto
                    if (state.outfit.prendasTexto.isNotEmpty()) {
                        Text(text = stringResource(R.string.propuesta_del_asesor), style = MaterialTheme.typography.titleMedium)
                        state.outfit.prendasTexto.forEach { Text(text = "• $it", style = MaterialTheme.typography.bodyMedium) }
                        if (state.outfit.porQue.isNotBlank()) {
                            Text(text = state.outfit.porQue, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Text(text = stringResource(R.string.elige_prendas), style = MaterialTheme.typography.titleMedium)
                }
            }
            if (state.prendas.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) { EmptyMessage(messageRes = R.string.sin_prendas_para_outfit) }
            }
            items(state.prendas, key = { it.id }) { prenda ->
                val elegida = prenda.id in state.outfit.prendasIds
                Card(
                    border = if (elegida) BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else null,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    PrendaCard(
                        prenda = prenda,
                        onClick = { outfitFormViewModel.togglePrenda(prenda.id) },
                        modifier = Modifier.testTag("elegir_${prenda.id}"),
                    )
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ErrorMessage(messageRes = state.errorMessageRes)
                    AppButton(
                        texto = stringResource(R.string.guardar_outfit),
                        onClick = { outfitFormViewModel.guardar() },
                        cargando = state.guardando,
                        modifier = Modifier.testTag("btnGuardarOutfit"),
                    )
                }
            }
        }
    }
}
