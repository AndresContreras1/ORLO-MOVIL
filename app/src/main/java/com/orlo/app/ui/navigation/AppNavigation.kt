package com.orlo.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.orlo.app.ui.screens.account.AccountScreen
import com.orlo.app.ui.screens.advisor.AdvisorScreen
import com.orlo.app.ui.screens.explore.ExploreScreen
import com.orlo.app.ui.screens.garmentform.GarmentFormScreen
import com.orlo.app.ui.screens.home.HomeScreen
import com.orlo.app.ui.screens.lessondetail.LessonDetailScreen
import com.orlo.app.ui.screens.lessons.LessonsScreen
import com.orlo.app.ui.screens.login.LoginScreen
import com.orlo.app.ui.screens.outfitform.OutfitFormScreen
import com.orlo.app.ui.screens.paywall.PaywallScreen
import com.orlo.app.ui.screens.profilesetup.ProfileSetupScreen
import com.orlo.app.ui.screens.quiz.QuizScreen
import com.orlo.app.ui.screens.result.ResultScreen
import com.orlo.app.ui.screens.signup.SignUpScreen
import com.orlo.app.ui.screens.splash.SplashScreen
import com.orlo.app.ui.screens.wardrobe.WardrobeScreen
import com.orlo.app.ui.screens.welcome.WelcomeScreen

