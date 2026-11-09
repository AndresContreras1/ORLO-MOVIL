package com.orlo.app.notificaciones

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.orlo.app.MainActivity
import com.orlo.app.R

// Con la app cerrada, el sistema muestra solo la notificacion del tema "lecciones". Con la app
// abierta llega aqui y se muestra a mano. En los dos casos, tocarla abre la leccion
class OrloMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val titulo = message.notification?.title ?: getString(R.string.notificacion_leccion_titulo)
        val texto = message.notification?.body ?: message.data[EXTRA_TITULO].orEmpty()
        mostrar(this, titulo, texto, message.data[EXTRA_LECCION])
    }

    // La app no guarda el token: se suscribe al tema y el backend envia al tema
    override fun onNewToken(token: String) = Unit

    companion object {
        const val CANAL_LECCIONES = "lecciones"
        const val EXTRA_LECCION = "lessonId"
        const val EXTRA_TITULO = "title"

        fun crearCanal(context: Context) {
            val canal = NotificationChannel(
                CANAL_LECCIONES,
                context.getString(R.string.canal_lecciones),
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
        }

        fun mostrar(context: Context, titulo: String, texto: String, leccionId: String?) {
            // Desde Android 13 hace falta el permiso; sin el, simplemente no se muestra
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            val abrir = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                if (leccionId != null) putExtra(EXTRA_LECCION, leccionId)
            }
            val pendiente = PendingIntent.getActivity(
                context, leccionId?.hashCode() ?: 0, abrir,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val notificacion = NotificationCompat.Builder(context, CANAL_LECCIONES)
                .setSmallIcon(R.drawable.ic_notificacion)
                .setContentTitle(titulo)
                .setContentText(texto)
                .setAutoCancel(true)
                .setContentIntent(pendiente)
                .build()
            NotificationManagerCompat.from(context).notify(leccionId?.hashCode() ?: 0, notificacion)
        }
    }
}
