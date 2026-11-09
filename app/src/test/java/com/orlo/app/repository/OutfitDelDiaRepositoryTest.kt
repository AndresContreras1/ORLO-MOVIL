package com.orlo.app.repository

import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseUser
import com.orlo.app.data.datasource.ArmarioRemoteDataSource
import com.orlo.app.data.datasource.AsesorRemoteDataSource
import com.orlo.app.data.datasource.AuthRemoteDataSource
import com.orlo.app.data.datasource.OutfitDelDiaRemoteDataSource
import com.orlo.app.data.datasource.UsuarioRemoteDataSource
import com.orlo.app.data.dtos.OutfitDelDiaDto
import com.orlo.app.data.dtos.OutfitDelDiaFirestoreDto
import com.orlo.app.data.dtos.PrendaFirestoreDto
import com.orlo.app.data.dtos.UsuarioFirestoreDto
import com.orlo.app.data.repository.ErrorDelServidorException
import com.orlo.app.data.repository.LimiteAlcanzadoException
import com.orlo.app.data.repository.OutfitDelDiaRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class OutfitDelDiaRepositoryTest {

    private lateinit var asesor: AsesorRemoteDataSource
    private lateinit var outfitsDelDia: OutfitDelDiaRemoteDataSource
    private lateinit var usuario: UsuarioRemoteDataSource
    private lateinit var armario: ArmarioRemoteDataSource
    private lateinit var repository: OutfitDelDiaRepository

    @Before
    fun setUp() {
        asesor = mockk()
        outfitsDelDia = mockk()
        usuario = mockk()
        armario = mockk()
        val auth = mockk<AuthRemoteDataSource>()
        val sesion = mockk<FirebaseUser>()
        every { sesion.uid } returns "user_1"
        every { auth.currentUser } returns sesion
        coEvery { usuario.getUsuario("user_1") } returns UsuarioFirestoreDto(name = "Laura Gómez")
        coEvery { armario.getPrendas("user_1") } returns listOf(PrendaFirestoreDto(name = "Jean", category = "abajo", color = "azul"))
        repository = OutfitDelDiaRepository(asesor, outfitsDelDia, usuario, armario, auth)
    }

    @Test
    fun pedir_respuestaCompleta_seGuardaConLaFechaDeHoy() = runTest {
        //Arrange
        val guardado = slot<OutfitDelDiaFirestoreDto>()
        coEvery { asesor.outfitDelDia(any()) } returns OutfitDelDiaDto(title = "Lunes", items = listOf("Jean", "Camiseta"), why = "Cómodo")
        coEvery { outfitsDelDia.guardarOutfitDelDia("user_1", "2026-11-09", capture(guardado)) } just runs
        //Act
        val result = repository.pedir("2026-11-09", ahora = 100)
        //Assert
        assertThat(result.getOrThrow().titulo).isEqualTo("Lunes")
        assertThat(guardado.captured.createdAt).isEqualTo(100)
    }

    @Test
    fun pedir_tercerOutfitDelDia_limiteAlcanzadoYNoGuarda() = runTest {
        //Arrange
        coEvery { asesor.outfitDelDia(any()) } throws HttpException(Response.error<Any>(429, "{}".toResponseBody()))
        //Act
        val result = repository.pedir("2026-11-09", ahora = 100)
        //Assert
        assertThat(result.exceptionOrNull()).isInstanceOf(LimiteAlcanzadoException::class.java)
        coVerify(exactly = 0) { outfitsDelDia.guardarOutfitDelDia(any(), any(), any()) }
    }

    @Test
    fun pedir_respuestaSinPrendas_errorDelServidor() = runTest {
        //Arrange
        coEvery { asesor.outfitDelDia(any()) } returns OutfitDelDiaDto(title = "Vacío", items = emptyList())
        //Act
        val result = repository.pedir("2026-11-09", ahora = 100)
        //Assert
        assertThat(result.exceptionOrNull()).isInstanceOf(ErrorDelServidorException::class.java)
    }

    @Test
    fun getDeHoy_sinOutfitGuardado_retornaNull() = runTest {
        //Arrange
        coEvery { outfitsDelDia.getOutfitDelDia("user_1", "2026-11-09") } returns null
        //Act
        val result = repository.getDeHoy("2026-11-09")
        //Assert
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isNull()
    }
}
