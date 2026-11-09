package com.orlo.app.ui.screens.profilesetup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import com.orlo.app.data.model.OpcionColor
import com.orlo.app.data.model.OpcionPerfil
import com.orlo.app.data.model.OpcionesDelPerfil
import com.orlo.app.data.model.PerfilUi
import com.orlo.app.ui.screens.profilesetup.components.SeccionDelPerfil
import com.orlo.app.ui.theme.OrloTheme
import com.orlo.app.ui.utils.AppButton
import com.orlo.app.ui.utils.AppTextField
import com.orlo.app.ui.utils.ErrorBox
import com.orlo.app.ui.utils.ErrorMessage
import com.orlo.app.ui.utils.LoadingBox
import com.orlo.app.ui.utils.ScreenTitle
import com.orlo.app.ui.utils.SelectorDeChips
import com.orlo.app.ui.utils.SelectorDeColor

@Composable
fun ProfileSetupScreen(
    profileSetupViewModel: ProfileSetupViewModel,
    editando: Boolean,
    onGuardado: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by profileSetupViewModel.uiState.collectAsState()

    LaunchedEffect(state.navigate) {
        if (state.navigate) onGuardado()
    }

    val errorCarga = state.errorCargaRes
    when {
        state.isLoading -> LoadingBox(modifier = modifier)
        errorCarga != null -> ErrorBox(
            messageRes = errorCarga,
            onRetry = { profileSetupViewModel.cargarPerfil() },
            modifier = modifier,
        )
        else -> ProfileSetupScreenContent(
            perfil = state.perfil,
            editando = editando,
            errorMessageRes = state.errorMessageRes,
            guardando = state.guardando,
            onNombreChange = { profileSetupViewModel.updateNombre(it) },
            onRangoEdad = { profileSetupViewModel.updateRangoEdad(it) },
            onTratoPrendas = { profileSetupViewModel.updateTratoPrendas(it) },
            onOcasion = { profileSetupViewModel.toggleOcasion(it) },
            onClima = { profileSetupViewModel.updateClima(it) },
            onPresupuesto = { profileSetupViewModel.updatePresupuesto(it) },
            onConsentimiento = { profileSetupViewModel.updateConsentimiento(it) },
            onTipoCuerpo = { profileSetupViewModel.updateTipoCuerpo(it) },
            onTonoPiel = { profileSetupViewModel.updateTonoPiel(it) },
            onColorCabello = { profileSetupViewModel.updateColorCabello(it) },
            onColorOjos = { profileSetupViewModel.updateColorOjos(it) },
            onGuardar = { profileSetupViewModel.guardarButtonPressed() },
            modifier = modifier,
        )
    }
}

@Composable
private fun opcionesDeTexto(opciones: List<OpcionPerfil>): List<Pair<String, String>> =
    opciones.map { it.codigo to stringResource(it.etiqueta) }

@Composable
private fun opcionesDeColor(opciones: List<OpcionColor>): List<Triple<String, String, String>> =
    opciones.map { Triple(it.codigo, it.hex, stringResource(it.etiqueta)) }

