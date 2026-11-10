package com.orlo.app.ui.utils

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.orlo.app.R
import com.orlo.app.ui.theme.OrloTheme
import java.io.File

// Imagen de internet (o una Uri del celular) con Coil: aparece con un desvanecido. Mientras carga
// queda el fondo suave y, si falla o no hay foto, un icono de imagen en vez de un hueco
@Composable
fun OrloAsyncImage(
    url: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .error(R.drawable.ic_imagen_no_disponible)
            .fallback(R.drawable.ic_imagen_no_disponible)
            .build(),
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
    )
}

// La foto de perfil en un circulo; sin foto, la silueta de una persona
@Composable
fun FotoDePerfil(
    url: Any?,
    modifier: Modifier = Modifier,
    tamano: Dp = 40.dp,
) {
    Box(
        modifier = modifier
            .size(tamano)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        if (url != null) {
            OrloAsyncImage(url = url, contentDescription = stringResource(R.string.foto_de_perfil), modifier = Modifier.size(tamano))
        } else {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = stringResource(R.string.foto_de_perfil),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(tamano * 0.6F),
            )
        }
    }
}

// Dos formas de poner una foto: elegirla (galeria, Drive o archivos del celular) o tomarla con la
// camara. Ninguna pide permisos: el selector del sistema y la app de camara solo entregan la foto
// que el usuario elige o toma
@Composable
fun SelectorDeFoto(
    onFoto: (Uri) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    // Donde la camara guarda la foto mientras se toma. rememberSaveable y no remember: la app de
    // camara puede hacer que Android cierre la nuestra, y al volver hay que saber donde quedo
    var fotoDeCamara by rememberSaveable { mutableStateOf<Uri?>(null) }

    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) onFoto(uri)
    }
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { tomada ->
        val uri = fotoDeCamara
        if (tomada && uri != null) onFoto(uri)
    }

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
            onClick = { galeria.launch("image/*") },
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.testTag("btnElegirFoto"),
        ) {
            Icon(imageVector = Icons.Outlined.AddPhotoAlternate, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.elegir_foto))
        }
        OutlinedButton(
            onClick = {
                val uri = nuevaFotoDeCamara(context)
                fotoDeCamara = uri
                camara.launch(uri)
            },
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.testTag("btnTomarFoto"),
        ) {
            Icon(imageVector = Icons.Outlined.PhotoCamera, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.tomar_foto))
        }
    }
}

// Un archivo vacio en la cache de la app que la camara llena; FileProvider le da permiso de escribirlo
private fun nuevaFotoDeCamara(context: Context): Uri {
    val carpeta = File(context.cacheDir, "fotos").apply { mkdirs() }
    val archivo = File(carpeta, "foto_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fotos", archivo)
}

@Composable
@Preview(showBackground = true)
fun OrloAsyncImageSinFotoPreview() {
    OrloTheme {
        OrloAsyncImage(url = null, contentDescription = null, modifier = Modifier.size(140.dp))
    }
}

@Composable
@Preview(showBackground = true)
fun FotoDePerfilPreview() {
    OrloTheme {
        FotoDePerfil(url = null, tamano = 72.dp)
    }
}

@Composable
@Preview(showBackground = true)
fun SelectorDeFotoPreview() {
    OrloTheme {
        SelectorDeFoto(onFoto = {})
    }
}
