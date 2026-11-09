package com.orlo.app.ui.screens.advisor

import androidx.annotation.StringRes
import com.orlo.app.data.model.AccionRapida
import com.orlo.app.data.model.CuentaUi
import com.orlo.app.data.model.MensajeChatUi
import com.orlo.app.data.model.PerfilUi
import com.orlo.app.data.model.PrendaUi

// La ultima pregunta que no obtuvo respuesta, para el boton de reintentar
data class PreguntaPendiente(
    val texto: String,
    val accion: AccionRapida?,
)

data class AdvisorState(
    val cuenta: CuentaUi? = null,
    val perfil: PerfilUi = PerfilUi(),
    val prendas: List<PrendaUi> = emptyList(),
    val conversacionId: String? = null,
    val mensajes: List<MensajeChatUi> = emptyList(),
    val texto: String = "",
    val enviando: Boolean = false,
    val isLoading: Boolean = true,
    @StringRes val errorCargaRes: Int? = null,
    @StringRes val errorMessageRes: Int? = null,
    val pendiente: PreguntaPendiente? = null,
    // Los outfits ya guardados en Mi armario: "idDelMensaje-indice"
    val outfitsGuardados: Set<String> = emptySet(),
    @StringRes val avisoRes: Int? = null,
) {
    val premium: Boolean get() = cuenta?.premium == true

    // Aviso claro al acercarse al limite de mensajes del dia
    val cercaDelLimite: Boolean
        get() = cuenta != null && cuenta.premium && cuenta.mensajesRestantes in 1..AVISO_LIMITE

    val sinMensajes: Boolean get() = cuenta != null && cuenta.premium && cuenta.mensajesRestantes <= 0

    companion object {
        const val AVISO_LIMITE = 3
    }
}
