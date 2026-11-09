package com.orlo.app.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.orlo.app.ui.theme.OrloTheme

// La barra fija de abajo: Inicio, Aprende, Asesor IA, Explora y Mi armario. Es la unica que recibe
// el navController, porque navega a cinco destinos distintos
@Composable
fun BottomNav(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar(modifier = modifier) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.filledIcon else item.outlineIcon,
                        // La ruta: las pruebas e2e encuentran la pestana por ella
                        contentDescription = item.navigateTo
                    )
                },
                label = {
                    Text(
                        text = stringResource(item.label),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                selected = isSelected,
                // Al cambiar de pestana no se apilan pantallas: Inicio queda siempre en la base
                onClick = {
                    if (!isSelected) {
                        navController.navigate(item.navigateTo) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun BottomNavPreview() {
    OrloTheme {
        BottomNav(navController = rememberNavController())
    }
}
