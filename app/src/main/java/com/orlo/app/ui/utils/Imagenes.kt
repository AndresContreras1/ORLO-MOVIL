package com.orlo.app.ui.utils

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.orlo.app.R
import com.orlo.app.ui.theme.OrloTheme
import coil.request.ImageRequest

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

// Abre la galeria del celular. No pide permisos: el selector del sistema solo entrega la foto que
// el usuario elige
@Composable
fun PickImageButton(
    texto: String,
    onImagePicked: (Uri) -> Unit,
    modifier: Modifier = Modifier,
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) onImagePicked(uri)
    }

    OutlinedButton(
        onClick = { launcher.launch("image/*") },
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
    ) {
        Icon(imageVector = Icons.Outlined.AddPhotoAlternate, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = texto)
    }
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
fun PickImageButtonPreview() {
    OrloTheme {
        PickImageButton(texto = "Elegir foto", onImagePicked = {})
    }
}
