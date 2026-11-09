package com.orlo.app.ui.screens.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.orlo.app.R
import com.orlo.app.ui.theme.OrloTheme
import com.orlo.app.ui.utils.AppButton
import com.orlo.app.ui.utils.AppTextField
import com.orlo.app.ui.utils.ErrorMessage
import com.orlo.app.ui.utils.ScreenTitle

@Composable
fun SignUpScreen(
    signUpViewModel: SignUpViewModel,
    onSignUpSuccess: () -> Unit,
    onYaTengoCuenta: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by signUpViewModel.uiState.collectAsState()

    LaunchedEffect(state.navigate) {
        if (state.navigate) onSignUpSuccess()
    }

    SignUpScreenContent(
        state = state,
        onNombreChange = { signUpViewModel.updateNombre(it) },
        onEmailChange = { signUpViewModel.updateEmail(it) },
        onContrasenaChange = { signUpViewModel.updateContrasena(it) },
        onConfirmacionChange = { signUpViewModel.updateConfirmacion(it) },
        onContrasenaVisibleChange = { signUpViewModel.mostrarEsconderContrasena() },
        onAceptaTerminosChange = { signUpViewModel.updateAceptaTerminos(it) },
        onRegistrarClick = { signUpViewModel.registrarButtonPressed() },
        onYaTengoCuenta = onYaTengoCuenta,
        modifier = modifier,
    )
}

@Composable
fun SignUpScreenContent(
    state: SignUpState,
    onNombreChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onContrasenaChange: (String) -> Unit,
    onConfirmacionChange: (String) -> Unit,
    onContrasenaVisibleChange: () -> Unit,
    onAceptaTerminosChange: (Boolean) -> Unit,
    onRegistrarClick: () -> Unit,
    onYaTengoCuenta: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 32.dp)
            .testTag("signUpScreen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenTitle(
            titulo = stringResource(R.string.registro_titulo),
            subtitulo = stringResource(R.string.registro_subtitulo),
        )
        AppTextField(
            value = state.nombre,
            onValueChange = onNombreChange,
            label = stringResource(R.string.campo_nombre),
            modifier = Modifier.testTag("txtNombre"),
        )
        AppTextField(
            value = state.email,
            onValueChange = onEmailChange,
            label = stringResource(R.string.campo_correo),
            keyboardType = KeyboardType.Email,
            modifier = Modifier.testTag("txtEmail"),
        )
        AppTextField(
            value = state.contrasena,
            onValueChange = onContrasenaChange,
            label = stringResource(R.string.campo_contrasena),
            keyboardType = KeyboardType.Password,
            esContrasena = true,
            contrasenaVisible = state.contrasenaVisible,
            onContrasenaVisibleChange = onContrasenaVisibleChange,
            modifier = Modifier.testTag("txtPassword"),
        )
        AppTextField(
            value = state.confirmacion,
            onValueChange = onConfirmacionChange,
            label = stringResource(R.string.campo_confirmar_contrasena),
            keyboardType = KeyboardType.Password,
            esContrasena = true,
            contrasenaVisible = state.contrasenaVisible,
            onContrasenaVisibleChange = onContrasenaVisibleChange,
            modifier = Modifier.testTag("txtConfirmPassword"),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = state.aceptaTerminos,
                onCheckedChange = onAceptaTerminosChange,
                modifier = Modifier.testTag("chkTerminos"),
            )
            Text(
                text = stringResource(R.string.acepto_terminos),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        ErrorMessage(messageRes = state.errorMessageRes)
        AppButton(
            texto = stringResource(R.string.crear_cuenta),
            onClick = onRegistrarClick,
            cargando = state.cargando,
            modifier = Modifier.testTag("btnRegister"),
        )
        TextButton(onClick = onYaTengoCuenta, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(text = stringResource(R.string.ya_tengo_cuenta))
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 393, heightDp = 852)
fun SignUpScreenPreview() {
    OrloTheme {
        SignUpScreenContent(
            state = SignUpState(), onNombreChange = {}, onEmailChange = {}, onContrasenaChange = {},
            onConfirmacionChange = {}, onContrasenaVisibleChange = {}, onAceptaTerminosChange = {},
            onRegistrarClick = {}, onYaTengoCuenta = {},
        )
    }
}
