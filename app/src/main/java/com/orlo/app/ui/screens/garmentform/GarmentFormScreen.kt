package com.orlo.app.ui.screens.garmentform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.orlo.app.data.model.CategoriaPrenda
import com.orlo.app.data.model.colorDePrenda
import com.orlo.app.data.model.coloresDePrenda
import com.orlo.app.ui.theme.OrloTheme
import com.orlo.app.ui.utils.AppButton
import com.orlo.app.ui.utils.AppTextField
import com.orlo.app.ui.utils.ConfirmDialog
import com.orlo.app.ui.utils.ErrorMessage
import com.orlo.app.ui.utils.LoadingBox
import com.orlo.app.ui.utils.OrloAsyncImage
import com.orlo.app.ui.utils.PickImageButton
import com.orlo.app.ui.utils.SelectorDeChips
import com.orlo.app.ui.utils.SelectorDeColor
import com.orlo.app.ui.utils.colorDeHex

// Atajos del catalogo generico, para quien no quiere subir fotos
private val sugerencias = listOf(
    R.string.sugerencia_camiseta_blanca to CategoriaPrenda.ARRIBA,
    R.string.sugerencia_camisa to CategoriaPrenda.ARRIBA,
    R.string.sugerencia_jean to CategoriaPrenda.ABAJO,
    R.string.sugerencia_pantalon_tela to CategoriaPrenda.ABAJO,
    R.string.sugerencia_chaqueta to CategoriaPrenda.ABRIGO,
    R.string.sugerencia_tenis to CategoriaPrenda.CALZADO,
    R.string.sugerencia_zapatos to CategoriaPrenda.CALZADO,
)

@Composable
fun GarmentFormScreen(
    garmentFormViewModel: GarmentFormViewModel,
    prendaId: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by garmentFormViewModel.uiState.collectAsState()

    LaunchedEffect(prendaId) {
        garmentFormViewModel.cargar(prendaId)
    }

    LaunchedEffect(state.navigateBack) {
        if (state.navigateBack) onBack()
    }

    if (state.confirmarBorrado) {
        ConfirmDialog(
            titulo = stringResource(R.string.borrar_prenda_titulo),
            texto = stringResource(R.string.borrar_prenda_texto),
            confirmar = stringResource(R.string.borrar),
            onConfirmar = { garmentFormViewModel.borrar() },
            onCancelar = { garmentFormViewModel.cancelarBorrado() },
        )
    }

    val textosSugerencias = sugerencias.map { (texto, categoria) -> stringResource(texto) to categoria }
    val coloresConEtiqueta = coloresDePrenda.map { Triple(it.codigo, it.hex, stringResource(it.etiqueta)) }
    val categorias = CategoriaPrenda.entries.map { it.codigo to stringResource(it.etiqueta) }

    Column(modifier = modifier.fillMaxSize()) {
        IconButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp, top = 8.dp)) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.atras))
        }
        if (state.isLoading) {
            LoadingBox()
            return@Column
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .testTag("garmentFormScreen"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(if (state.esNueva) R.string.nueva_prenda else R.string.editar_prenda),
                style = MaterialTheme.typography.headlineMedium,
            )

            // La foto: la nueva elegida, la que ya tenia o un cuadro con el color
            val foto = state.fotoNueva ?: state.prenda.fotoUrl
            if (foto != null) {
                OrloAsyncImage(
                    url = foto,
                    contentDescription = state.prenda.nombre,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1F).clip(MaterialTheme.shapes.medium),
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2F)
                        .clip(MaterialTheme.shapes.medium)
                        .background(colorDeHex(colorDePrenda(state.prenda.color)?.hex ?: "#F1ECE7")),
                ) {
                    Text(text = stringResource(R.string.sin_foto), style = MaterialTheme.typography.bodyMedium)
                }
            }
            PickImageButton(
                texto = stringResource(if (foto == null) R.string.subir_foto else R.string.cambiar_foto),
                onImagePicked = { garmentFormViewModel.updateFoto(it) },
            )

            if (state.esNueva) {
                Text(text = stringResource(R.string.o_elige_rapido), style = MaterialTheme.typography.titleMedium)
                SelectorDeChips(
                    opciones = textosSugerencias.map { it.first to it.first },
                    seleccion = setOf(state.prenda.nombre),
                    onToggle = { nombre ->
                        val categoria = textosSugerencias.first { it.first == nombre }.second
                        garmentFormViewModel.usarSugerencia(nombre, categoria)
                    },
                )
            }

            AppTextField(
                value = state.prenda.nombre,
                onValueChange = { garmentFormViewModel.updateNombre(it) },
                label = stringResource(R.string.nombre_opcional),
                modifier = Modifier.testTag("txtNombrePrenda"),
            )

            Text(text = stringResource(R.string.categoria), style = MaterialTheme.typography.titleMedium)
            SelectorDeChips(
                opciones = categorias,
                seleccion = setOf(state.prenda.categoria.codigo),
                onToggle = { garmentFormViewModel.updateCategoria(CategoriaPrenda.desdeCodigo(it)) },
            )

            Text(text = stringResource(R.string.color_principal), style = MaterialTheme.typography.titleMedium)
            SelectorDeColor(
                opciones = coloresConEtiqueta,
                seleccion = state.prenda.color,
                onSeleccion = { garmentFormViewModel.updateColor(it) },
            )

            ErrorMessage(messageRes = state.errorMessageRes)
            AppButton(
                texto = stringResource(R.string.guardar),
                onClick = { garmentFormViewModel.guardar() },
                cargando = state.guardando,
                modifier = Modifier.testTag("btnGuardarPrenda"),
            )
            if (!state.esNueva) {
                TextButton(onClick = { garmentFormViewModel.pedirBorrado() }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(text = stringResource(R.string.borrar_prenda), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun GarmentFormTitlePreview() {
    OrloTheme {
        Text(text = "Nueva prenda", style = MaterialTheme.typography.headlineMedium)
    }
}
