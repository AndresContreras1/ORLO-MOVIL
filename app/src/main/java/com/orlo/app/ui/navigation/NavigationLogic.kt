package com.orlo.app.ui.navigation

// Decide en que rutas se ve la barra de abajo: solo en las cinco secciones principales. Para la del
// asesor se compara sin los argumentos opcionales
object NavigationLogic {

    private val bottomBarScreens = listOf(
        Screen.Home.route,
        Screen.Lessons.route,
        Screen.Advisor.route,
        Screen.Explore.route,
        Screen.Wardrobe.route,
    )

    fun shouldShowBottomBar(route: String?) = bottomBarScreens.contains(route)
}
