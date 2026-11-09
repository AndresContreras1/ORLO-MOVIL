package com.orlo.app.repository

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import com.orlo.app.EmuladorDePrueba
import com.orlo.app.data.datasource.AsesorRemoteDataSource
import com.orlo.app.data.datasource.AuthRemoteDataSource
import com.orlo.app.data.datasource.ExploraRemoteDataSource
import com.orlo.app.data.datasource.ImagenLocalDataSource
import com.orlo.app.data.datasource.LeccionRemoteDataSource
import com.orlo.app.data.datasource.StorageRemoteDataSource
import com.orlo.app.data.datasource.impl.firestore.ArmarioFirestoreDataSourceImpl
import com.orlo.app.data.datasource.impl.firestore.GuardadoFirestoreDataSourceImpl
import com.orlo.app.data.datasource.impl.firestore.OutfitDelDiaFirestoreDataSourceImpl
import com.orlo.app.data.datasource.impl.firestore.UsuarioFirestoreDataSourceImpl
import com.orlo.app.data.dtos.CompletarArmarioDto
import com.orlo.app.data.dtos.ItemExploraDto
import com.orlo.app.data.dtos.LeccionDto
import com.orlo.app.data.dtos.LeccionResumenDto
import com.orlo.app.data.dtos.MensajeAsesorDto
import com.orlo.app.data.dtos.OutfitDelDiaDto
import com.orlo.app.data.dtos.OutfitDelDiaPedidoDto
import com.orlo.app.data.dtos.RespuestaAsesorDto
import com.orlo.app.data.dtos.SugerenciaArmarioDto
import com.orlo.app.data.model.CategoriaPrenda
import com.orlo.app.data.model.EstadoLeccion
import com.orlo.app.data.model.EstiloUi
import com.orlo.app.data.model.ItemExploraUi
import com.orlo.app.data.model.OutfitUi
import com.orlo.app.data.model.PerfilUi
import com.orlo.app.data.model.PrendaUi
import com.orlo.app.data.model.ResultadoUi
import com.orlo.app.data.repository.ArmarioRepository
import com.orlo.app.data.repository.ExploraRepository
import com.orlo.app.data.repository.LeccionRepository
import com.orlo.app.data.repository.OutfitDelDiaRepository
import com.orlo.app.data.repository.UsuarioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

// Los repositorios con sus data sources reales contra el emulador de Firebase. Lo que va al backend
// se reemplaza por un doble sencillo: aqui se prueba el camino Firestore/Storage completo
class RepositoriosIntegracionTest {

    private lateinit var userId: String
    private lateinit var usuarioRepository: UsuarioRepository
    private lateinit var armarioRepository: ArmarioRepository
    private lateinit var exploraRepository: ExploraRepository
    private lateinit var outfitDelDiaRepository: OutfitDelDiaRepository
    private lateinit var leccionRepository: LeccionRepository

    private val usuarioDataSource = UsuarioFirestoreDataSourceImpl(Firebase.firestore)
    private val armarioDataSource = ArmarioFirestoreDataSourceImpl(Firebase.firestore)

    // Lo que el backend recibe en "Completa tu armario"
    private var pedidoCompletar: CompletarArmarioDto? = null
    private var pedidoOutfit: OutfitDelDiaPedidoDto? = null

    private val exploraFalsa = object : ExploraRemoteDataSource {
        override suspend fun getItems(seccion: String, estilo: String?) = emptyList<ItemExploraDto>()
        override suspend fun getItem(id: String) = ItemExploraDto(id = id)
        override suspend fun completarArmario(armario: CompletarArmarioDto): List<SugerenciaArmarioDto> {
            pedidoCompletar = armario
            return listOf(SugerenciaArmarioDto(category = "abrigo", title = "Una chaqueta"))
        }
    }

    private val asesorFalso = object : AsesorRemoteDataSource {
        override suspend fun enviarMensaje(mensaje: MensajeAsesorDto) = RespuestaAsesorDto(reply = "Listo")
        override suspend fun outfitDelDia(pedido: OutfitDelDiaPedidoDto): OutfitDelDiaDto {
            pedidoOutfit = pedido
            return OutfitDelDiaDto(title = "Lunes cómodo", items = listOf("Jean azul", "Camiseta blanca"), why = "Neutros")
        }
    }

    private val leccionesFalsas = object : LeccionRemoteDataSource {
        override suspend fun getLecciones() = listOf(
            LeccionResumenDto(id = "1", title = "Conoce tu cuerpo", order = 1),
            LeccionResumenDto(id = "2", title = "Colores", order = 2),
        )
        override suspend fun getLeccion(id: String) = LeccionDto(id = id, title = "Lección $id")
    }

