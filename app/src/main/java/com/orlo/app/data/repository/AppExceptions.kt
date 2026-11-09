package com.orlo.app.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

// El mensaje de la excepcion es para el log del programador. El texto que ve el usuario lo elige el
// ViewModel desde strings.xml (ui/utils/MensajeDeError.kt). Todas heredan de AppException: asi
// errorDe() reconoce cualquier error que ya traduce la app sin tener que nombrarlos uno por uno
sealed class AppException(mensaje: String) : Exception(mensaje)

class CredencialesInvalidasException : AppException("Correo o contrasena incorrectos")

class CorreoNoRegistradoException : AppException("No existe una cuenta con ese correo")

class CorreoYaRegistradoException : AppException("Ese correo ya tiene una cuenta")

class ContrasenaDebilException : AppException("La contrasena es muy debil")

class SinConexionException : AppException("No hay conexion con la red o con el servidor")

class DemasiadosIntentosException : AppException("Demasiados intentos seguidos")

class SinSesionException : AppException("No hay una sesion iniciada")

class ErrorDeInicioSesionException : AppException("Fallo el inicio de sesion")

class ErrorDeRegistroException : AppException("Fallo el registro")

class PermisoDenegadoException : AppException("Las reglas de Firestore o de Storage no permiten la operacion")

class ErrorAlSubirImagenException : AppException("Fallo la subida de la imagen")

class ContenidoNoEncontradoException : AppException("No se encontro el contenido")

class ErrorDelServidorException : AppException("El servidor respondio con un error")

// El backend responde 400 cuando los datos enviados no pasan su validacion
class DatosInvalidosException : AppException("El backend rechazo los datos enviados")

// El backend responde 403 cuando la funcion es premium y el usuario no lo es
class PremiumRequeridoException : AppException("La funcion requiere premium")

// El backend responde 409 cuando el cuestionario cambio mientras se respondia
class CuestionarioCambioException : AppException("El cuestionario cambio en el backend")

// El backend responde 429 cuando el usuario ya gasto los mensajes del asesor del dia
class LimiteAlcanzadoException : AppException("Se alcanzo el limite de mensajes del asesor")

// El backend responde 503 cuando el modelo de IA no respondio; el mensaje no se descuenta
class AsesorNoDisponibleException : AppException("El asesor no esta disponible")

class CuestionarioIncompletoException : AppException("Faltan preguntas por responder")

// Lo que no tiene un catch propio; el detalle de la excepcion original queda en el log
class ErrorInesperadoException : AppException("Fallo inesperado")

// El codigo HTTP del backend dice que paso
fun errorDelBackend(e: HttpException): AppException = when (e.code()) {
    400 -> DatosInvalidosException()
    401 -> SinSesionException()
    403 -> PremiumRequeridoException()
    404 -> ContenidoNoEncontradoException()
    409 -> CuestionarioCambioException()
    429 -> LimiteAlcanzadoException()
    503 -> AsesorNoDisponibleException()
    else -> ErrorDelServidorException()
}

// Firestore dice por que fallo con un codigo: UNAVAILABLE es que no hay conexion, NOT_FOUND que el
// documento no existe y PERMISSION_DENIED que las reglas no lo permiten
fun errorDeFirestore(e: FirebaseFirestoreException): AppException = when (e.code) {
    FirebaseFirestoreException.Code.UNAVAILABLE -> SinConexionException()
    FirebaseFirestoreException.Code.NOT_FOUND -> ContenidoNoEncontradoException()
    FirebaseFirestoreException.Code.PERMISSION_DENIED -> PermisoDenegadoException()
    else -> ErrorDelServidorException()
}

fun errorDeStorage(e: StorageException): AppException = when (e.errorCode) {
    StorageException.ERROR_NOT_AUTHORIZED -> PermisoDenegadoException()
    StorageException.ERROR_OBJECT_NOT_FOUND -> ContenidoNoEncontradoException()
    StorageException.ERROR_RETRY_LIMIT_EXCEEDED -> SinConexionException()
    else -> ErrorAlSubirImagenException()
}

// Un solo lugar para traducir cualquier excepcion a una de la app. Las que ya son de la app pasan
// igual. IOException es la de Retrofit cuando el backend no responde o no hay red. La cancelacion
// de la corrutina (el usuario salio de la pantalla) no es un error: se vuelve a lanzar
fun errorDe(e: Throwable): AppException = when (e) {
    is CancellationException -> throw e
    is AppException -> e
    is HttpException -> errorDelBackend(e)
    is FirebaseFirestoreException -> errorDeFirestore(e)
    is StorageException -> errorDeStorage(e)
    is FirebaseNetworkException -> SinConexionException()
    is IOException -> SinConexionException()
    else -> ErrorInesperadoException()
}
