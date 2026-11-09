package com.orlo.app.repository

import com.google.common.truth.Truth.assertThat
import com.orlo.app.data.datasource.ContenidoRemoteDataSource
import com.orlo.app.data.dtos.CuestionarioDto
import com.orlo.app.data.dtos.EstiloDto
import com.orlo.app.data.dtos.OpcionDto
import com.orlo.app.data.dtos.PreguntaDto
import com.orlo.app.data.dtos.ResultadoCuestionarioDto
import com.orlo.app.data.repository.ContenidoNoEncontradoException
import com.orlo.app.data.repository.CuestionarioIncompletoException
import com.orlo.app.data.repository.CuestionarioRepository
import com.orlo.app.data.repository.PremiumRequeridoException
import com.orlo.app.data.repository.SinConexionException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

// El repositorio con el data source moqueado: se revisa el Result y el mapeo, no el backend
class CuestionarioRepositoryTest {

    private lateinit var dataSource: ContenidoRemoteDataSource
    private lateinit var repository: CuestionarioRepository

    @Before
    fun setUp() {
        dataSource = mockk()
        repository = CuestionarioRepository(dataSource)
    }

    @Test
    fun getCuestionario_conPreguntas_retornaElCuestionario() = runTest {
        //Arrange
        coEvery { dataSource.getCuestionario() } returns CuestionarioDto(
            version = 1,
            questions = listOf(PreguntaDto(id = 1, text = "¿Qué usas más?", order = 1, options = listOf(OpcionDto(id = 1, text = "Jeans")))),
        )
        //Act
        val result = repository.getCuestionario()
        //Assert
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()?.preguntas).hasSize(1)
    }

    @Test
    fun getCuestionario_sinPreguntas_contenidoNoEncontrado() = runTest {
        //Arrange
        coEvery { dataSource.getCuestionario() } returns CuestionarioDto(version = 1, questions = emptyList())
        //Act
        val result = repository.getCuestionario()
        //Assert
        assertThat(result.exceptionOrNull()).isInstanceOf(ContenidoNoEncontradoException::class.java)
    }

    @Test
    fun getCuestionario_sinRed_sinConexion() = runTest {
        //Arrange
        coEvery { dataSource.getCuestionario() } throws IOException("timeout")
        //Act
        val result = repository.getCuestionario()
        //Assert
        assertThat(result.exceptionOrNull()).isInstanceOf(SinConexionException::class.java)
    }

    @Test
    fun puntuar_preguntaSinRespuesta_noLlamaAlBackend() = runTest {
        //Act
        val result = repository.puntuar(mapOf(1L to setOf(10L), 2L to emptySet()), fecha = 1)
        //Assert
        assertThat(result.exceptionOrNull()).isInstanceOf(CuestionarioIncompletoException::class.java)
        coVerify(exactly = 0) { dataSource.puntuarCuestionario(any()) }
    }

    @Test
    fun puntuar_respuestasCompletas_retornaPrincipalYSecundario() = runTest {
        //Arrange
        coEvery { dataSource.puntuarCuestionario(any()) } returns ResultadoCuestionarioDto(
            primary = EstiloDto(id = 1, code = "urbano", name = "Urbano"),
            secondary = EstiloDto(id = 2, code = "elegante", name = "Elegante"),
        )
        //Act
        val result = repository.puntuar(mapOf(1L to setOf(10L)), fecha = 50)
        //Assert
        val resultado = result.getOrNull()
        assertThat(resultado?.principal?.code).isEqualTo("urbano")
        assertThat(resultado?.secundario?.nombre).isEqualTo("Elegante")
        assertThat(resultado?.fecha).isEqualTo(50)
    }

    @Test
    fun puntuar_backendResponde403_premiumRequerido() = runTest {
        //Arrange
        coEvery { dataSource.puntuarCuestionario(any()) } throws
            HttpException(Response.error<Any>(403, "{}".toResponseBody()))
        //Act
        val result = repository.puntuar(mapOf(1L to setOf(10L)), fecha = 1)
        //Assert
        assertThat(result.exceptionOrNull()).isInstanceOf(PremiumRequeridoException::class.java)
    }
}
