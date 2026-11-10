package com.orlo.app.data.datasource

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.math.max

// Lee la foto que el usuario eligio (galeria, archivo o camara) y la comprime antes de subirla: las
// fotos se guardan pequenas para ahorrar espacio y datos. Si no se puede leer, retorna null y el
// repositorio decide el error
class ImagenLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun comprimir(uri: Uri): ByteArray? {
        // Primero solo se leen las medidas, para no cargar en memoria una foto enorme
        val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, medidas) }
        if (medidas.outWidth <= 0 || medidas.outHeight <= 0) return null

        var escala = 1
        while (max(medidas.outWidth, medidas.outHeight) / escala > LADO_MAXIMO * 2) {
            escala *= 2
        }

        val opciones = BitmapFactory.Options().apply { inSampleSize = escala }
        val original = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opciones)
        } ?: return null

        val ladoMayor = max(original.width, original.height)
        val final = if (ladoMayor > LADO_MAXIMO) {
            val factor = LADO_MAXIMO.toFloat() / ladoMayor
            Bitmap.createScaledBitmap(
                original,
                (original.width * factor).toInt(),
                (original.height * factor).toInt(),
                true
            )
        } else {
            original
        }

        val salida = ByteArrayOutputStream()
        enderezar(uri, final).compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, salida)
        return salida.toByteArray()
    }

    // Las fotos de la camara suelen venir de lado con la orientacion escrita en el EXIF: se giran
    // aqui porque al comprimir el JPEG se pierde ese dato
    private fun enderezar(uri: Uri, foto: Bitmap): Bitmap {
        val orientacion = context.contentResolver.openInputStream(uri)?.use {
            ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } ?: ExifInterface.ORIENTATION_NORMAL
        val grados = when (orientacion) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90F
            ExifInterface.ORIENTATION_ROTATE_180 -> 180F
            ExifInterface.ORIENTATION_ROTATE_270 -> 270F
            else -> return foto
        }
        val giro = Matrix().apply { postRotate(grados) }
        return Bitmap.createBitmap(foto, 0, 0, foto.width, foto.height, giro, true)
    }

    companion object {
        const val LADO_MAXIMO = 1080
        const val CALIDAD_JPEG = 80
    }
}
