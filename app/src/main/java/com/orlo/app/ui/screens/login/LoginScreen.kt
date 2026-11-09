package com.orlo.app.ui.screens.login

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
fun LoginScreen(
    loginViewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onCrearCuenta: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by loginViewModel.uiState.collectAsState()

    LaunchedEffect(state.navigate) {
        if (state.navigate) onLoginSuccess()
    }

    LoginScreenContent(
        email = state.email,
        onEmailChange = { loginViewModel.updateEmail(it) },
        contrasena = state.contrasena,
        onContrasenaChange = { loginViewModel.updateContrasena(it) },
        contrasenaVisible = state.contrasenaVisible,
        onContrasenaVisibleChange = { loginViewModel.mostrarEsconderContrasena() },
        errorMessageRes = state.errorMessageRes,
        cargando = state.cargando,
        onLoginClick = { loginViewModel.loginButtonPressed() },
        onCrearCuenta = onCrearCuenta,
        modifier = modifier,
    )
}

@Composable
fun LoginScreenContent(
    email: String,
    onEmailChange: (String) -> Unit,
    contrasena: String,
    onContrasenaChange: (String) -> Unit,
    contrasenaVisible: Boolean,
    onContrasenaVisibleChange: () -> Unit,
    @StringRes errorMessageRes: Int?,
    cargando: Boolean,
    onLoginClick: () -> Unit,
    onCrearCuenta: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Con el teclado abierto el formulario se desplaza
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 32.dp)
            .testTag("loginScreen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenTitle(
            titulo = stringResource(R.string.login_titulo),
            subtitulo = stringResource(R.string.login_subtitulo),
        )
        Spacer(modifier = Modifier.height(8.dp))
        AppTextField(
            value = email,
            onValueChange = onEmailChange,
            label = stringResource(R.string.campo_correo),
            keyboardType = KeyboardType.Email,
            modifier = Modifier.testTag("txtEmail"),
        )
        AppTextField(
            value = contrasena,
            onValueChange = onContrasenaChange,
            label = stringResource(R.string.campo_contrasena),
            keyboardType = KeyboardType.Password,
            esContrasena = true,
            contrasenaVisible = contrasenaVisible,
            onContrasenaVisibleChange = onContrasenaVisibleChange,
            modifier = Modifier.testTag("txtPassword"),
        )
        ErrorMessage(messageRes = errorMessageRes)
        AppButton(
            texto = stringResource(R.string.entrar),
            onClick = onLoginClick,
            cargando = cargando,
            modifier = Modifier.testTag("btnLogin"),
        )
        TextButton(onClick = onCrearCuenta, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(text = stringResource(R.string.no_tengo_cuenta))
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 393, heightDp = 852)
fun LoginScreenPreview() {
    OrloTheme {
        LoginScreenContent(
            email = "", onEmailChange = {}, contrasena = "", onContrasenaChange = {},
            contrasenaVisible = false, onContrasenaVisibleChange = {}, errorMessageRes = null,
            cargando = false, onLoginClick = {}, onCrearCuenta = {},
        )
    }
}
