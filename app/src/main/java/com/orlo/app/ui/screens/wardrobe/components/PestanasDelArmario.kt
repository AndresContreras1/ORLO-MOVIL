package com.orlo.app.ui.screens.wardrobe.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.orlo.app.R
import com.orlo.app.data.model.CategoriaPrenda
import com.orlo.app.data.model.ElementoHistorialUi
import com.orlo.app.data.model.OutfitUi
import com.orlo.app.data.model.PerfilUi
import com.orlo.app.data.model.PrendaUi
import com.orlo.app.data.model.coloresDePrenda
import com.orlo.app.ui.screens.result.components.TarjetaDeEstilo
import com.orlo.app.ui.utils.AppButton
import com.orlo.app.ui.utils.AppOutlinedButton
import com.orlo.app.ui.utils.EmptyMessage
import com.orlo.app.ui.utils.formatearFecha

// Prendas: filtros por categoria y color, y la cuadricula
@Composable
fun PestanaPrendas(
    prendas: List<PrendaUi>,
    hayPrendas: Boolean,
    filtroCategoria: CategoriaPrenda?,
    filtroColor: String?,
    onFiltroCategoria: (CategoriaPrenda) -> Unit,
    onFiltroColor: (String) -> Unit,
    onPrenda: (PrendaUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        modifier = modifier.fillMaxSize().testTag("pestanaPrendas"),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CategoriaPrenda.entries) { categoria ->
                    FilterChip(
                        selected = filtroCategoria == categoria,
                        onClick = { onFiltroCategoria(categoria) },
                        label = { Text(text = stringResource(categoria.etiqueta)) },
                    )
                }
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            // Solo los colores que hay en el armario
            val colores = coloresDePrenda.filter { color -> prendasConColor(prendas, filtroColor, color.codigo) }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(colores) { color ->
                    FilterChip(
                        selected = filtroColor == color.codigo,
                        onClick = { onFiltroColor(color.codigo) },
                        label = { Text(text = stringResource(color.etiqueta)) },
                    )
                }
            }
        }
        if (prendas.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                EmptyMessage(messageRes = if (hayPrendas) R.string.sin_prendas_filtro else R.string.sin_prendas)
            }
        }
        items(prendas, key = { it.id }) { prenda ->
            PrendaCard(prenda = prenda, onClick = { onPrenda(prenda) }, modifier = Modifier.testTag("prenda_${prenda.id}"))
        }
    }
}

private fun prendasConColor(prendas: List<PrendaUi>, filtroColor: String?, color: String): Boolean =
    filtroColor == color || prendas.any { it.color == color }

// Outfits: los guardados del asesor y los armados a mano
@Composable
fun PestanaOutfits(
    outfits: List<OutfitUi>,
    nombresDePrendas: (OutfitUi) -> List<String>,
    onOutfit: (OutfitUi) -> Unit,
    onFavorito: (OutfitUi) -> Unit,
    onDuplicar: (OutfitUi) -> Unit,
    onBorrar: (OutfitUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().testTag("pestanaOutfits"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (outfits.isEmpty()) {
            item { EmptyMessage(messageRes = R.string.sin_outfits) }
        }
        items(outfits, key = { it.id }) { outfit ->
            OutfitCard(
                outfit = outfit,
                prendas = nombresDePrendas(outfit),
                onClick = { onOutfit(outfit) },
                onFavorito = { onFavorito(outfit) },
                onDuplicar = { onDuplicar(outfit) },
                onBorrar = { onBorrar(outfit) },
            )
        }
    }
}

// Mi estilo: el resultado actual, editar preferencias o rehacer el cuestionario
@Composable
fun PestanaMiEstilo(
    perfil: PerfilUi,
    onEditarPreferencias: () -> Unit,
    onRehacerCuestionario: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("pestanaMiEstilo"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val resultado = perfil.resultadoActual
        if (resultado != null) {
            TarjetaDeEstilo(resultado = resultado)
        } else {
            EmptyMessage(messageRes = R.string.sin_resultado)
        }
        Text(
            text = stringResource(R.string.mi_estilo_texto),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AppButton(texto = stringResource(R.string.editar_preferencias), onClick = onEditarPreferencias)
        AppOutlinedButton(
            texto = stringResource(R.string.rehacer_cuestionario),
            onClick = onRehacerCuestionario,
            modifier = Modifier.testTag("btnRehacerCuestionario"),
        )
    }
}

// Historial: resultados, outfits y conversaciones por fecha. Desde cada uno se puede volver a abrir
@Composable
fun PestanaHistorial(
    historial: List<ElementoHistorialUi>,
    onOutfit: (OutfitUi) -> Unit,
    onConversacion: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().testTag("pestanaHistorial"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (historial.isEmpty()) {
            item { EmptyMessage(messageRes = R.string.sin_historial) }
        }
        items(historial) { elemento ->
            val datos: Triple<String, String, (() -> Unit)?> = when (elemento) {
                is ElementoHistorialUi.Resultado -> Triple(
                    stringResource(R.string.historial_resultado),
                    elemento.resultado.nombreCompleto,
                    null,
                )
                is ElementoHistorialUi.Outfit -> Triple(
                    stringResource(R.string.historial_outfit),
                    elemento.outfit.nombre,
                    { onOutfit(elemento.outfit) },
                )
                is ElementoHistorialUi.Conversacion -> Triple(
                    stringResource(R.string.historial_conversacion),
                    elemento.conversacion.titulo,
                    { onConversacion(elemento.conversacion.id) },
                )
            }
            ElementoDeHistorial(titulo = datos.first, detalle = datos.second, fecha = elemento.fecha, onClick = datos.third)
        }
    }
}

@Composable
private fun ElementoDeHistorial(titulo: String, detalle: String, fecha: Long, onClick: (() -> Unit)?) {
    val colores = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val contenido: @Composable () -> Unit = {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = "$titulo · ${formatearFecha(fecha)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = detalle, style = MaterialTheme.typography.bodyLarge)
        }
    }
    if (onClick != null) {
        Card(onClick = onClick, colors = colores, modifier = Modifier.fillMaxWidth()) { contenido() }
    } else {
        Card(colors = colores, modifier = Modifier.fillMaxWidth()) { contenido() }
    }
}
