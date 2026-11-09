package com.orlo.app.repository

import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseUser
import com.orlo.app.data.datasource.AuthRemoteDataSource
import com.orlo.app.data.datasource.ContenidoRemoteDataSource
import com.orlo.app.data.datasource.UsuarioRemoteDataSource
import com.orlo.app.data.dtos.LeccionResumenDto
import com.orlo.app.data.dtos.ProgresoLeccionFirestoreDto
import com.orlo.app.data.model.EstadoLeccion
import com.orlo.app.data.repository.LeccionRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

// Dos fuentes en un solo resultado: las lecciones del backend y el avance de Firestore
class LeccionRepositoryTest {

    private lateinit var contenidoDataSource: ContenidoRemoteDataSource
    private lateinit var usuarioDataSource: UsuarioRemoteDataSource
    private lateinit var authDataSource: AuthRemoteDataSource
    private lateinit var repository: LeccionRepository

    private val lecciones = listOf(
        LeccionResumenDto(id = 2, title = "Proporciones", order = 2),
        LeccionResumenDto(id = 1, title = "Conoce tu cuerpo", order = 1, freeSample = true),
    )

    @Before
    fun setUp() {
        contenidoDataSource = mockk()
        usuarioDataSource = mockk()
        authDataSource = mockk()
        val usuario = mockk<FirebaseUser>()
        every { usuario.uid } returns "user_1"
        every { authDataSource.currentUser } returns usuario
        repository = LeccionRepository(contenidoDataSource, usuarioDataSource, authDataSource)
    }

    @Test
    fun getLecciones_conAvance_unaLeccionCompletadaYOtraSinEmpezar() = runTest {
        //Arrange
        coEvery { contenidoDataSource.getLecciones() } returns lecciones
        coEvery { usuarioDataSource.getProgresoLecciones("user_1") } returns
            listOf(ProgresoLeccionFirestoreDto(lessonId = 1, status = "completada"))
        //Act
        val result = repository.getLecciones().getOrThrow()
        //Assert
        assertThat(result.map { it.id }).containsExactly(1L, 2L).inOrder()
        assertThat(result[0].estado).isEqualTo(EstadoLeccion.COMPLETADA)
        assertThat(result[1].estado).isEqualTo(EstadoLeccion.SIN_EMPEZAR)
    }

    @Test
    fun getLecciones_falloElAvance_muestraLasLeccionesSinEmpezar() = runTest {
        //Arrange
        coEvery { contenidoDataSource.getLecciones() } returns lecciones
        coEvery { usuarioDataSource.getProgresoLecciones(any()) } throws RuntimeException("Firestore caido")
        //Act
        val result = repository.getLecciones()
        //Assert
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow().all { it.estado == EstadoLeccion.SIN_EMPEZAR }).isTrue()
    }
}