// Todas las rutas. Las pantallas reciben lambdas y aqui se decide a donde va cada una: ninguna
// pantalla recibe el navController
@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // Entrar a una seccion principal borra la pila: atras no vuelve al registro ni al cuestionario
    fun irAInicio() = navController.navigate(Screen.Home.route) { popUpTo(0) { inclusive = true } }

    fun irABienvenida() = navController.navigate(Screen.Welcome.route) { popUpTo(0) { inclusive = true } }

    fun irAPremium() = navController.navigate(Screen.Paywall.route)

    fun irAEditarPerfil() = navController.navigate(Screen.ProfileSetup.createRoute(edit = true))

    fun volver() = navController.popBackStack()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier,
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                splashViewModel = hiltViewModel(),
                onBienvenida = { irABienvenida() },
                onPerfil = {
                    navController.navigate(Screen.ProfileSetup.createRoute(edit = false)) { popUpTo(0) { inclusive = true } }
                },
                onCuestionario = { navController.navigate(Screen.Quiz.route) { popUpTo(0) { inclusive = true } } },
                onInicio = { irAInicio() },
            )
        }

        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onEmpezar = { navController.navigate(Screen.SignUp.route) },
                onYaTengoCuenta = { navController.navigate(Screen.Login.route) },
            )
        }

        // Al entrar, el splash decide si falta el perfil, el cuestionario o va al inicio
        composable(Screen.Login.route) {
            LoginScreen(
                loginViewModel = hiltViewModel(),
                onLoginSuccess = { navController.navigate(Screen.Splash.route) { popUpTo(0) { inclusive = true } } },
                onCrearCuenta = {
                    navController.navigate(Screen.SignUp.route) { popUpTo(Screen.Welcome.route) }
                },
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                signUpViewModel = hiltViewModel(),
                onSignUpSuccess = {
                    navController.navigate(Screen.ProfileSetup.createRoute(edit = false)) { popUpTo(0) { inclusive = true } }
                },
                onYaTengoCuenta = {
                    navController.navigate(Screen.Login.route) { popUpTo(Screen.Welcome.route) }
                },
            )
        }

        composable(
            route = Screen.ProfileSetup.route,
            arguments = listOf(navArgument("edit") { type = NavType.BoolType; defaultValue = false }),
        ) {
            val editando = it.arguments?.getBoolean("edit") ?: false
            ProfileSetupScreen(
                profileSetupViewModel = hiltViewModel(),
                editando = editando,
                // En el registro sigue el cuestionario; al editar, se vuelve a donde estaba
                onGuardado = {
                    if (editando) {
                        volver()
                    } else {
                        navController.navigate(Screen.Quiz.route) { popUpTo(0) { inclusive = true } }
                    }
                },
            )
        }

        composable(Screen.Quiz.route) {
            QuizScreen(
                quizViewModel = hiltViewModel(),
                onResultado = { navController.navigate(Screen.Result.route) { popUpTo(0) { inclusive = true } } },
            )
        }

        composable(Screen.Result.route) {
            ResultScreen(
                resultViewModel = hiltViewModel(),
                onContinuar = { irAInicio() },
            )
        }

        // "Ahora no" vuelve a donde estaba; si no hay a donde volver, va al inicio
        composable(Screen.Paywall.route) {
            PaywallScreen(
                paywallViewModel = hiltViewModel(),
                onAhoraNo = {
                    if (!volver()) irAInicio()
                },
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                homeViewModel = hiltViewModel(),
                onAprende = { navController.navigate(Screen.Lessons.route) },
                onAsesor = { navController.navigate(Screen.Advisor.BASE) },
                onExplora = { navController.navigate(Screen.Explore.route) },
                onArmario = { navController.navigate(Screen.Wardrobe.route) },
                onCuenta = { navController.navigate(Screen.Account.route) },
                onPremium = { irAPremium() },
                onCuestionario = { navController.navigate(Screen.Quiz.route) },
            )
        }

        composable(Screen.Lessons.route) {
            LessonsScreen(
                lessonsViewModel = hiltViewModel(),
                onLeccion = { navController.navigate(Screen.LessonDetail.createRoute(it)) },
            )
        }

        composable(
            route = Screen.LessonDetail.route,
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType }),
        ) {
            LessonDetailScreen(
                lessonDetailViewModel = hiltViewModel(),
                leccionId = it.arguments?.getString("lessonId") ?: "",
                onBack = { volver() },
                onPremium = { irAPremium() },
                onCompletarPerfil = { irAEditarPerfil() },
            )
        }

        composable(
            route = Screen.Advisor.route,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType; nullable = true; defaultValue = null }),
        ) {
            AdvisorScreen(
                advisorViewModel = hiltViewModel(),
                conversacionId = it.arguments?.getString("conversationId"),
                onPremium = { irAPremium() },
            )
        }

        composable(Screen.Explore.route) {
            ExploreScreen(onIrAlArmario = { navController.navigate(Screen.Wardrobe.route) })
        }

        composable(Screen.Wardrobe.route) {
            WardrobeScreen(
                wardrobeViewModel = hiltViewModel(),
                onNuevaPrenda = { navController.navigate(Screen.GarmentForm.BASE) },
                onPrenda = { navController.navigate(Screen.GarmentForm.createRoute(it)) },
                onNuevoOutfit = { navController.navigate(Screen.OutfitForm.BASE) },
                onOutfit = { navController.navigate(Screen.OutfitForm.createRoute(it)) },
                onConversacion = { navController.navigate(Screen.Advisor.createRoute(it)) },
                onEditarPreferencias = { irAEditarPerfil() },
                onRehacerCuestionario = { navController.navigate(Screen.Quiz.route) },
                onPremium = { irAPremium() },
            )
        }

        composable(
            route = Screen.GarmentForm.route,
            arguments = listOf(navArgument("garmentId") { type = NavType.StringType; nullable = true; defaultValue = null }),
        ) {
            GarmentFormScreen(
                garmentFormViewModel = hiltViewModel(),
                prendaId = it.arguments?.getString("garmentId"),
                onBack = { volver() },
            )
        }

        composable(
            route = Screen.OutfitForm.route,
            arguments = listOf(navArgument("outfitId") { type = NavType.StringType; nullable = true; defaultValue = null }),
        ) {
            OutfitFormScreen(
                outfitFormViewModel = hiltViewModel(),
                outfitId = it.arguments?.getString("outfitId"),
                onBack = { volver() },
            )
        }

        composable(Screen.Account.route) {
            AccountScreen(
                accountViewModel = hiltViewModel(),
                onBack = { volver() },
                onEditarDatos = { irAEditarPerfil() },
                onPremium = { irAPremium() },
                onSalir = { irABienvenida() },
            )
        }
    }
}
