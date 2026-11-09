package com.orlo.app.ui.screens.garmentform

import android.net.Uri
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.orlo.app.R
import com.orlo.app.data.local.LocalPreviewProvider
import com.orlo.app.data.model.CategoriaPrenda
import com.orlo.app.data.model.colorDePrenda
import com.orlo.app.data.model.coloresDePrenda
import com.orlo.app.ui.theme.OrloTheme
import com.orlo.app.ui.utils.AppButton
import com.orlo.app.ui.utils.AppTextField
import com.orlo.app.ui.utils.ConfirmDialog
import com.orlo.app.ui.utils.ErrorBox
import com.orlo.app.ui.utils.ErrorMessage
import com.orlo.app.ui.utils.LoadingBox
import com.orlo.app.ui.utils.OrloAsyncImage
import com.orlo.app.ui.utils.SelectorDeFoto
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

    Column(modifier = modifier.fillMaxSize().testTag("garmentFormScreen")) {
        IconButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp, top = 8.dp)) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.atras))
        }
        val errorCarga = state.errorCargaRes
        when {
            state.isLoading -> LoadingBox()
            errorCarga != null -> ErrorBox(messageRes = errorCarga, onRetry = { garmentFormViewModel.cargar(prendaId) })
            else -> GarmentFormScreenContent(
                state = state,
                onFoto = { garmentFormViewModel.updateFoto(it) },
                onSugerencia = { nombre, categoria -> garmentFormViewModel.usarSugerencia(nombre, categoria) },
                onNombre = { garmentFormViewModel.updateNombre(it) },
                onCategoria = { garmentFormViewModel.updateCategoria(it) },
                onColor = { garmentFormViewModel.updateColor(it) },
                onGuardar = { garmentFormViewModel.guardar() },
                onBorrar = { garmentFormViewModel.pedirBorrado() },
            )
        }
    }
}

@Composable
fun GarmentFormScreenContent(
    state: GarmentFormState,
    onFoto: (Uri) -> Unit,
    onSugerencia: (String, CategoriaPrenda) -> Unit,
    onNombre: (String) -> Unit,
    onCategoria: (CategoriaPrenda) -> Unit,
    onColor: (String) -> Unit,
    onGuardar: () -> Unit,
    onBorrar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val textosSugerencias = sugerencias.map { (texto, categoria) -> stringResource(texto) to categoria }
    val coloresConEtiqueta = coloresDePrenda.map { Triple(it.codigo, it.hex, stringResource(it.etiqueta)) }
    val categorias = CategoriaPrenda.entries.map { it.codigo to stringResource(it.etiqueta) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
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
            // Sin color elegido el cuadro usa el color de superficie del tema (claro u oscuro)
            val fondo = colorDePrenda(state.prenda.color)?.let { colorDeHex(it.hex) }
                ?: MaterialTheme.colorScheme.surfaceVariant
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2F)
                    .clip(MaterialTheme.shapes.medium)
                    .background(fondo),
            ) {
                // El texto se lee sobre colores claros y oscuros
                Text(
                    text = stringResource(R.string.sin_foto),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (fondo.luminance() > 0.5F) Color.Black else Color.White,
                )
            }
        }
        SelectorDeFoto(onFoto = onFoto)

        if (state.esNueva) {
            Text(text = stringResource(R.string.o_elige_rapido), style = MaterialTheme.typography.titleMedium)
            SelectorDeChips(
                opciones = textosSugerencias.map { it.first to it.first },
                seleccion = setOf(state.prenda.nombre),
                onToggle = { nombre ->
                    val categoria = textosSugerencias.first { it.first == nombre }.second
                    onSugerencia(nombre, categoria)
                },
            )
        }

        AppTextField(
            value = state.prenda.nombre,
            onValueChange = onNombre,
            label = stringResource(R.string.nombre_opcional),
            modifier = Modifier.testTag("txtNombrePrenda"),
        )

        Text(text = stringResource(R.string.categoria), style = MaterialTheme.typography.titleMedium)
        SelectorDeChips(
            opciones = categorias,
            seleccion = setOf(state.prenda.categoria.codigo),
            onToggle = { onCategoria(CategoriaPrenda.desdeCodigo(it)) },
        )

        Text(text = stringResource(R.string.color_principal), style = MaterialTheme.typography.titleMedium)
        SelectorDeColor(
            opciones = coloresConEtiqueta,
            seleccion = state.prenda.color,
            onSeleccion = onColor,
        )

        ErrorMessage(messageRes = state.errorMessageRes)
        AppButton(
            texto = stringResource(R.string.guardar),
            onClick = onGuardar,
            cargando = state.guardando,
            modifier = Modifier.testTag("btnGuardarPrenda"),
        )
        if (!state.esNueva) {
            TextButton(onClick = onBorrar, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(text = stringResource(R.string.borrar_prenda), color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 393, heightDp = 1100)
fun GarmentFormScreenPreview() {
    OrloTheme {
        GarmentFormScreenContent(
            state = GarmentFormState(),
            onFoto = {}, onSugerencia = { _, _ -> }, onNombre = {}, onCategoria = {}, onColor = {},
            onGuardar = {}, onBorrar = {},
        )
    }
}

@Composable
@Preview(showBackground = true, widthDp = 393, heightDp = 1100)
fun GarmentFormScreenEditarOscuroPreview() {
    OrloTheme(darkTheme = true) {
        GarmentFormScreenContent(
            state = GarmentFormState(prenda = LocalPreviewProvider.prendas.first(), esNueva = false),
            onFoto = {}, onSugerencia = { _, _ -> }, onNombre = {}, onCategoria = {}, onColor = {},
            onGuardar = {}, onBorrar = {},
        )
    }
}
