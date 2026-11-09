package com.orlo.app.datasource

import com.google.common.truth.Truth.assertThat
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.orlo.app.EmuladorDePrueba
import com.orlo.app.data.datasource.impl.firestore.UsuarioFirestoreDataSourceImpl
import com.orlo.app.data.dtos.ActualizarPerfilDto
import com.orlo.app.data.dtos.EstiloFirestoreDto
import com.orlo.app.data.dtos.ProgresoLeccionFirestoreDto
import com.orlo.app.data.dtos.RegistrarUsuarioDto
import com.orlo.app.data.dtos.ResultadoFirestoreDto
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

// El data source contra el emulador de Firestore: se prueban las consultas, el batch y el borrado de
// subcolecciones, no la logica (esa va en las pruebas con mocks del repositorio)
class UsuarioFirestoreDataSourceTest {

    private lateinit var dataSource: UsuarioFirestoreDataSourceImpl
    private lateinit var userId: String

    @Before
    fun setUp() = runTest {
        EmuladorDePrueba.conectar()
        userId = EmuladorDePrueba.crearUsuario()
        dataSource = UsuarioFirestoreDataSourceImpl(Firebase.firestore)
        dataSource.registrarUsuario(userId, RegistrarUsuarioDto(name = "Laura", acceptedTermsAt = 1, createdAt = 1))
    }

    @After
    fun tearDown() = runTest {
        dataSource.borrarDatosDelUsuario(userId)
        EmuladorDePrueba.borrarUsuarioActual()
    }

    @Test
    fun getUsuario_usuarioRegistrado_retornaSuNombre() = runTest {
        //Act
        val usuario = dataSource.getUsuario(userId)
        //Assert
        assertThat(usuario).isNotNull()
        assertThat(usuario?.id).isEqualTo(userId)
        assertThat(usuario?.name).isEqualTo("Laura")
        assertThat(usuario?.currentResult).isNull()
    }

    @Test
    fun guardarResultado_resultadoNuevo_quedaEnHistorialYComoActual() = runTest {
        //Arrange
        val resultado = ResultadoFirestoreDto(
            primary = EstiloFirestoreDto(id = "e1", code = "urbano", name = "Urbano"),
            secondary = EstiloFirestoreDto(id = "e2", code = "creativo", name = "Creativo"),
            createdAt = 100,
        )
        //Act
        val id = dataSource.guardarResultado(userId, resultado)
        //Assert
        val usuario = dataSource.getUsuario(userId)
        assertThat(usuario?.currentResult?.id).isEqualTo(id)
        assertThat(usuario?.currentResult?.primary?.code).isEqualTo("urbano")
        assertThat(dataSource.getResultados(userId).map { it.id }).containsExactly(id)
    }

    @Test
    fun guardarProgresoLeccion_dosVecesLaMismaLeccion_quedaUnSoloAvance() = runTest {
        //Act
        dataSource.guardarProgresoLeccion(userId, ProgresoLeccionFirestoreDto(lessonId = "l3", status = "en_curso", updatedAt = 1))
        dataSource.guardarProgresoLeccion(userId, ProgresoLeccionFirestoreDto(lessonId = "l3", status = "completada", updatedAt = 2))
        //Assert
        val progreso = dataSource.getProgresoLecciones(userId)
        assertThat(progreso).hasSize(1)
        assertThat(progreso.first().status).isEqualTo("completada")
    }

    @Test
    fun borrarDatosDelUsuario_conSubcolecciones_noQuedaNada() = runTest {
        //Arrange
        dataSource.guardarProgresoLeccion(userId, ProgresoLeccionFirestoreDto(lessonId = "l1", status = "en_curso", updatedAt = 1))
        dataSource.guardarResultado(userId, ResultadoFirestoreDto(primary = EstiloFirestoreDto(code = "casual")))
        //Act
        dataSource.borrarDatosDelUsuario(userId)
        //Assert
        assertThat(dataSource.getUsuario(userId)).isNull()
        assertThat(dataSource.getResultados(userId)).isEmpty()
        assertThat(dataSource.getProgresoLecciones(userId)).isEmpty()
    }

    @Test
    fun actualizarPerfil_conDatos_seMezclaSinBorrarElResultado() = runTest {
        //Arrange
        dataSource.guardarResultado(userId, ResultadoFirestoreDto(primary = EstiloFirestoreDto(code = "urbano", name = "Urbano")))
        //Act
        dataSource.actualizarPerfil(userId, ActualizarPerfilDto(name = "Laura Gómez", climate = "calido", occasions = listOf("universidad")))
        //Assert
        val usuario = dataSource.getUsuario(userId)
        assertThat(usuario?.name).isEqualTo("Laura Gómez")
        assertThat(usuario?.occasions).containsExactly("universidad")
        assertThat(usuario?.currentResult?.primary?.code).isEqualTo("urbano")
    }
}
