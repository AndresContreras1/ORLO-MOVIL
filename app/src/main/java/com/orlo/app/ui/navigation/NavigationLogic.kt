package com.orlo.app.ui.navigation

// Decide en que rutas se ve la barra de abajo: solo en las cinco secciones principales. La ruta que
// llega es el patron del destino ("advisor?conversationId={conversationId}"), asi que el asesor
// coincide con o sin conversacion abierta
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