@Composable
fun ProfileSetupScreenContent(
    perfil: PerfilUi,
    editando: Boolean,
    errorMessageRes: Int?,
    guardando: Boolean,
    onNombreChange: (String) -> Unit,
    onRangoEdad: (String) -> Unit,
    onTratoPrendas: (String) -> Unit,
    onOcasion: (String) -> Unit,
    onClima: (String) -> Unit,
    onPresupuesto: (String) -> Unit,
    onConsentimiento: (Boolean) -> Unit,
    onTipoCuerpo: (String) -> Unit,
    onTonoPiel: (String) -> Unit,
    onColorCabello: (String) -> Unit,
    onColorOjos: (String) -> Unit,
    onGuardar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .testTag("profileSetupScreen"),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        ScreenTitle(
            titulo = stringResource(if (editando) R.string.perfil_titulo_editar else R.string.perfil_titulo),
            subtitulo = stringResource(R.string.perfil_subtitulo),
        )

        SeccionDelPerfil(titulo = stringResource(R.string.perfil_nombre), paraQue = stringResource(R.string.perfil_nombre_para_que)) {
            AppTextField(
                value = perfil.nombre,
                onValueChange = onNombreChange,
                label = stringResource(R.string.campo_nombre),
                modifier = Modifier.testTag("txtNombrePerfil"),
            )
        }

        SeccionDelPerfil(titulo = stringResource(R.string.perfil_edad), paraQue = stringResource(R.string.perfil_edad_para_que)) {
            SelectorDeChips(
                opciones = opcionesDeTexto(OpcionesDelPerfil.rangosDeEdad),
                seleccion = setOf(perfil.rangoEdad),
                onToggle = onRangoEdad,
            )
        }

        SeccionDelPerfil(titulo = stringResource(R.string.perfil_trato), paraQue = stringResource(R.string.perfil_trato_para_que)) {
            SelectorDeChips(
                opciones = opcionesDeTexto(OpcionesDelPerfil.tratoPrendas),
                seleccion = setOf(perfil.tratoPrendas),
                onToggle = onTratoPrendas,
            )
        }

        SeccionDelPerfil(titulo = stringResource(R.string.perfil_ocasiones), paraQue = stringResource(R.string.perfil_ocasiones_para_que)) {
            SelectorDeChips(
                opciones = opcionesDeTexto(OpcionesDelPerfil.ocasiones),
                seleccion = perfil.ocasiones.toSet(),
                onToggle = onOcasion,
            )
        }

        SeccionDelPerfil(titulo = stringResource(R.string.perfil_clima), paraQue = stringResource(R.string.perfil_clima_para_que)) {
            SelectorDeChips(
                opciones = opcionesDeTexto(OpcionesDelPerfil.climas),
                seleccion = setOf(perfil.clima),
                onToggle = onClima,
            )
        }

        SeccionDelPerfil(titulo = stringResource(R.string.perfil_presupuesto), paraQue = stringResource(R.string.perfil_presupuesto_para_que)) {
            SelectorDeChips(
                opciones = opcionesDeTexto(OpcionesDelPerfil.presupuestos),
                seleccion = setOf(perfil.presupuesto),
                onToggle = onPresupuesto,
            )
        }

        // Los datos de apariencia son sensibles: se piden con explicacion y consentimiento claro, y
        // la app funciona sin ellos
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.perfil_apariencia),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1F),
                    )
                    Switch(
                        checked = perfil.consentimientoDatosSensibles,
                        onCheckedChange = onConsentimiento,
                        modifier = Modifier.testTag("switchConsentimiento"),
                    )
                }
                Text(
                    text = stringResource(R.string.perfil_apariencia_consentimiento),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (perfil.consentimientoDatosSensibles) {
            SeccionDelPerfil(titulo = stringResource(R.string.perfil_cuerpo), paraQue = stringResource(R.string.perfil_cuerpo_para_que)) {
                SelectorDeChips(
                    opciones = opcionesDeTexto(OpcionesDelPerfil.tiposDeCuerpo),
                    seleccion = setOf(perfil.tipoCuerpo),
                    onToggle = onTipoCuerpo,
                )
            }
            SeccionDelPerfil(titulo = stringResource(R.string.perfil_piel), paraQue = stringResource(R.string.perfil_colores_para_que)) {
                SelectorDeColor(
                    opciones = opcionesDeColor(OpcionesDelPerfil.tonosDePiel),
                    seleccion = perfil.tonoPiel,
                    onSeleccion = onTonoPiel,
                )
            }
            SeccionDelPerfil(titulo = stringResource(R.string.perfil_cabello), paraQue = stringResource(R.string.perfil_colores_para_que)) {
                SelectorDeColor(
                    opciones = opcionesDeColor(OpcionesDelPerfil.coloresDeCabello),
                    seleccion = perfil.colorCabello,
                    onSeleccion = onColorCabello,
                )
            }
            SeccionDelPerfil(titulo = stringResource(R.string.perfil_ojos), paraQue = stringResource(R.string.perfil_colores_para_que)) {
                SelectorDeColor(
                    opciones = opcionesDeColor(OpcionesDelPerfil.coloresDeOjos),
                    seleccion = perfil.colorOjos,
                    onSeleccion = onColorOjos,
                )
            }
        }

        ErrorMessage(messageRes = errorMessageRes)
        AppButton(
            texto = stringResource(if (editando) R.string.guardar_cambios else R.string.continuar),
            onClick = onGuardar,
            cargando = guardando,
            modifier = Modifier.testTag("btnGuardarPerfil"),
        )
    }
}

@Composable
@Preview(showBackground = true, widthDp = 393, heightDp = 1400)
fun ProfileSetupScreenPreview() {
    OrloTheme {
        ProfileSetupScreenContent(
            perfil = PerfilUi(nombre = "Laura", consentimientoDatosSensibles = true),
            editando = false, errorMessageRes = null, guardando = false,
            onNombreChange = {}, onRangoEdad = {}, onTratoPrendas = {}, onOcasion = {}, onClima = {},
            onPresupuesto = {}, onConsentimiento = {}, onTipoCuerpo = {}, onTonoPiel = {},
            onColorCabello = {}, onColorOjos = {}, onGuardar = {},
        )
    }
}
