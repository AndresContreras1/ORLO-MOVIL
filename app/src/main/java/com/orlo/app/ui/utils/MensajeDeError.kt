package com.orlo.app.ui.utils

import androidx.annotation.StringRes
import com.orlo.app.R
import com.orlo.app.data.repository.ContenidoNoEncontradoException
import com.orlo.app.data.repository.CorreoNoRegistradoException
import com.orlo.app.data.repository.CorreoYaRegistradoException
import com.orlo.app.data.repository.CredencialesInvalidasException
import com.orlo.app.data.repository.CuestionarioIncompletoException
import com.orlo.app.data.repository.DemasiadosIntentosException
import com.orlo.app.data.repository.ErrorAlSubirImagenException
import com.orlo.app.data.repository.ErrorDelServidorException
import com.orlo.app.data.repository.LimiteAlcanzadoException
import com.orlo.app.data.repository.PermisoDenegadoException
import com.orlo.app.data.repository.PremiumRequeridoException
import com.orlo.app.data.repository.SinConexionException
import com.orlo.app.data.repository.SinSesionException

// El texto que ve el usuario segun la excepcion que entrega el repositorio: en palabras simples, un
// mensaje por cada error que el usuario puede provocar y el general de la pantalla para el resto
@StringRes
fun mensajeDeError(
    error: Throwable?,
    @StringRes generico: Int,
    @StringRes noEncontrado: Int = R.string.error_no_encontrado,
): Int = when (error) {
    is SinConexionException -> R.string.error_sin_conexion
    is ErrorDelServidorException -> R.string.error_servidor
    is SinSesionException -> R.string.error_sin_sesion
    is ContenidoNoEncontradoException -> noEncontrado
    is PermisoDenegadoException -> R.string.error_permiso_denegado
    is CredencialesInvalidasException -> R.string.error_credenciales
    is CorreoNoRegistradoException -> R.string.error_correo_no_registrado
    is CorreoYaRegistradoException -> R.string.error_correo_ya_registrado
    is DemasiadosIntentosException -> R.string.error_demasiados_intentos
    is PremiumRequeridoException -> R.string.error_premium_requerido
    is LimiteAlcanzadoException -> R.string.error_limite_alcanzado
    is ErrorAlSubirImagenException -> R.string.error_subir_imagen
    is CuestionarioIncompletoException -> R.string.error_cuestionario_incompleto
    else -> generico
}
