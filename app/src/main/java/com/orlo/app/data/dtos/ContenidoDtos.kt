package com.orlo.app.data.dtos

import com.orlo.app.data.model.CuestionarioUi
import com.orlo.app.data.model.EjemploUi
import com.orlo.app.data.model.EstadoLeccion
import com.orlo.app.data.model.EstiloUi
import com.orlo.app.data.model.LeccionResumenUi
import com.orlo.app.data.model.LeccionUi
import com.orlo.app.data.model.OpcionUi
import com.orlo.app.data.model.PreguntaUi
import com.orlo.app.data.model.RepasoUi
import com.orlo.app.data.model.ResultadoUi

// DTOs de lo que responde el backend. Los nombres de los campos son los del JSON del backend; todo
// es nulable porque el backend puede omitir un campo, y el mapeo pone el valor por defecto

data class EstiloDto(
    val id: String? = null,
    val code: String? = null,
    val name: String? = null,
    val description: String? = null,
    val palette: List<String>? = null,
    val gallery: List<String>? = null,
)

fun EstiloDto.toEstiloUi(): EstiloUi = EstiloUi(
    id = id ?: "",
    code = code ?: "",
    nombre = name ?: "",
    descripcion = description ?: "",
    paleta = palette ?: emptyList(),
    galeria = gallery ?: emptyList(),
)

data class OpcionDto(
    val id: String? = null,
    val text: String? = null,
    val imageUrl: String? = null,
)

data class PreguntaDto(
    val id: String? = null,
    val text: String? = null,
    val helpText: String? = null,
    val multiple: Boolean? = null,
    val order: Int? = null,
    val options: List<OpcionDto>? = null,
)

data class CuestionarioDto(
    val version: Long? = null,
    val questions: List<PreguntaDto>? = null,
)

// Las preguntas llegan con su orden: la app las muestra en ese orden aunque el JSON venga revuelto
fun CuestionarioDto.toCuestionarioUi(): CuestionarioUi = CuestionarioUi(
    version = version ?: 0L,
    preguntas = (questions ?: emptyList())
        .sortedBy { it.order ?: 0 }
        .map { pregunta ->
            PreguntaUi(
                id = pregunta.id ?: "",
                texto = pregunta.text ?: "",
                ayuda = pregunta.helpText ?: "",
                multiple = pregunta.multiple ?: false,
                opciones = (pregunta.options ?: emptyList()).map { opcion ->
                    OpcionUi(
                        id = opcion.id ?: "",
                        texto = opcion.text ?: "",
                        imagenUrl = opcion.imageUrl,
                    )
                },
            )
        },
)

// Lo que se envia para calcular el resultado: las opciones elegidas en cada pregunta
data class RespuestaDto(
    val questionId: String,
    val optionIds: List<String>,
)

data class PuntuarCuestionarioDto(
    val answers: List<RespuestaDto>,
)

data class ResultadoCuestionarioDto(
    val primary: EstiloDto? = null,
    val secondary: EstiloDto? = null,
    val scores: Map<String, Int>? = null,
)

fun ResultadoCuestionarioDto.toResultadoUi(fecha: Long): ResultadoUi = ResultadoUi(
    principal = primary?.toEstiloUi() ?: EstiloUi(),
    secundario = secondary?.toEstiloUi() ?: EstiloUi(),
    fecha = fecha,
)

data class LeccionResumenDto(
    val id: String? = null,
    val title: String? = null,
    val summary: String? = null,
    val order: Int? = null,
    val freeSample: Boolean? = null,
    val coverUrl: String? = null,
    val minutes: Int? = null,
)

fun LeccionResumenDto.toLeccionResumenUi(estado: EstadoLeccion): LeccionResumenUi = LeccionResumenUi(
    id = id ?: "",
    titulo = title ?: "",
    resumen = summary ?: "",
    orden = order ?: 0,
    muestraGratis = freeSample ?: false,
    portadaUrl = coverUrl,
    minutos = minutes ?: 0,
    estado = estado,
)

data class EjemploDto(
    val imageUrl: String? = null,
    val text: String? = null,
)

data class RepasoDto(
    val question: String? = null,
    val options: List<String>? = null,
    val correctIndex: Int? = null,
)

data class LeccionDto(
    val id: String? = null,
    val title: String? = null,
    val mainIdea: String? = null,
    val examples: List<EjemploDto>? = null,
    val tip: String? = null,
    val review: RepasoDto? = null,
    val freeSample: Boolean? = null,
    val usesColorProfile: Boolean? = null,
)

fun LeccionDto.toLeccionUi(): LeccionUi = LeccionUi(
    id = id ?: "",
    titulo = title ?: "",
    ideaPrincipal = mainIdea ?: "",
    ejemplos = (examples ?: emptyList()).map { EjemploUi(imagenUrl = it.imageUrl, texto = it.text ?: "") },
    consejo = tip ?: "",
    // Sin pregunta u opciones no hay repaso que mostrar
    repaso = review?.takeIf { !it.question.isNullOrBlank() && !it.options.isNullOrEmpty() }?.let {
        RepasoUi(
            pregunta = it.question ?: "",
            opciones = it.options ?: emptyList(),
            indiceCorrecto = it.correctIndex ?: 0,
        )
    },
    muestraGratis = freeSample ?: false,
    usaColorimetria = usesColorProfile ?: false,
)