    @Before
    fun setUp() = runTest {
        EmuladorDePrueba.conectar()
        userId = EmuladorDePrueba.crearUsuario()
        val contexto = ApplicationProvider.getApplicationContext<android.content.Context>()
        val auth = AuthRemoteDataSource(Firebase.auth)
        val storage = StorageRemoteDataSource(Firebase.storage)
        val imagenes = ImagenLocalDataSource(contexto)
        usuarioRepository = UsuarioRepository(usuarioDataSource, storage, imagenes, auth)
        armarioRepository = ArmarioRepository(armarioDataSource, storage, imagenes, auth)
        exploraRepository = ExploraRepository(
            exploraFalsa, GuardadoFirestoreDataSourceImpl(Firebase.firestore), usuarioDataSource, armarioDataSource, auth,
        )
        outfitDelDiaRepository = OutfitDelDiaRepository(
            asesorFalso, OutfitDelDiaFirestoreDataSourceImpl(Firebase.firestore), usuarioDataSource, armarioDataSource, auth,
        )
        leccionRepository = LeccionRepository(leccionesFalsas, usuarioDataSource, auth)
        usuarioRepository.registrarUsuario("Laura Gómez", fecha = 1).getOrThrow()
    }

    @After
    fun tearDown() = runTest {
        usuarioDataSource.borrarDatosDelUsuario(userId)
        EmuladorDePrueba.borrarUsuarioActual()
    }

    // Espera con tiempo real el primer valor del flujo que cumpla la condicion
    private suspend fun <T> Flow<Result<T>>.esperar(condicion: (T) -> Boolean): T = withContext(Dispatchers.IO) {
        withTimeout(10_000) { first { it.getOrNull()?.let(condicion) == true }.getOrThrow() }
    }

