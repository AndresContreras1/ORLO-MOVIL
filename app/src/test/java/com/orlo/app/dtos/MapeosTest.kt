package com.orlo.app.dtos

import com.google.common.truth.Truth.assertThat
import com.orlo.app.data.dtos.CuestionarioDto
import com.orlo.app.data.dtos.LeccionDto
import com.orlo.app.data.dtos.OpcionDto
import com.orlo.app.data.dtos.PreguntaDto
import com.orlo.app.data.dtos.RepasoDto
import com.orlo.app.data.dtos.RespuestaAsesorDto
import com.orlo.app.data.dtos.OutfitAsesorDto
import com.orlo.app.data.dtos.UsuarioFirestoreDto
import com.orlo.app.data.dtos.toCamposDePerfil
import com.orlo.app.data.dtos.toCuestionarioUi
import com.orlo.app.data.dtos.toLeccionUi
import com.orlo.app.data.dtos.toPerfilUi
import com.orlo.app.data.dtos.toRespuestaAsesorUi
import com.orlo.app.data.model.EstiloUi
import com.orlo.app.data.model.PerfilUi
import com.orlo.app.data.model.ResultadoUi
import com.orlo.app.data.model.RolMensaje
import org.junit.Test

// Los mapeos DTO -> UI: valores por defecto cuando el backend omite campos y reglas de los datos
// sensibles del perfil
class MapeosTest {

    @Test
    fun toCuestionarioUi_preguntasDesordenadas_quedanEnSuOrden() {
        //Arrange
        val dto = CuestionarioDto(
            version = 2,
            questions = listOf(
                PreguntaDto(id = 2, text = "Segunda", order = 2, options = listOf(OpcionDto(id = 20, text = "B"))),
                PreguntaDto(id = 1, text = "Primera", order = 1, options = listOf(OpcionDto(id = 10, text = "A"))),
            ),
        )
        //Act
        val cuestionario = dto.toCuestionarioUi()
        //Assert
        assertThat(cuestionario.version).isEqualTo(2)
        assertThat(cuestionario.preguntas.map { it.id }).containsExactly(1L, 2L).inOrder()
        assertThat(cuestionario.preguntas.first().multiple).isFalse()
    }

    @Test
    fun toCuestionarioUi_camposNulos_valoresPorDefecto() {
        //Act
        val cuestionario = CuestionarioDto().toCuestionarioUi()
        //Assert
        assertThat(cuestionario.version).isEqualTo(0)
        assertThat(cuestionario.preguntas).isEmpty()
    }

    @Test
    fun toLeccionUi_repasoSinOpciones_noHayRepaso() {
        //Arrange
        val dto = LeccionDto(id = 1, title = "Colores", review = RepasoDto(question = "¿Cuál?", options = emptyList()))
        //Act
        val leccion = dto.toLeccionUi()
        //Assert
        assertThat(leccion.repaso).isNull()
        assertThat(leccion.ejemplos).isEmpty()
        assertThat(leccion.usaColorimetria).isFalse()
    }

    @Test
    fun toLeccionUi_repasoCompleto_seMapea() {
        //Arrange
        val dto = LeccionDto(id = 1, review = RepasoDto(question = "¿Cuál?", options = listOf("A", "B"), correctIndex = 1))
        //Act
        val repaso = dto.toLeccionUi().repaso
        //Assert
        assertThat(repaso).isNotNull()
        assertThat(repaso?.indiceCorrecto).isEqualTo(1)
    }

    @Test
    fun toPerfilUi_sinConsentimiento_noLeeDatosSensibles() {
        //Arrange
        val dto = UsuarioFirestoreDto(name = "Laura", bodyType = "ovalo", skinTone = "piel_media", sensitiveDataConsent = false)
        //Act
        val perfil = dto.toPerfilUi()
        //Assert
        assertThat(perfil.tipoCuerpo).isEmpty()
        assertThat(perfil.tonoPiel).isEmpty()
        assertThat(perfil.resultadoActual).isNull()
    }

    @Test
    fun toCamposDePerfil_sinConsentimiento_borraDatosSensibles() {
        //Arrange
        val perfil = PerfilUi(nombre = " Laura ", tipoCuerpo = "ovalo", tonoPiel = "piel_media", consentimientoDatosSensibles = false)
        //Act
        val campos = perfil.toCamposDePerfil()
        //Assert
        assertThat(campos["name"]).isEqualTo("Laura")
        assertThat(campos["bodyType"]).isEqualTo("")
        assertThat(campos["skinTone"]).isEqualTo("")
        assertThat(campos["sensitiveDataConsent"]).isEqualTo(false)
    }

    @Test
    fun nombreCompleto_conSecundario_principalYSecundarioEnMinuscula() {
        //Arrange
        val resultado = ResultadoUi(principal = EstiloUi(nombre = "Urbano"), secundario = EstiloUi(nombre = "Elegante"))
        //Act
        val nombre = resultado.nombreCompleto
        //Assert
        assertThat(nombre).isEqualTo("Urbano · elegante")
    }

    @Test
    fun toRespuestaAsesorUi_preguntaVacia_quedaNula() {
        //Arrange
        val dto = RespuestaAsesorDto(
            reply = "Dos ideas",
            outfits = listOf(OutfitAsesorDto(title = "Fresco", items = listOf("Jean"), why = "Cómodo")),
            followUpQuestion = "  ",
            remainingMessages = 4,
        )
        //Act
        val respuesta = dto.toRespuestaAsesorUi(fecha = 10)
        //Assert
        assertThat(respuesta.mensaje.rol).isEqualTo(RolMensaje.ASESOR)
        assertThat(respuesta.mensaje.outfits).hasSize(1)
        assertThat(respuesta.mensaje.preguntaSeguimiento).isNull()
        assertThat(respuesta.mensajesRestantes).isEqualTo(4)
    }
}
