package com.orlo.app.viewmodels

import com.google.common.truth.Truth.assertThat
import com.orlo.app.R
import com.orlo.app.data.model.CuestionarioUi
import com.orlo.app.data.model.EstiloUi
import com.orlo.app.data.model.OpcionUi
import com.orlo.app.data.model.PreguntaUi
import com.orlo.app.data.model.ResultadoUi
import com.orlo.app.data.repository.CuestionarioRepository
import com.orlo.app.data.repository.SinConexionException
import com.orlo.app.data.repository.UsuarioRepository
import com.orlo.app.ui.screens.quiz.QuizViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var cuestionarioRepository: CuestionarioRepository
    private lateinit var usuarioRepository: UsuarioRepository
    private lateinit var viewModel: QuizViewModel

    private val cuestionario = CuestionarioUi(
        version = 1,
        preguntas = listOf(
            PreguntaUi(id = 1, texto = "¿Qué usas más?", multiple = false, opciones = listOf(OpcionUi(10, "Jeans"), OpcionUi(11, "Vestidos"))),
            PreguntaUi(id = 2, texto = "¿Qué colores usas?", multiple = true, opciones = listOf(OpcionUi(20, "Negro"), OpcionUi(21, "Azul"))),
        ),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        cuestionarioRepository = mockk()
        usuarioRepository = mockk()
        coEvery { cuestionarioRepository.getCuestionario() } returns Result.success(cuestionario)
        viewModel = QuizViewModel(cuestionarioRepository, usuarioRepository, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun seleccionarOpcion_preguntaDeUnaOpcion_reemplazaLaAnterior() = runTest {
        //Arrange
        advanceUntilIdle()
        //Act
        viewModel.seleccionarOpcion(10)
        viewModel.seleccionarOpcion(11)
        //Assert
        assertThat(viewModel.uiState.value.seleccionActual).containsExactly(11L)
    }

    @Test
    fun seleccionarOpcion_preguntaDeVarias_alternaLasOpciones() = runTest {
        //Arrange
        advanceUntilIdle()
        viewModel.seleccionarOpcion(10)
        viewModel.siguiente()
        //Act
        viewModel.seleccionarOpcion(20)
        viewModel.seleccionarOpcion(21)
        viewModel.seleccionarOpcion(20)
        //Assert
        assertThat(viewModel.uiState.value.indice).isEqualTo(1)
        assertThat(viewModel.uiState.value.seleccionActual).containsExactly(21L)
    }

    @Test
    fun siguiente_sinElegir_muestraErrorYNoAvanza() = runTest {
        //Arrange
        advanceUntilIdle()
        //Act
        viewModel.siguiente()
        //Assert
        assertThat(viewModel.uiState.value.indice).isEqualTo(0)
        assertThat(viewModel.uiState.value.errorMessageRes).isEqualTo(R.string.error_elige_una_opcion)
    }

    @Test
    fun siguiente_ultimaPreguntaRespondida_guardaElResultadoYNavega() = runTest {
        //Arrange
        val resultado = ResultadoUi(principal = EstiloUi(code = "urbano", nombre = "Urbano"))
        coEvery { cuestionarioRepository.puntuar(any(), any()) } returns Result.success(resultado)
        coEvery { usuarioRepository.guardarResultado(resultado) } returns Result.success(resultado.copy(id = "r1"))
        advanceUntilIdle()
        viewModel.seleccionarOpcion(10)
        viewModel.siguiente()
        viewModel.seleccionarOpcion(21)
        //Act
        viewModel.siguiente()
        advanceUntilIdle()
        //Assert
        assertThat(viewModel.uiState.value.navigate).isTrue()
        coVerify { cuestionarioRepository.puntuar(mapOf(1L to setOf(10L), 2L to setOf(21L)), any()) }
    }

    @Test
    fun cargarCuestionario_sinConexion_errorDeCarga() = runTest {
        //Arrange
        coEvery { cuestionarioRepository.getCuestionario() } returns Result.failure(SinConexionException())
        //Act
        viewModel.cargarCuestionario()
        advanceUntilIdle()
        //Assert
        assertThat(viewModel.uiState.value.isLoading).isFalse()
        assertThat(viewModel.uiState.value.errorCargaRes).isEqualTo(R.string.error_sin_conexion)
    }
}
