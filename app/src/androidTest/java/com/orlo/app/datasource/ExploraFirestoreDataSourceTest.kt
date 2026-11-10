package com.orlo.app.datasource

import com.google.common.truth.Truth.assertThat
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.orlo.app.EmuladorDePrueba
import com.orlo.app.data.datasource.impl.firestore.GuardadoFirestoreDataSourceImpl
import com.orlo.app.data.datasource.impl.firestore.OutfitDelDiaFirestoreDataSourceImpl
import com.orlo.app.data.datasource.impl.firestore.UsuarioFirestoreDataSourceImpl
import com.orlo.app.data.dtos.GuardadoFirestoreDto
import com.orlo.app.data.dtos.OutfitDelDiaFirestoreDto
import com.orlo.app.data.dtos.RegistrarUsuarioDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Before
import org.junit.Test

// Guardados de Explora y outfit del dia contra el emulador, con las reglas compartidas
class ExploraFirestoreDataSourceTest {

    private lateinit var guardados: GuardadoFirestoreDataSourceImpl
    private lateinit var outfitsDelDia: OutfitDelDiaFirestoreDataSourceImpl
    private lateinit var usuarioDataSource: UsuarioFirestoreDataSourceImpl
    private lateinit var userId: String

    @Before
    fun setUp() = runTest {
        EmuladorDePrueba.conectar()
        userId = EmuladorDePrueba.crearUsuario()
        usuarioDataSource = UsuarioFirestoreDataSourceImpl(Firebase.firestore)
        usuarioDataSource.registrarUsuario(userId, RegistrarUsuarioDto(name = "Laura"))
        guardados = GuardadoFirestoreDataSourceImpl(Firebase.firestore)
        outfitsDelDia = OutfitDelDiaFirestoreDataSourceImpl(Firebase.firestore)
    }

    @After
    fun tearDown() = runTest {
        usuarioDataSource.borrarDatosDelUsuario(userId)
        EmuladorDePrueba.borrarUsuarioActual()
    }

    @Test
    fun guardar_dosVecesLaMismaPrenda_quedaUnaSola() = runTest {
        //Arrange
        val blazer = GuardadoFirestoreDto(itemId = 17, name = "Blazer azul oscuro", category = "abrigo", savedAt = 1)
        //Act
        guardados.guardar(userId, "17", blazer)
        guardados.guardar(userId, "17", blazer.copy(savedAt = 2))
        //Assert
        val lista = withContext(Dispatchers.IO) { withTimeout(10_000) { guardados.observarGuardados(userId).first() } }
        assertThat(lista).hasSize(1)
        assertThat(lista.first().itemId).isEqualTo(17L)
    }

    @Test
    fun quitar_prendaGuardada_elListenerEntregaLaListaVacia() = runTest {
        //Arrange
        guardados.guardar(userId, "5", GuardadoFirestoreDto(itemId = 5, name = "Tenis blancos", category = "calzado", savedAt = 1))
        //Act
        guardados.quitar(userId, "5")
        //Assert
        val lista = withContext(Dispatchers.IO) { withTimeout(10_000) { guardados.observarGuardados(userId).first() } }
        assertThat(lista).isEmpty()
    }

    @Test
    fun outfitDelDia_otraOpcionElMismoDia_reemplazaLaAnterior() = runTest {
        //Arrange
        outfitsDelDia.guardarOutfitDelDia(userId, "2026-11-09", OutfitDelDiaFirestoreDto(title = "Primero", items = listOf("Jean"), createdAt = 1))
        //Act
        outfitsDelDia.guardarOutfitDelDia(userId, "2026-11-09", OutfitDelDiaFirestoreDto(title = "Segundo", items = listOf("Falda"), createdAt = 2))
        //Assert
        assertThat(outfitsDelDia.getOutfitDelDia(userId, "2026-11-09")?.title).isEqualTo("Segundo")
        assertThat(outfitsDelDia.getOutfitDelDia(userId, "2026-11-10")).isNull()
    }
}
