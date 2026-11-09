package com.orlo.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = Acento,
    onPrimary = SobreAcento,
    primaryContainer = AcentoSuave,
    onPrimaryContainer = AcentoOscuro,
    secondary = AcentoOscuro,
    onSecondary = SobreAcento,
    background = FondoClaro,
    onBackground = TextoPrincipal,
    surface = SuperficieClara,
    onSurface = TextoPrincipal,
    surfaceVariant = SuperficieVariante,
    onSurfaceVariant = TextoSecundario,
    outline = Borde,
    error = ErrorClaro,
)

private val DarkColorScheme = darkColorScheme(
    primary = AcentoNoche,
    onPrimary = FondoOscuro,
    primaryContainer = AcentoOscuro,
    onPrimaryContainer = TextoPrincipalOscuro,
    secondary = AcentoNoche,
    onSecondary = FondoOscuro,
    background = FondoOscuro,
    onBackground = TextoPrincipalOscuro,
    surface = SuperficieOscura,
    onSurface = TextoPrincipalOscuro,
    surfaceVariant = SuperficieVarianteOscura,
    onSurfaceVariant = TextoSecundarioOscuro,
    outline = BordeOscuro,
    error = ErrorOscuro,
)

// Bordes redondeados en tarjetas, botones y campos: formas simples, sin sombras fuertes
private val OrloShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

// La marca tiene colores propios: no se usa el color dinamico del fondo de pantalla del celular
@Composable
fun OrloTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = OrloShapes,
        content = content
    )
}
