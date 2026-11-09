package com.orlo.app.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageException
import retrofit2.HttpException
import java.io.IOException

// El mensaje de la excepcion es para el log del programador. El texto que ve el usuario lo elige el
// ViewModel desde strings.xml (ui/utils/MensajeDeError.kt)

class CredencialesInvalidasException : Exception("Correo o contrasena incorrectos")

class CorreoNoRegistradoException : Exception("No existe una cuenta con ese correo")

class CorreoYaRegistradoException : Exception("Ese correo ya tiene una cuenta")

class SinConexionException : Exception("No hay conexion con la red o con el servidor")

class DemasiadosIntentosException : Exception("Demasiados intentos seguidos")

class SinSesionException : Exception("No hay una sesion iniciada")

class ErrorDeInicioSesionException : Exception("Fallo el inicio de sesion")

class ErrorDeRegistroException : Exception("Fallo el registro")

class PermisoDenegadoException : Exception("Las reglas de Firestore o de Storage no permiten la operacion")

class ErrorAlSubirImagenException : Exception("Fallo la subida de la imagen")

class ContenidoNoEncontradoException : Exception("No se encontro el contenido")

class ErrorDelServidorException : Exception("El servidor respondio con un error")

// El backend responde 403 cuando la funcion es premium y el usuario no lo es
class PremiumRequeridoException : Exception("La funcion requiere premium")

// El backend responde 429 cuando el usuario ya gasto los mensajes del asesor del dia
class LimiteAlcanzadoException : Exception("Se alcanzo el limite de mensajes del asesor")

class CuestionarioIncompletoException : Exception("Faltan preguntas por responder")

// Lo que no tiene un catch propio; el detalle de la excepcion original queda en el log
class ErrorInesperadoException : Exception("Fallo inesperado")

// El codigo HTTP del backend dice que paso
fun errorDelBackend(e: HttpException): Exception = when (e.code()) {
    401 -> SinSesionException()
    403 -> PremiumRequeridoException()
    404 -> ContenidoNoEncontradoException()
    429 -> LimiteAlcanzadoException()
    else -> ErrorDelServidorException()
}

// Firestore dice por que fallo con un codigo: UNAVAILABLE es que no hay conexion, NOT_FOUND que el
// documento no existe y PERMISSION_DENIED que las reglas no lo permiten
fun errorDeFirestore(e: FirebaseFirestoreException): Exception = when (e.code) {
    FirebaseFirestoreException.Code.UNAVAILABLE -> SinConexionException()
    FirebaseFirestoreException.Code.NOT_FOUND -> ContenidoNoEncontradoException()
    FirebaseFirestoreException.Code.PERMISSION_DENIED -> PermisoDenegadoException()
    else -> ErrorDelServidorException()
}

fun errorDeStorage(e: StorageException): Exception = when (e.errorCode) {
    StorageException.ERROR_NOT_AUTHORIZED -> PermisoDenegadoException()
    StorageException.ERROR_RETRY_LIMIT_EXCEEDED -> SinConexionException()
    else -> ErrorAlSubirImagenException()
}

// Un solo lugar para traducir cualquier excepcion a una de la app. Las que ya son de la app pasan
// igual. IOException es la de Retrofit cuando el backend no responde o no hay red
fun errorDe(e: Throwable): Exception = when (e) {
    is HttpException -> errorDelBackend(e)
    is FirebaseFirestoreException -> errorDeFirestore(e)
    is StorageException -> errorDeStorage(e)
    is FirebaseNetworkException -> SinConexionException()
    is IOException -> SinConexionException()
    is SinSesionException, is ContenidoNoEncontradoException, is PremiumRequeridoException,
    is LimiteAlcanzadoException, is CuestionarioIncompletoException, is PermisoDenegadoException,
    is SinConexionException, is ErrorDelServidorException, is ErrorAlSubirImagenException -> e as Exception
    else -> ErrorInesperadoException()
}
