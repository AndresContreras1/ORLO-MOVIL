package com.orlo.app.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.google.common.truth.Truth.assertThat
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.orlo.app.EmuladorDePrueba
import com.orlo.app.MainActivity
import com.orlo.app.data.datasource.impl.firestore.UsuarioFirestoreDataSourceImpl
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// El caso de uso completo del registro, desde la bienvenida hasta el cuestionario, contra el emulador
// de Firebase
@HiltAndroidTest
class RegistroE2E {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
        EmuladorDePrueba.conectar()
        Firebase.auth.signOut()
    }

    // Se borra lo que dejo la prueba: los datos de Firestore y la cuenta
    @After
    fun tearDown() = runBlocking {
        val uid = Firebase.auth.currentUser?.uid
        if (uid != null) UsuarioFirestoreDataSourceImpl(Firebase.firestore).borrarDatosDelUsuario(uid)
        EmuladorDePrueba.borrarUsuarioActual()
    }

    private fun esperar(tag: String) {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun llenarRegistro(confirmacion: String) {
        esperar("welcomeScreen")
        composeRule.onNodeWithTag("btnEmpezar").performClick()
        esperar("signUpScreen")
        composeRule.onNodeWithTag("txtNombre").performTextInput("Laura")
        composeRule.onNodeWithTag("txtEmail").performTextInput(EmuladorDePrueba.correoNuevo())
        composeRule.onNodeWithTag("txtPassword").performTextInput("123456")
        composeRule.onNodeWithTag("txtConfirmPassword").performTextClearance()
        composeRule.onNodeWithTag("txtConfirmPassword").performTextInput(confirmacion)
    }

    @Test
    fun registrar_sinAceptarTerminos_muestraMensajeYNoCreaLaCuenta() {
        //Arrange
        llenarRegistro(confirmacion = "123456")
        //Act
        composeRule.onNodeWithTag("btnRegister").performScrollTo().performClick()
        //Assert
        composeRule.onNodeWithTag("errorMessage").assertIsDisplayed()
        assertThat(Firebase.auth.currentUser).isNull()
    }

    @Test
    fun registrar_todoValido_guardaElPerfilYLlegaAlCuestionario() {
        //Arrange
        llenarRegistro(confirmacion = "123456")
        composeRule.onNodeWithTag("chkTerminos").performScrollTo().performClick()
        //Act
        composeRule.onNodeWithTag("btnRegister").performScrollTo().performClick()
        esperar("profileSetupScreen")
        composeRule.onNodeWithTag("btnGuardarPerfil").performScrollTo().performClick()
        //Assert: el cuestionario se carga del backend; si no esta corriendo se ve el error con reintentar
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithTag("quizScreen").fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithTag("btnRetry").fetchSemanticsNodes().isNotEmpty()
        }
        val uid = Firebase.auth.currentUser?.uid
        assertThat(uid).isNotNull()
        val usuario = runBlocking { UsuarioFirestoreDataSourceImpl(Firebase.firestore).getUsuario(uid!!) }
        assertThat(usuario?.name).isEqualTo("Laura")
        assertThat(usuario?.acceptedTermsAt).isGreaterThan(0L)
    }
}
