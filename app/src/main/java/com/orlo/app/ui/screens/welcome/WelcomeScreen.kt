package com.orlo.app.ui.screens.welcome

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.orlo.app.R
import com.orlo.app.ui.theme.OrloTheme
import com.orlo.app.ui.utils.AppButton
import com.orlo.app.ui.utils.AppOutlinedButton

// Pantalla 1: la propuesta en una frase, "Empezar" y "Ya tengo cuenta". No necesita ViewModel:
// no tiene estado ni datos
@Composable
fun WelcomeScreen(
    onEmpezar: () -> Unit,
    onYaTengoCuenta: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize().testTag("welcomeScreen")) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(1F))

            // El logo: el aro colgado del gancho, con el color de acento del tema
            Icon(
                painter = painterResource(R.drawable.ic_orlo_simbolo),
                contentDescription = stringResource(R.string.logo_descripcion),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(width = 64.dp, height = 80.dp),
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.marca),
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.bienvenida_propuesta),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.weight(1F))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AppButton(
                    texto = stringResource(R.string.empezar),
                    onClick = onEmpezar,
                    modifier = Modifier.testTag("btnEmpezar"),
                )
                AppOutlinedButton(
                    texto = stringResource(R.string.ya_tengo_cuenta),
                    onClick = onYaTengoCuenta,
                    modifier = Modifier.testTag("btnYaTengoCuenta"),
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 393, heightDp = 852)
fun WelcomeScreenPreview() {
    OrloTheme {
        WelcomeScreen(onEmpezar = {}, onYaTengoCuenta = {})
    }
}

@Composable
@Preview(showBackground = true, widthDp = 393, heightDp = 852, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun WelcomeScreenOscuroPreview() {
    OrloTheme {
        WelcomeScreen(onEmpezar = {}, onYaTengoCuenta = {})
    }
}
