package com.orlo.app.viewmodels

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import com.orlo.app.EmuladorDePrueba
import com.orlo.app.data.datasource.AsesorRemoteDataSource
import com.orlo.app.data.datasource.AuthRemoteDataSource
import com.orlo.app.data.datasource.CuentaRemoteDataSource
import com.orlo.app.data.datasource.ImagenLocalDataSource
import com.orlo.app.data.datasource.LeccionRemoteDataSource
import com.orlo.app.data.datasource.StorageRemoteDataSource
import com.orlo.app.data.datasource.impl.firestore.ArmarioFirestoreDataSourceImpl
import com.orlo.app.data.datasource.impl.firestore.ConversacionFirestoreDataSourceImpl
import com.orlo.app.data.datasource.impl.firestore.UsuarioFirestoreDataSourceImpl
import com.orlo.app.data.dtos.BorrarCuentaDto
import com.orlo.app.data.dtos.CuentaDto
import com.orlo.app.data.dtos.LeccionDto
import com.orlo.app.data.dtos.LeccionResumenDto
import com.orlo.app.data.dtos.MensajeAsesorDto
import com.orlo.app.data.dtos.OutfitDelDiaDto
import com.orlo.app.data.dtos.OutfitDelDiaPedidoDto
import com.orlo.app.data.dtos.RespuestaAsesorDto
import com.orlo.app.data.model.CategoriaPrenda
import com.orlo.app.data.model.EstadoLeccion
import com.orlo.app.data.model.PrendaUi
import com.orlo.app.data.repository.ArmarioRepository
import com.orlo.app.data.repository.AsesorRepository
import com.orlo.app.data.repository.CuentaRepository
import com.orlo.app.data.repository.LeccionRepository
import com.orlo.app.data.repository.UsuarioRepository
import com.orlo.app.ui.screens.lessons.LessonsState
import com.orlo.app.ui.screens.lessons.LessonsViewModel
import com.orlo.app.ui.screens.wardrobe.WardrobeState
import com.orlo.app.ui.screens.wardrobe.WardrobeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Before
import org.junit.Test

// Los ViewModels con repositorios y data sources reales contra el emulador de Firebase: se prueba
// que lo que cambia en Firestore llega solo al estado de la pantalla. El backend es un doble
class ViewModelsIntegracionTest {

    private lateinit var userId: String
    private lateinit var usuarioRepository: UsuarioRepository
    private lateinit var armarioRepository: ArmarioRepository
    private lateinit var asesorRepository: AsesorRepository
    private lateinit var cuentaRepository: CuentaRepository
    private lateinit var leccionRepository: LeccionRepository

    private val usuarioDataSource = UsuarioFirestoreDataSourceImpl(Firebase.firestore)

    private val cuentaFalsa = object : CuentaRemoteDataSource {
        override suspend fun getCuenta() = CuentaDto(premium = true, dailyLimit = 20, remainingMessages = 20)
        override suspend fun borrarCuenta() = BorrarCuentaDto(deleted = true)
    }

    private val asesorFalso = object : AsesorRemoteDataSource {
        override suspend fun enviarMensaje(mensaje: MensajeAsesorDto) = RespuestaAsesorDto(reply = "Listo")
        override suspend fun outfitDelDia(pedido: OutfitDelDiaPedidoDto) = OutfitDelDiaDto(title = "Hoy")
    }

    private val leccionesFalsas = object : LeccionRemoteDataSource {
        override suspend fun getLecciones() = listOf(
            LeccionResumenDto(id = "1", title = "Conoce tu cuerpo", order = 1),
            LeccionResumenDto(id = "2", title = "Colores", order = 2),
        )
        override suspend fun getLeccion(id: String) = LeccionDto(id = id)
    }

    @Before
    fun setUp() = runTest {
        EmuladorDePrueba.conectar()
        userId = EmuladorDePrueba.crearUsuario()
        val contexto = ApplicationProvider.getApplicationContext<android.content.Context>()
        val auth = AuthRemoteDataSource(Firebase.auth)
        val storage = StorageRemoteDataSource(Firebase.storage)
        val imagenes = ImagenLocalDataSource(contexto)
        val armarioDataSource = ArmarioFirestoreDataSourceImpl(Firebase.firestore)
        usuarioRepository = UsuarioRepository(usuarioDataSource, storage, imagenes, auth)
        armarioRepository = ArmarioRepository(armarioDataSource, storage, imagenes, auth)
        asesorRepository = AsesorRepository(
            asesorFalso, ConversacionFirestoreDataSourceImpl(Firebase.firestore), usuarioDataSource, armarioDataSource, auth,
        )
        cuentaRepository = CuentaRepository(cuentaFalsa, usuarioDataSource, storage, auth)
        leccionRepository = LeccionRepository(leccionesFalsas, usuarioDataSource, auth)
        usuarioRepository.registrarUsuario("Laura", fecha = 1).getOrThrow()
    }

    @After
    fun tearDown() = runTest {
        usuarioDataSource.borrarDatosDelUsuario(userId)
        EmuladorDePrueba.borrarUsuarioActual()
    }

    private suspend fun <T> StateFlow<T>.esperar(condicion: (T) -> Boolean): T = withContext(Dispatchers.IO) {
        withTimeout(10_000) { first(condicion) }
    }

    @Test
    fun armario_prendaNuevaYFavorita_elEstadoCambiaSinRecargar() = runTest {
        //Arrange
        val viewModel = WardrobeViewModel(armarioRepository, usuarioRepository, asesorRepository, cuentaRepository, Dispatchers.IO)
        viewModel.cargar()
        viewModel.uiState.esperar { !it.isLoading }
        //Act: la prenda se guarda por fuera del ViewModel, como desde el formulario
        val prenda = armarioRepository.guardarPrenda(
            PrendaUi(nombre = "Camisa blanca", categoria = CategoriaPrenda.ARRIBA, color = "blanco"), null, fecha = 1,
        ).getOrThrow()
        val conPrenda: WardrobeState = viewModel.uiState.esperar { it.prendas.isNotEmpty() }
        viewModel.marcarPrendaFavorita(conPrenda.prendas.single())
        //Assert
        val conFavorita = viewModel.uiState.esperar { it.prendasFavoritas == 1 }
        assertThat(conFavorita.prendas.single().id).isEqualTo(prenda.id)
        assertThat(conFavorita.errorCargaRes).isNull()
    }

    @Test
    fun aprende_leccionCompletadaEnOtraPantalla_elAvanceSubeSolo() = runTest {
        //Arrange
        val viewModel = LessonsViewModel(leccionRepository, cuentaRepository, Dispatchers.IO)
        viewModel.cargar()
        val inicial: LessonsState = viewModel.uiState.esperar { it.lecciones.size == 2 }
        //Act: el detalle de la leccion guarda el avance
        leccionRepository.guardarEstado("1", EstadoLeccion.COMPLETADA, fecha = 1).getOrThrow()
        //Assert
        val despues = viewModel.uiState.esperar { it.completadas == 1 }
        assertThat(inicial.completadas).isEqualTo(0)
        assertThat(despues.lecciones.first { it.id == "1" }.estado).isEqualTo(EstadoLeccion.COMPLETADA)
        assertThat(despues.premium).isTrue()
    }
}
