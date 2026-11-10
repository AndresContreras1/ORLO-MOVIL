package com.orlo.app.datasource

import com.google.common.truth.Truth.assertThat
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.orlo.app.EmuladorDePrueba
import com.orlo.app.data.datasource.impl.firestore.ConversacionFirestoreDataSourceImpl
import com.orlo.app.data.datasource.impl.firestore.UsuarioFirestoreDataSourceImpl
import com.orlo.app.data.dtos.GuardarMensajeDto
import com.orlo.app.data.dtos.RegistrarUsuarioDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Before
import org.junit.Test

// Las conversaciones del asesor contra el emulador: el batch que guarda pregunta y respuesta, el
// orden de los mensajes, el listener en vivo y el borrado con sus mensajes
class ConversacionFirestoreDataSourceTest {

    private lateinit var dataSource: ConversacionFirestoreDataSourceImpl
    private lateinit var usuarioDataSource: UsuarioFirestoreDataSourceImpl
    private lateinit var userId: String

    private fun pregunta(texto: String, fecha: Long) = GuardarMensajeDto(role = "user", text = texto, createdAt = fecha)
    private fun respuesta(texto: String, fecha: Long) = GuardarMensajeDto(role = "assistant", text = texto, createdAt = fecha)

    @Before
    fun setUp() = runTest {
        EmuladorDePrueba.conectar()
        userId = EmuladorDePrueba.crearUsuario()
        usuarioDataSource = UsuarioFirestoreDataSourceImpl(Firebase.firestore)
        usuarioDataSource.registrarUsuario(userId, RegistrarUsuarioDto(name = "Laura"))
        dataSource = ConversacionFirestoreDataSourceImpl(Firebase.firestore)
    }

    @After
    fun tearDown() = runTest {
        usuarioDataSource.borrarDatosDelUsuario(userId)
        EmuladorDePrueba.borrarUsuarioActual()
    }

    @Test
    fun guardarIntercambio_conversacionNueva_creaLaConversacionConSusDosMensajes() = runTest {
        //Act
        val id = dataSource.guardarIntercambio(userId, null, "¿Qué me pongo?", pregunta("¿Qué me pongo?", 1), respuesta("Dos ideas", 2))
        //Assert
        val conversaciones = dataSource.getConversaciones(userId)
        assertThat(conversaciones.map { it.id }).containsExactly(id)
        assertThat(conversaciones.first().title).isEqualTo("¿Qué me pongo?")
        assertThat(dataSource.getMensajes(userId, id).map { it.role }).containsExactly("user", "assistant").inOrder()
    }

    @Test
    fun guardarIntercambio_mismaConversacion_agregaLosMensajesEnOrden() = runTest {
        //Arrange
        val id = dataSource.guardarIntercambio(userId, null, "Lunes", pregunta("Lunes", 1), respuesta("Idea 1", 2))
        //Act
        dataSource.guardarIntercambio(userId, id, "Lunes", pregunta("Más formal", 3), respuesta("Idea 2", 4))
        //Assert
        assertThat(dataSource.getConversaciones(userId)).hasSize(1)
        assertThat(dataSource.getMensajes(userId, id).map { it.text })
            .containsExactly("Lunes", "Idea 1", "Más formal", "Idea 2").inOrder()
    }

    @Test
    fun observarConversaciones_alGuardarUnIntercambio_llegaLaListaNueva() = runTest {
        //Act
        val id = dataSource.guardarIntercambio(userId, null, "Martes", pregunta("Martes", 1), respuesta("Idea", 2))
        //Assert: el listener entrega la conversacion sin volver a consultar
        val lista = withContext(Dispatchers.IO) {
            withTimeout(10_000) { dataSource.observarConversaciones(userId).first { it.isNotEmpty() } }
        }
        assertThat(lista.map { it.id }).containsExactly(id)
    }

    @Test
    fun borrarConversacion_conMensajes_noQuedaNada() = runTest {
        //Arrange
        val id = dataSource.guardarIntercambio(userId, null, "Viernes", pregunta("Viernes", 1), respuesta("Idea", 2))
        //Act
        dataSource.borrarConversacion(userId, id)
        //Assert
        assertThat(dataSource.getConversaciones(userId)).isEmpty()
        assertThat(dataSource.getMensajes(userId, id)).isEmpty()
    }
}
