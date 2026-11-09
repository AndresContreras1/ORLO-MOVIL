package com.orlo.app.datasource

import com.google.common.truth.Truth.assertThat
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.orlo.app.EmuladorDePrueba
import com.orlo.app.data.datasource.impl.firestore.ArmarioFirestoreDataSourceImpl
import com.orlo.app.data.datasource.impl.firestore.UsuarioFirestoreDataSourceImpl
import com.orlo.app.data.dtos.OutfitFirestoreDto
import com.orlo.app.data.dtos.PrendaFirestoreDto
import com.orlo.app.data.dtos.RegistrarUsuarioDto
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class ArmarioFirestoreDataSourceTest {

    private lateinit var dataSource: ArmarioFirestoreDataSourceImpl
    private lateinit var usuarioDataSource: UsuarioFirestoreDataSourceImpl
    private lateinit var userId: String

    @Before
    fun setUp() = runTest {
        EmuladorDePrueba.conectar()
        userId = EmuladorDePrueba.crearUsuario()
        usuarioDataSource = UsuarioFirestoreDataSourceImpl(Firebase.firestore)
        usuarioDataSource.registrarUsuario(userId, RegistrarUsuarioDto(name = "Laura"))
        dataSource = ArmarioFirestoreDataSourceImpl(Firebase.firestore)
    }

    @After
    fun tearDown() = runTest {
        usuarioDataSource.borrarDatosDelUsuario(userId)
        EmuladorDePrueba.borrarUsuarioActual()
    }

    @Test
    fun guardarPrenda_sinId_creaLaPrendaConSuId() = runTest {
        //Act
        val id = dataSource.guardarPrenda(userId, PrendaFirestoreDto(name = "Jean", category = "abajo", color = "azul", createdAt = 1))
        //Assert
        val prenda = dataSource.getPrenda(userId, id)
        assertThat(prenda?.id).isEqualTo(id)
        assertThat(prenda?.category).isEqualTo("abajo")
    }

    @Test
    fun getPrendas_variasPrendas_laMasRecientePrimero() = runTest {
        //Arrange
        dataSource.guardarPrenda(userId, PrendaFirestoreDto(name = "Vieja", category = "arriba", color = "blanco", createdAt = 1))
        dataSource.guardarPrenda(userId, PrendaFirestoreDto(name = "Nueva", category = "arriba", color = "negro", createdAt = 2))
        //Act
        val prendas = dataSource.getPrendas(userId)
        //Assert
        assertThat(prendas.map { it.name }).containsExactly("Nueva", "Vieja").inOrder()
    }

    @Test
    fun marcarFavorito_outfitGuardado_quedaFavorito() = runTest {
        //Arrange
        val id = dataSource.guardarOutfit(userId, OutfitFirestoreDto(name = "Lunes", occasion = "universidad", origin = "manual", createdAt = 1))
        //Act
        dataSource.marcarFavorito(userId, id, true)
        //Assert
        assertThat(dataSource.getOutfit(userId, id)?.favorite).isTrue()
    }

    @Test
    fun borrarPrenda_prendaGuardada_yaNoExiste() = runTest {
        //Arrange
        val id = dataSource.guardarPrenda(userId, PrendaFirestoreDto(name = "Tenis", category = "calzado", color = "blanco", createdAt = 1))
        //Act
        dataSource.borrarPrenda(userId, id)
        //Assert
        assertThat(dataSource.getPrenda(userId, id)).isNull()
    }
}
