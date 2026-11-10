package com.orlo.app.ui.navigation

// Definicion de las rutas de la aplicacion. Cada objeto tiene una ruta distinta
sealed class Screen(val route: String) {
    // De la bienvenida al resultado
    data object Splash : Screen(route = "splash")
    data object Welcome : Screen(route = "welcome")
    data object Login : Screen(route = "login")
    data object SignUp : Screen(route = "signUp")
    data object Quiz : Screen(route = "quiz")
    data object Result : Screen(route = "result")
    // El plan premium: en esta version todo esta abierto y se abre solo para verlo
    data object Paywall : Screen(route = "paywall")

    // Los datos del perfil: en el registro van seguidos del cuestionario; al editarlos, vuelven atras
    data object ProfileSetup : Screen(route = "profileSetup?edit={edit}") {
        fun createRoute(edit: Boolean) = "profileSetup?edit=$edit"
    }

    // Las cinco pestanas de la barra de abajo
    data object Home : Screen(route = "home")
    data object Lessons : Screen(route = "lessons")
    data object Explore : Screen(route = "explore")
    data object Wardrobe : Screen(route = "wardrobe")

    // El asesor abre una conversacion nueva o una del historial
    data object Advisor : Screen(route = "advisor?conversationId={conversationId}") {
        const val BASE = "advisor"
        fun createRoute(conversationId: String) = "advisor?conversationId=$conversationId"
    }

    data object LessonDetail : Screen(route = "lessonDetail/{lessonId}") {
        fun createRoute(lessonId: String) = "lessonDetail/$lessonId"
    }

    // Crear y editar con la misma pantalla: sin id es una prenda (o un outfit) nueva
    data object GarmentForm : Screen(route = "garmentForm?garmentId={garmentId}") {
        const val BASE = "garmentForm"
        fun createRoute(garmentId: String) = "garmentForm?garmentId=$garmentId"
    }

    data object OutfitForm : Screen(route = "outfitForm?outfitId={outfitId}") {
        const val BASE = "outfitForm"
        fun createRoute(outfitId: String) = "outfitForm?outfitId=$outfitId"
    }

    data object Account : Screen(route = "account")
}
