package com.orlo.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.School
import androidx.compose.ui.graphics.vector.ImageVector
import com.orlo.app.R

// Una pestana de la barra: el icono lleno es el de la pestana actual y el vacio el de las demas.
// navigateTo es la ruta a la que se navega (sin argumentos opcionales)
data class BottomNavItem(
    val filledIcon: ImageVector,
    val outlineIcon: ImageVector,
    val route: String,
    val navigateTo: String,
    @StringRes val label: Int
)

val bottomNavItems = listOf(
    BottomNavItem(Icons.Filled.Home, Icons.Outlined.Home, Screen.Home.route, Screen.Home.route, R.string.nav_inicio),
    BottomNavItem(Icons.Filled.School, Icons.Outlined.School, Screen.Lessons.route, Screen.Lessons.route, R.string.nav_aprende),
    BottomNavItem(
        Icons.AutoMirrored.Filled.Chat,
        Icons.AutoMirrored.Outlined.Chat,
        Screen.Advisor.route,
        Screen.Advisor.BASE,
        R.string.nav_asesor
    ),
    BottomNavItem(Icons.Filled.Explore, Icons.Outlined.Explore, Screen.Explore.route, Screen.Explore.route, R.string.nav_explora),
    BottomNavItem(Icons.Filled.Checkroom, Icons.Outlined.Checkroom, Screen.Wardrobe.route, Screen.Wardrobe.route, R.string.nav_armario),
)