    // Una foto de prueba en la cache, como la que entrega la camara
    private fun fotoDePrueba(): Uri {
        val contexto = ApplicationProvider.getApplicationContext<android.content.Context>()
        val archivo = File(contexto.cacheDir, "prueba_${System.currentTimeMillis()}.jpg")
        val bitmap = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.rgb(226, 114, 91)) }
        archivo.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        return Uri.fromFile(archivo)
    }

    @Test
    fun guardarPerfil_sinConsentimiento_noGuardaDatosSensibles() = runTest {
        //Act
        usuarioRepository.guardarPerfil(
            PerfilUi(nombre = "Laura", tonoPiel = "piel_media", tipoCuerpo = "ovalo", clima = "calido", consentimientoDatosSensibles = false)
        ).getOrThrow()
        //Assert
        val perfil = usuarioRepository.getPerfil().getOrThrow()
        assertThat(perfil.clima).isEqualTo("calido")
        assertThat(perfil.tonoPiel).isEmpty()
        assertThat(usuarioDataSource.getUsuario(userId)?.skinTone).isEmpty()
    }

    @Test
    fun guardarResultado_resultadoNuevo_elPerfilEnVivoLoTraeComoActual() = runTest {
        //Act
        usuarioRepository.guardarResultado(
            ResultadoUi(principal = EstiloUi(id = "3", code = "elegante", nombre = "Elegante"), secundario = EstiloUi(code = "clasico"), fecha = 5)
        ).getOrThrow()
        //Assert
        val perfil = usuarioRepository.observarPerfil().esperar { it.resultadoActual != null }
        assertThat(perfil.resultadoActual?.principal?.id).isEqualTo("3")
        assertThat(usuarioRepository.getResultados().getOrThrow()).hasSize(1)
    }

    @Test
    fun cambiarFoto_fotoDeLaCamara_quedaEnStorageYEnElPerfil() = runTest {
        //Act
        val url = usuarioRepository.cambiarFoto(fotoDePrueba(), fecha = 10).getOrThrow()
        //Assert: la URL se guarda como la ve la web (127.0.0.1) y el perfil en vivo la trae
        assertThat(url).startsWith("http://127.0.0.1:9199/")
        val perfil = usuarioRepository.observarPerfil().esperar { it.fotoUrl != null }
        assertThat(perfil.fotoUrl).isEqualTo(url)
    }

    @Test
    fun guardarPrenda_conFoto_subeLaFotoYLaListaEnVivoLaTrae() = runTest {
        //Act
        val prenda = armarioRepository.guardarPrenda(
            PrendaUi(nombre = "Camisa blanca", categoria = CategoriaPrenda.ARRIBA, color = "blanco"), fotoDePrueba(), fecha = 1,
        ).getOrThrow()
        //Assert
        assertThat(prenda.id).isNotEmpty()
        assertThat(prenda.fotoUrl).contains("garmentImages")
        val prendas = armarioRepository.observarPrendas().esperar { lista -> lista.any { it.id == prenda.id } }
        assertThat(prendas.first { it.id == prenda.id }.nombre).isEqualTo("Camisa blanca")
    }

    @Test
    fun marcarPrendaFavorita_prendaGuardada_laListaEnVivoCambia() = runTest {
        //Arrange
        val prenda = armarioRepository.guardarPrenda(
            PrendaUi(nombre = "Jean", categoria = CategoriaPrenda.ABAJO, color = "azul"), null, fecha = 1,
        ).getOrThrow()
        //Act
        armarioRepository.marcarPrendaFavorita(prenda.id, true).getOrThrow()
        //Assert
        val prendas = armarioRepository.observarPrendas().esperar { lista -> lista.any { it.favorita } }
        assertThat(prendas.single().id).isEqualTo(prenda.id)
    }

    @Test
    fun borrarPrenda_conFoto_borraElDocumentoYLaFoto() = runTest {
        //Arrange
        val prenda = armarioRepository.guardarPrenda(
            PrendaUi(nombre = "Tenis", categoria = CategoriaPrenda.CALZADO, color = "blanco"), fotoDePrueba(), fecha = 1,
        ).getOrThrow()
        //Act
        val result = armarioRepository.borrarPrenda(prenda)
        //Assert
        assertThat(result.isSuccess).isTrue()
        assertThat(armarioRepository.getPrendas().getOrThrow()).isEmpty()
        assertThat(Firebase.storage.reference.child("garmentImages/$userId").listAll().await().items).isEmpty()
    }

    @Test
    fun duplicarOutfit_outfitGuardado_quedanDosConLaCopiaMarcada() = runTest {
        //Arrange
        val outfit = armarioRepository.guardarOutfit(OutfitUi(nombre = "Lunes de clase"), fecha = 1).getOrThrow()
        //Act
        armarioRepository.duplicarOutfit(outfit, sufijo = "(copia)", fecha = 2).getOrThrow()
        //Assert
        val outfits = armarioRepository.observarOutfits().esperar { it.size == 2 }
        assertThat(outfits.map { it.nombre }).containsExactly("Lunes de clase (copia)", "Lunes de clase").inOrder()
    }

    @Test
    fun completarArmario_conPrendas_alBackendSoloVanCategoriaColorYEstilo() = runTest {
        //Arrange
        usuarioRepository.guardarResultado(ResultadoUi(principal = EstiloUi(id = "3", code = "elegante", nombre = "Elegante"))).getOrThrow()
        armarioRepository.guardarPrenda(PrendaUi(nombre = "Camisa", categoria = CategoriaPrenda.ARRIBA, color = "blanco"), null, 1).getOrThrow()
        //Act
        val sugerencias = exploraRepository.completarArmario().getOrThrow()
        //Assert
        assertThat(sugerencias.single().categoria).isEqualTo(CategoriaPrenda.ABRIGO)
        assertThat(pedidoCompletar?.primaryStyle).isEqualTo("elegante")
        assertThat(pedidoCompletar?.wardrobe?.single()?.category).isEqualTo("arriba")
        assertThat(pedidoCompletar?.wardrobe?.single()?.color).isEqualTo("blanco")
    }

    @Test
    fun guardarYQuitar_prendaDeExplora_losGuardadosEnVivoCambian() = runTest {
        //Arrange
        val blazer = ItemExploraUi(id = "17", nombre = "Blazer azul oscuro", categoria = CategoriaPrenda.ABRIGO)
        //Act
        exploraRepository.guardar(blazer, fecha = 1).getOrThrow()
        val guardados = exploraRepository.observarGuardados().esperar { it.isNotEmpty() }
        exploraRepository.quitar("17").getOrThrow()
        //Assert
        assertThat(guardados.single().itemId).isEqualTo("17")
        assertThat(exploraRepository.observarGuardados().esperar { it.isEmpty() }).isEmpty()
    }

    @Test
    fun outfitDelDia_pedirloGuardaElDeHoyYSeLeeDespues() = runTest {
        //Arrange
        armarioRepository.guardarPrenda(PrendaUi(nombre = "Jean azul", categoria = CategoriaPrenda.ABAJO, color = "azul"), null, 1).getOrThrow()
        assertThat(outfitDelDiaRepository.getDeHoy("2026-11-09").getOrThrow()).isNull()
        //Act
        outfitDelDiaRepository.pedir("2026-11-09", ahora = 100).getOrThrow()
        //Assert
        assertThat(outfitDelDiaRepository.getDeHoy("2026-11-09").getOrThrow()?.titulo).isEqualTo("Lunes cómodo")
        assertThat(pedidoOutfit?.profile?.name).isEqualTo("Laura")
        assertThat(pedidoOutfit?.wardrobe?.single()?.name).isEqualTo("Jean azul")
    }

    @Test
    fun guardarEstado_leccionCompletada_laListaEnVivoLaMarca() = runTest {
        //Act
        leccionRepository.guardarEstado("2", EstadoLeccion.COMPLETADA, fecha = 1).getOrThrow()
        //Assert
        val lecciones = leccionRepository.observarLecciones().esperar { lista -> lista.any { it.estado == EstadoLeccion.COMPLETADA } }
        assertThat(lecciones.map { it.id to it.estado })
            .containsExactly("1" to EstadoLeccion.SIN_EMPEZAR, "2" to EstadoLeccion.COMPLETADA).inOrder()
    }
}
