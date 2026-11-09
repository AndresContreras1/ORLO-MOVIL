package com.orlo.app.e2e

import android.Manifest
import android.os.Build
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToString
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.orlo.app.EmuladorDePrueba
import com.orlo.app.MainActivity
import com.orlo.app.data.datasource.impl.firestore.UsuarioFirestoreDataSourceImpl
import com.orlo.app.data.dtos.EstiloFirestoreDto
import com.orlo.app.data.dtos.RegistrarUsuarioDto
import com.orlo.app.data.dtos.ResultadoFirestoreDto
import com.orlo.app.ui.utils.formatearFecha
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// Dos casos de uso completos contra el emulador de Firebase y el backend local, como los haria una
// persona: tocando la pantalla. La actividad se abre despues de preparar los datos de cada caso
@HiltAndroidTest
class EscenariosE2E {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var composeRule = createEmptyComposeRule()

    private var escenario: ActivityScenario<MainActivity>? = null

    @Before
    fun setUp() {
        hiltRule.inject()
        EmuladorDePrueba.conectar()
        Firebase.auth.signOut()
        // El inicio pide el permiso de notificaciones; con el dialogo encima la app queda en pausa
        // y la prueba no ve la pantalla. Se concede antes de abrirla
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val instrumentacion = InstrumentationRegistry.getInstrumentation()
            instrumentacion.uiAutomation.grantRuntimePermission(
                instrumentacion.targetContext.packageName,
                Manifest.permission.POST_NOTIFICATIONS,
            )
        }
    }

    @After
    fun tearDown() = runBlocking {
        escenario?.close()
        val uid = Firebase.auth.currentUser?.uid
        if (uid != null) UsuarioFirestoreDataSourceImpl(Firebase.firestore).borrarDatosDelUsuario(uid)
        EmuladorDePrueba.borrarUsuarioActual()
    }

    private fun abrirApp() {
        escenario = ActivityScenario.launch(MainActivity::class.java)
    }

    // Mientras la actividad no ha puesto su contenido no hay arbol de Compose: cuenta como "todavia no"
    private fun hay(consulta: () -> Boolean): Boolean = try {
        consulta()
    } catch (e: IllegalStateException) {
        false
    }

    private fun esperar(tag: String, timeoutMillis: Long = 15_000) {
        try {
            composeRule.waitUntil(timeoutMillis = timeoutMillis) {
                hay { composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }
            }
        } catch (e: ComposeTimeoutException) {
            throw AssertionError("No aparecio $tag. Pantalla: " + composeRule.onRoot().printToString(), e)
        }
    }

    private fun existe(tag: String): Boolean = hay { composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }

    // La barra de progreso del cuestionario cambia con cada pregunta
    private fun progreso(): String = try {
        composeRule.onNodeWithTag("progresoCuestionario").fetchSemanticsNode().config.toString()
    } catch (e: AssertionError) {
        ""
    } catch (e: IllegalStateException) {
        ""
    }

    private fun cerrarTeclado() {
        Espresso.closeSoftKeyboard()
        composeRule.waitForIdle()
    }

    private fun esperarTexto(texto: String, timeoutMillis: Long = 15_000) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            hay { composeRule.onAllNodesWithText(texto, substring = true).fetchSemanticsNodes().isNotEmpty() }
        }
    }

    // Los nodos cuyo testTag empieza con un prefijo, como "opcion_" o "leccion_"
    private fun conPrefijo(prefijo: String) = SemanticsMatcher("testTag empieza con $prefijo") {
        it.config.getOrElse(SemanticsProperties.TestTag) { "" }.startsWith(prefijo)
    }

    @Test
    fun usuarioNuevo_contrasenaCortaLaCorrigeHaceElCuestionarioYCompletaUnaLeccion() {
        abrirApp()
        // Registro con una contrasena de 4 caracteres: la app la rechaza antes de crear la cuenta
        esperar("welcomeScreen")
        composeRule.onNodeWithTag("btnEmpezar").performClick()
        esperar("signUpScreen")
        composeRule.onNodeWithTag("txtNombre").performTextInput("Laura")
        composeRule.onNodeWithTag("txtEmail").performTextInput(EmuladorDePrueba.correoNuevo())
        composeRule.onNodeWithTag("txtPassword").performTextInput("1234")
        composeRule.onNodeWithTag("txtConfirmPassword").performTextInput("1234")
        composeRule.onNodeWithTag("chkTerminos").performScrollTo().performClick()
        composeRule.onNodeWithTag("btnRegister").performScrollTo().performClick()
        composeRule.onNodeWithTag("errorMessage").assertIsDisplayed()
        assertThat(Firebase.auth.currentUser).isNull()

        // La corrige y sigue: perfil y cuestionario
        composeRule.onNodeWithTag("txtPassword").performTextClearance()
        composeRule.onNodeWithTag("txtPassword").performTextInput("Clave2026")
        composeRule.onNodeWithTag("txtConfirmPassword").performTextClearance()
        composeRule.onNodeWithTag("txtConfirmPassword").performTextInput("Clave2026")
        composeRule.onNodeWithTag("btnRegister").performScrollTo().performClick()
        esperar("profileSetupScreen")
        composeRule.onNodeWithTag("btnGuardarPerfil").performScrollTo().performClick()
        esperar("quizScreen", timeoutMillis = 20_000)

        // Cada pregunta: la primera opcion y siguiente, hasta llegar al resultado
        var preguntas = 0
        while (preguntas < MAX_PREGUNTAS && !existe("resultScreen")) {
            composeRule.waitUntil(timeoutMillis = 10_000) {
                hay { composeRule.onAllNodes(conPrefijo("opcion_")).fetchSemanticsNodes().isNotEmpty() } || existe("resultScreen")
            }
            if (existe("resultScreen")) break
            composeRule.onAllNodes(conPrefijo("opcion_")).onFirst().performClick()
            // En la ultima, mientras se calcula el resultado, puede llegar el resultado en cualquier momento
            composeRule.waitUntil(timeoutMillis = 10_000) { existe("btnSiguiente") || existe("resultScreen") }
            if (existe("resultScreen")) break
            val antes = progreso()
            try {
                composeRule.onNodeWithTag("btnSiguiente").performClick()
            } catch (e: AssertionError) {
                // El resultado reemplazo al cuestionario justo antes del toque
                if (existe("resultScreen")) break else throw e
            }
            composeRule.waitUntil(timeoutMillis = 20_000) { existe("resultScreen") || progreso() != antes }
            preguntas++
        }
        esperar("resultScreen", timeoutMillis = 20_000)
        // El resultado muestra el estilo con su nombre, su descripcion y su paleta
        composeRule.onNodeWithText("Tu estilo").assertIsDisplayed()
        composeRule.onNodeWithTag("paletaDelEstilo", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag("btnContinuarResultado").performScrollTo().performClick()

        // Aprende: abre la primera leccion, la completa y el avance sube
        esperar("homeScreen")
        composeRule.onAllNodesWithText("Aprende").onFirst().performClick()
        esperar("lessonsScreen")
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodes(conPrefijo("leccion_")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("avanceLecciones").assertTextContains("0 de", substring = true)
        composeRule.onAllNodes(conPrefijo("leccion_")).onFirst().performClick()
        esperar("lessonDetailScreen")
        // El boton esta al final de una lista perezosa: hay que desplazarla hasta el
        composeRule.waitUntil(timeoutMillis = 15_000) {
            hay { composeRule.onAllNodes(hasScrollToNodeAction()).fetchSemanticsNodes().isNotEmpty() }
        }
        composeRule.onAllNodes(hasScrollToNodeAction()).onFirst().performScrollToNode(hasTestTag("btnCompletarLeccion"))
        composeRule.onNodeWithTag("btnCompletarLeccion").performClick()
        // Aparece en el mismo lugar del boton cuando Firestore confirma el avance
        esperar("leccionCompletada")
        Espresso.pressBack()
        esperar("lessonsScreen")
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("1 de", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("avanceLecciones").assertTextContains("1 de", substring = true)

        // Al volver a abrirla sigue completada
        composeRule.onAllNodes(conPrefijo("leccion_")).onFirst().performClick()
        esperar("lessonDetailScreen")
        composeRule.waitUntil(timeoutMillis = 15_000) {
            hay { composeRule.onAllNodes(hasScrollToNodeAction()).fetchSemanticsNodes().isNotEmpty() }
        }
        composeRule.onAllNodes(hasScrollToNodeAction()).onFirst().performScrollToNode(hasTestTag("leccionCompletada"))
        composeRule.onNodeWithTag("leccionCompletada").assertIsDisplayed()
    }

    @Test
    fun usuarioRegistrado_agregaPrendaArmaOutfitLoMarcaFavoritoYLoVeEnElHistorial() {
        // Una cuenta que ya hizo el cuestionario: entra directo al inicio
        runBlocking {
            val uid = EmuladorDePrueba.crearUsuario()
            val usuarios = UsuarioFirestoreDataSourceImpl(Firebase.firestore)
            usuarios.registrarUsuario(uid, RegistrarUsuarioDto(name = "Andrés", acceptedTermsAt = 1, createdAt = 1))
            usuarios.guardarResultado(
                uid,
                ResultadoFirestoreDto(
                    primary = EstiloFirestoreDto(id = 1, code = "casual", name = "Casual"),
                    secondary = EstiloFirestoreDto(id = 2, code = "urbano", name = "Urbano"),
                    createdAt = 1,
                ),
            )
        }
        abrirApp()
        esperar("homeScreen", timeoutMillis = 20_000)

        // Una prenda nueva
        composeRule.onAllNodesWithText("Armario").onFirst().performClick()
        esperar("wardrobeScreen")
        composeRule.onNodeWithTag("fabAgregar").performClick()
        esperar("garmentFormScreen")
        composeRule.onNodeWithTag("txtNombrePrenda").performTextInput("Camisa blanca de lino")
        cerrarTeclado()
        composeRule.onNodeWithTag("chip_arriba").performScrollTo().performSemanticsAction(SemanticsActions.OnClick)
        composeRule.onNodeWithTag("color_blanco").performScrollTo().performSemanticsAction(SemanticsActions.OnClick)
        composeRule.onNodeWithTag("color_blanco").assertIsSelected()
        composeRule.onNodeWithTag("btnGuardarPrenda").performScrollTo().performClick()
        esperar("wardrobeScreen")
        esperarTexto("Camisa blanca de lino")

        // Un outfit con esa prenda
        composeRule.onNodeWithTag("tab_OUTFITS").performClick()
        composeRule.onNodeWithTag("fabAgregar").performClick()
        esperar("outfitFormScreen")
        composeRule.onNodeWithTag("txtNombreOutfit").performTextInput("Lunes de clase")
        cerrarTeclado()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodes(conPrefijo("elegir_")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodes(conPrefijo("elegir_")).onFirst().performScrollTo().performSemanticsAction(SemanticsActions.OnClick)
        composeRule.onNodeWithTag("btnGuardarOutfit").performScrollTo().performClick()
        esperar("pestanaOutfits")
        esperarTexto("Lunes de clase")

        // Favorito: el contador del filtro sube y el filtro lo muestra
        composeRule.onNodeWithText("Favoritos (0)").assertIsDisplayed()
        composeRule.onAllNodesWithContentDescription("Marcar como favorito").onFirst().performClick()
        esperarTexto("Favoritos (1)")
        composeRule.onNodeWithTag("filtroOutfitsFavoritos").performClick()
        composeRule.onNodeWithText("Lunes de clase").assertIsDisplayed()

        // El historial lo muestra junto al resultado del cuestionario
        composeRule.onNodeWithTag("tab_HISTORIAL").performClick()
        esperar("pestanaHistorial")
        esperarTexto("Lunes de clase")
        // Con la fecha de hoy
        composeRule.onAllNodesWithText(formatearFecha(System.currentTimeMillis()), substring = true).onFirst().assertIsDisplayed()
        composeRule.onNodeWithText("Casual · urbano", substring = true).assertIsDisplayed()
    }

    companion object {
        const val MAX_PREGUNTAS = 20
    }
}
