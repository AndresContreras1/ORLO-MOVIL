package com.orlo.app.data.dtos

import com.orlo.app.data.model.EstiloUi
import com.orlo.app.data.model.PerfilUi
import com.orlo.app.data.model.ResultadoUi

// DTOs de Firestore: constructor vacio (todos los campos con valor por defecto) para que
// toObject() pueda crearlos, y los nombres de los campos tal como quedan en la base

// Lo que se guarda al registrarse: nunca el correo ni la contrasena, eso lo tiene Authentication
data class RegistrarUsuarioDto(
    val name: String = "",
    val acceptedTermsAt: Long = 0,
    val createdAt: Long = 0,
)

// Copia del estilo que se muestra en la app (desnormalizacion): asi el resultado se ve aunque el
// backend cambie despues la descripcion del estilo
data class EstiloFirestoreDto(
    val id: Long = 0,
    val code: String = "",
    val name: String = "",
    val description: String = "",
    val palette: List<String> = emptyList(),
    val gallery: List<String> = emptyList(),
)

data class ResultadoFirestoreDto(
    val id: String = "",
    val primary: EstiloFirestoreDto = EstiloFirestoreDto(),
    val secondary: EstiloFirestoreDto = EstiloFirestoreDto(),
    val createdAt: Long = 0,
)

data class UsuarioFirestoreDto(
    val id: String = "",
    val name: String = "",
    val ageRange: String = "",
    val garmentPronoun: String = "",
    val bodyType: String = "",
    val skinTone: String = "",
    val hairColor: String = "",
    val eyeColor: String = "",
    val climate: String = "",
    val budget: String = "",
    val occasions: List<String> = emptyList(),
    val sensitiveDataConsent: Boolean = false,
    val currentResult: ResultadoFirestoreDto? = null,
    val acceptedTermsAt: Long = 0,
    val createdAt: Long = 0,
)

fun EstiloUi.toEstiloFirestoreDto(): EstiloFirestoreDto = EstiloFirestoreDto(
    id = id,
    code = code,
    name = nombre,
    description = descripcion,
    palette = paleta,
    gallery = galeria,
)

fun EstiloFirestoreDto.toEstiloUi(): EstiloUi = EstiloUi(
    id = id,
    code = code,
    nombre = name,
    descripcion = description,
    paleta = palette,
    galeria = gallery,
)

fun ResultadoUi.toResultadoFirestoreDto(): ResultadoFirestoreDto = ResultadoFirestoreDto(
    id = id,
    primary = principal.toEstiloFirestoreDto(),
    secondary = secundario.toEstiloFirestoreDto(),
    createdAt = fecha,
)

fun ResultadoFirestoreDto.toResultadoUi(): ResultadoUi = ResultadoUi(
    id = id,
    principal = primary.toEstiloUi(),
    secundario = secondary.toEstiloUi(),
    fecha = createdAt,
)

fun UsuarioFirestoreDto.toPerfilUi(): PerfilUi = PerfilUi(
    nombre = name,
    rangoEdad = ageRange,
    tratoPrendas = garmentPronoun,
    // Los datos de apariencia solo se leen si hay consentimiento
    tipoCuerpo = if (sensitiveDataConsent) bodyType else "",
    tonoPiel = if (sensitiveDataConsent) skinTone else "",
    colorCabello = if (sensitiveDataConsent) hairColor else "",
    colorOjos = if (sensitiveDataConsent) eyeColor else "",
    clima = climate,
    presupuesto = budget,
    ocasiones = occasions,
    consentimientoDatosSensibles = sensitiveDataConsent,
    resultadoActual = currentResult?.takeIf { it.primary.code.isNotBlank() }?.toResultadoUi(),
)

// Los campos del perfil que se actualizan con update(): sin consentimiento, los datos de
// apariencia se borran
fun PerfilUi.toCamposDePerfil(): Map<String, Any> = mapOf(
    "name" to nombre.trim(),
    "ageRange" to rangoEdad,
    "garmentPronoun" to tratoPrendas,
    "bodyType" to if (consentimientoDatosSensibles) tipoCuerpo else "",
    "skinTone" to if (consentimientoDatosSensibles) tonoPiel else "",
    "hairColor" to if (consentimientoDatosSensibles) colorCabello else "",
    "eyeColor" to if (consentimientoDatosSensibles) colorOjos else "",
    "climate" to clima,
    "budget" to presupuesto,
    "occasions" to ocasiones,
    "sensitiveDataConsent" to consentimientoDatosSensibles,
)

data class ProgresoLeccionFirestoreDto(
    val lessonId: Long = 0,
    val status: String = "",
    val updatedAt: Long = 0,
)
