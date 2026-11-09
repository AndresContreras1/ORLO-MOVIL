package com.orlo.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.orlo.app.ui.navigation.AppNavigation
import com.orlo.app.ui.navigation.BottomNav
import com.orlo.app.ui.navigation.NavigationLogic

// El unico Scaffold de la app: la barra de abajo solo aparece en las cinco secciones principales
@Composable
fun OrloApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (NavigationLogic.shouldShowBottomBar(currentRoute)) {
                BottomNav(navController = navController)
            }
        }
    ) {
        AppNavigation(
            navController = navController,
            modifier = Modifier.padding(it)
        )
    }
}
