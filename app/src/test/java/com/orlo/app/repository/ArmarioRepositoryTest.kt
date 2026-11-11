package com.orlo.app.repository

import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseUser
import com.orlo.app.data.datasource.ArmarioRemoteDataSource
import com.orlo.app.data.datasource.AuthRemoteDataSource
import com.orlo.app.data.dtos.GuardarOutfitDto
import com.orlo.app.data.model.OutfitPropuestoUi
import com.orlo.app.data.model.OutfitUi
import com.orlo.app.data.repository.ArmarioRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ArmarioRepositoryTest {

    private lateinit var armario: ArmarioRemoteDataSource
    private lateinit var repository: ArmarioRepository
    private val guardado = slot<GuardarOutfitDto>()

    @Before
    fun setUp() {
        armario = mockk()
        val auth = mockk<AuthRemoteDataSource>()
        val sesion = mockk<FirebaseUser>()
        every { sesion.uid } returns "user_1"
        every { auth.currentUser } returns sesion
        coEvery { armario.crearOutfit("user_1", capture(guardado)) } returns "o1"
        repository = ArmarioRepository(armario, mockk(), mockk(), auth)
    }

    @Test
    fun guardarOutfitDelAsesor_tituloLargo_seRecortaAlTopeDelNombre() = runTest {
        //Arrange
        val propuesto = OutfitPropuestoUi(
            titulo = "  Look casual elegante para una cena de fin de semana  ",
            prendas = listOf("Blazer", "Jean"),
            porQue = "Combina",
        )
        //Act
        val result = repository.guardarOutfitDelAsesor(propuesto, fecha = 100)
        //Assert
        assertThat(result.isSuccess).isTrue()
        assertThat(guardado.captured.name).isEqualTo("Look casual elegante para una cena de fi")
        assertThat(guardado.captured.origin).isEqualTo("asesor")
        assertThat(guardado.captured.occasion).isEqualTo("otra")
    }

    @Test
    fun duplicarOutfit_nombreConSufijo_noPasaDelTope() = runTest {
        //Arrange
        val original = OutfitUi(id = "o0", nombre = "Outfit para la entrevista de trabajo", favorito = true)
        //Act
        val result = repository.duplicarOutfit(original, sufijo = "(copia)", fecha = 100)
        //Assert
        assertThat(guardado.captured.name).hasLength(40)
        assertThat(guardado.captured.favorite).isFalse()
        assertThat(result.getOrThrow().id).isEqualTo("o1")
    }
}
