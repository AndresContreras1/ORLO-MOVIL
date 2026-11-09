package com.orlo.app.viewmodels

import com.google.common.truth.Truth.assertThat
import com.orlo.app.R
import com.orlo.app.data.model.CuentaUi
import com.orlo.app.data.model.MensajeChatUi
import com.orlo.app.data.model.OutfitPropuestoUi
import com.orlo.app.data.model.PerfilUi
import com.orlo.app.data.model.RespuestaAsesorUi
import com.orlo.app.data.model.RolMensaje
import com.orlo.app.data.model.OutfitUi
import com.orlo.app.data.repository.ArmarioRepository
import com.orlo.app.data.repository.AsesorRepository
import com.orlo.app.data.repository.CuentaRepository
import com.orlo.app.data.repository.LimiteAlcanzadoException
import com.orlo.app.data.repository.ResultadoPregunta
import com.orlo.app.data.repository.UsuarioRepository
import com.orlo.app.ui.screens.advisor.AdvisorViewModel
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
class AdvisorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var asesorRepository: AsesorRepository
    private lateinit var cuentaRepository: CuentaRepository
    private lateinit var usuarioRepository: UsuarioRepository
    private lateinit var armarioRepository: ArmarioRepository
    private lateinit var viewModel: AdvisorViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        asesorRepository = mockk()
        cuentaRepository = mockk()
        usuarioRepository = mockk()
        armarioRepository = mockk()
        coEvery { usuarioRepository.getPerfil() } returns Result.success(PerfilUi(nombre = "Laura Gómez"))
        coEvery { armarioRepository.getPrendas() } returns Result.success(emptyList())
        viewModel = AdvisorViewModel(asesorRepository, cuentaRepository, usuarioRepository, armarioRepository, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun enviar_usuarioGratis_noLlamaAlAsesor() = runTest {
        //Arrange
        coEvery { cuentaRepository.getCuenta() } returns Result.success(CuentaUi(premium = false))
        viewModel.cargar(null)
        advanceUntilIdle()
        viewModel.updateTexto("¿Qué me pongo?")
        //Act
        viewModel.enviar()
        advanceUntilIdle()
        //Assert
        assertThat(viewModel.uiState.value.premium).isFalse()
        coVerify(exactly = 0) { asesorRepository.preguntar(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun enviar_premium_muestraPreguntaYRespuestaYActualizaElLimite() = runTest {
        //Arrange
        coEvery { cuentaRepository.getCuenta() } returns Result.success(CuentaUi(premium = true, limiteDiario = 20, mensajesRestantes = 10))
        coEvery { asesorRepository.preguntar(null, "¿Qué me pongo?", null, any(), any(), any()) } returns Result.success(
            ResultadoPregunta(
                conversacionId = "conv_1",
                respuesta = RespuestaAsesorUi(
                    mensaje = MensajeChatUi(rol = RolMensaje.ASESOR, texto = "Dos ideas", outfits = listOf(OutfitPropuestoUi("Fresco"))),
                    mensajesRestantes = 9,
                ),
            )
        )
        viewModel.cargar(null)
        advanceUntilIdle()
        viewModel.updateTexto("¿Qué me pongo?")
        //Act
        viewModel.enviar()
        advanceUntilIdle()
        //Assert
        val state = viewModel.uiState.value
        assertThat(state.mensajes.map { it.rol }).containsExactly(RolMensaje.USUARIO, RolMensaje.ASESOR).inOrder()
        assertThat(state.conversacionId).isEqualTo("conv_1")
        assertThat(state.cuenta?.mensajesRestantes).isEqualTo(9)
        assertThat(state.texto).isEmpty()
    }

    @Test
    fun enviar_limiteAlcanzado_quitaLaPreguntaYDejaCeroMensajes() = runTest {
        //Arrange
        coEvery { cuentaRepository.getCuenta() } returns Result.success(CuentaUi(premium = true, limiteDiario = 20, mensajesRestantes = 1))
        coEvery { asesorRepository.preguntar(any(), any(), any(), any(), any(), any()) } returns
            Result.failure(LimiteAlcanzadoException())
        viewModel.cargar(null)
        advanceUntilIdle()
        viewModel.updateTexto("¿Y para el trabajo?")
        //Act
        viewModel.enviar()
        advanceUntilIdle()
        //Assert
        val state = viewModel.uiState.value
        assertThat(state.mensajes).isEmpty()
        assertThat(state.sinMensajes).isTrue()
        assertThat(state.errorMessageRes).isEqualTo(R.string.error_limite_alcanzado)
        assertThat(state.pendiente).isNull()
    }

    @Test
    fun guardarOutfit_dosVeces_soloLoGuardaUnaVez() = runTest {
        //Arrange
        coEvery { cuentaRepository.getCuenta() } returns Result.success(CuentaUi(premium = true, mensajesRestantes = 5))
        coEvery { armarioRepository.guardarOutfitDelAsesor(any(), any()) } returns Result.success(OutfitUi(id = "o1"))
        val mensaje = MensajeChatUi(id = "asesor-1", rol = RolMensaje.ASESOR, outfits = listOf(OutfitPropuestoUi("Fresco")))
        //Act
        viewModel.guardarOutfit(mensaje, 0)
        advanceUntilIdle()
        viewModel.guardarOutfit(mensaje, 0)
        advanceUntilIdle()
        //Assert
        assertThat(viewModel.uiState.value.outfitsGuardados).containsExactly("asesor-1-0")
        coVerify(exactly = 1) { armarioRepository.guardarOutfitDelAsesor(any(), any()) }
    }
}
