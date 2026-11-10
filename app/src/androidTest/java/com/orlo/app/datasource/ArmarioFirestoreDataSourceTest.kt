package com.orlo.app.datasource

import com.google.common.truth.Truth.assertThat
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.orlo.app.EmuladorDePrueba
import com.orlo.app.data.datasource.impl.firestore.ArmarioFirestoreDataSourceImpl
import com.orlo.app.data.datasource.impl.firestore.UsuarioFirestoreDataSourceImpl
import com.orlo.app.data.dtos.GuardarOutfitDto
import com.orlo.app.data.dtos.GuardarPrendaDto
import com.orlo.app.data.dtos.RegistrarUsuarioDto
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

// El armario contra el emulador de Firestore: ids, orden de las consultas y que las reglas acepten
// lo que escribe la app
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

    private suspend fun guardarPrenda(nombre: String, categoria: String, fecha: Long): String {
        val id = dataSource.nuevoIdDePrenda(userId)
        dataSource.guardarPrenda(userId, id, GuardarPrendaDto(name = nombre, category = categoria, color = "azul", createdAt = fecha))
        return id
    }

    @Test
    fun guardarPrenda_conIdNuevo_seLeeConEseId() = runTest {
        //Act
        val id = guardarPrenda("Jean", "abajo", 1)
        //Assert
        val prenda = dataSource.getPrenda(userId, id)
        assertThat(prenda?.id).isEqualTo(id)
        assertThat(prenda?.category).isEqualTo("abajo")
        assertThat(prenda?.favorite).isFalse()
    }

    @Test
    fun getPrendas_variasPrendas_laMasRecientePrimero() = runTest {
        //Arrange
        guardarPrenda("Vieja", "arriba", 1)
        guardarPrenda("Nueva", "arriba", 2)
        //Act
        val prendas = dataSource.getPrendas(userId)
        //Assert
        assertThat(prendas.map { it.name }).containsExactly("Nueva", "Vieja").inOrder()
    }

    @Test
    fun marcarPrendaFavorita_prendaGuardada_quedaFavorita() = runTest {
        //Arrange
        val id = guardarPrenda("Chaqueta", "abrigo", 1)
        //Act
        dataSource.marcarPrendaFavorita(userId, id, true)
        //Assert
        assertThat(dataSource.getPrenda(userId, id)?.favorite).isTrue()
    }

    @Test
    fun marcarOutfitFavorito_outfitGuardado_quedaFavorito() = runTest {
        //Arrange
        val id = dataSource.crearOutfit(userId, GuardarOutfitDto(name = "Lunes", occasion = "universidad", origin = "manual", createdAt = 1))
        //Act
        dataSource.marcarOutfitFavorito(userId, id, true)
        //Assert
        assertThat(dataSource.getOutfit(userId, id)?.favorite).isTrue()
    }

    @Test
    fun actualizarOutfit_cambiaElNombre_noCreaOtro() = runTest {
        //Arrange
        val id = dataSource.crearOutfit(userId, GuardarOutfitDto(name = "Lunes", occasion = "universidad", origin = "manual", createdAt = 1))
        //Act
        dataSource.actualizarOutfit(userId, id, GuardarOutfitDto(name = "Martes", occasion = "trabajo", origin = "manual", createdAt = 1))
        //Assert
        val outfits = dataSource.getOutfits(userId)
        assertThat(outfits).hasSize(1)
        assertThat(outfits.first().name).isEqualTo("Martes")
    }

    @Test
    fun borrarPrenda_prendaGuardada_yaNoExiste() = runTest {
        //Arrange
        val id = guardarPrenda("Tenis", "calzado", 1)
        //Act
        dataSource.borrarPrenda(userId, id)
        //Assert
        assertThat(dataSource.getPrenda(userId, id)).isNull()
    }
}
