package com.orlo.app.ui.screens.advisor

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orlo.app.R
import com.orlo.app.data.injection.IoDispatcher
import com.orlo.app.data.model.AccionRapida
import com.orlo.app.data.model.MensajeChatUi
import com.orlo.app.data.model.RolMensaje
import com.orlo.app.data.repository.ArmarioRepository
import com.orlo.app.data.repository.AsesorRepository
import com.orlo.app.data.repository.CuentaRepository
import com.orlo.app.data.repository.LimiteAlcanzadoException
import com.orlo.app.ui.utils.mensajeDeError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// El chat con el asesor: recibe una duda, la envia y muestra 2 o 3 outfits que se pueden guardar.
// Lo que el asesor sabe del usuario (perfil y armario) lo arma el repositorio
@HiltViewModel
class AdvisorViewModel @Inject constructor(
    private val asesorRepository: AsesorRepository,
    private val cuentaRepository: CuentaRepository,
    private val armarioRepository: ArmarioRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdvisorState())
    val uiState: StateFlow<AdvisorState> = _uiState.asStateFlow()

    // La cuenta y (si viene del historial) la conversacion son independientes: se piden al tiempo
    fun cargar(conversacionId: String?) {
        _uiState.update { it.copy(isLoading = it.cuenta == null, errorCargaRes = null) }
        viewModelScope.launch(ioDispatcher) {
            coroutineScope {
                val cuentaAsync = async { cuentaRepository.getCuenta() }
                val mensajesAsync = async {
                    if (conversacionId == null) Result.success(emptyList()) else asesorRepository.getMensajes(conversacionId)
                }

                val cuenta = cuentaAsync.await()
                val mensajes = mensajesAsync.await()

                // Sin la cuenta no se sabe cuantos mensajes quedan: es el unico error que bloquea
                if (cuenta.isFailure) {
                    Log.d("AdvisorViewModel", cuenta.exceptionOrNull()?.message.toString())
                    _uiState.update {
                        it.copy(isLoading = false, errorCargaRes = mensajeDeError(cuenta.exceptionOrNull(), R.string.error_cargar_asesor))
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            cuenta = cuenta.getOrNull(),
                            // Al volver a la pestana sin conversacion elegida, sigue la que estaba abierta
                            conversacionId = conversacionId ?: it.conversacionId,
                            mensajes = if (conversacionId == null) it.mensajes else mensajes.getOrNull() ?: emptyList(),
                            errorMessageRes = if (mensajes.isFailure) {
                                mensajeDeError(mensajes.exceptionOrNull(), R.string.error_cargar_asesor)
                            } else {
                                null
                            },
                        )
                    }
                }
            }
        }
    }

    // El backend no acepta preguntas de mas de 1000 caracteres: el campo no deja pasar de ahi
    fun updateTexto(texto: String) {
        _uiState.update { it.copy(texto = texto.take(AsesorRepository.MAX_TEXTO), errorMessageRes = null) }
    }

    fun enviar() {
        val texto = _uiState.value.texto.trim()
        if (texto.isBlank()) return
        preguntar(texto = texto, accion = null)
    }

    // Las preguntas de ejemplo del inicio se envian tal cual
    fun enviarEjemplo(texto: String) = preguntar(texto = texto, accion = null)

    // "Hazlo mas casual", "Hazlo mas formal" y "Otra opcion": el texto lo pone la pantalla
    fun accionRapida(accion: AccionRapida, texto: String) = preguntar(texto = texto, accion = accion)

    fun reintentar() {
        val pendiente = _uiState.value.pendiente ?: return
        preguntar(texto = pendiente.texto, accion = pendiente.accion)
    }

    // Empieza una conversacion en blanco; la anterior queda guardada en el historial
    fun nuevaConversacion() {
        _uiState.update {
            it.copy(conversacionId = null, mensajes = emptyList(), texto = "", errorMessageRes = null, pendiente = null, outfitsGuardados = emptySet())
        }
    }

    private fun preguntar(texto: String, accion: AccionRapida?) {
        val estado = _uiState.value
        if (estado.enviando || !estado.premium) return

        val fecha = System.currentTimeMillis()
        val pregunta = MensajeChatUi(id = "local-$fecha", rol = RolMensaje.USUARIO, texto = texto, fecha = fecha)
        val historial = estado.mensajes

        // La pregunta aparece de una vez en el chat mientras llega la respuesta
        _uiState.update {
            it.copy(mensajes = it.mensajes + pregunta, texto = "", enviando = true, errorMessageRes = null, pendiente = null)
        }

        viewModelScope.launch(ioDispatcher) {
            val result = asesorRepository.preguntar(
                conversacionId = estado.conversacionId,
                texto = texto,
                accion = accion,
                historial = historial,
                fecha = fecha,
            )

            if (result.isSuccess) {
                val respuesta = result.getOrThrow()
                _uiState.update {
                    it.copy(
                        enviando = false,
                        conversacionId = respuesta.conversacionId,
                        mensajes = it.mensajes + respuesta.respuesta.mensaje.copy(id = "asesor-$fecha"),
                        cuenta = it.cuenta?.copy(mensajesRestantes = respuesta.respuesta.mensajesRestantes),
                    )
                }
            } else {
                val error = result.exceptionOrNull()
                Log.d("AdvisorViewModel", error?.message.toString())
                // La pregunta sin respuesta sale del chat y vuelve al campo de texto
                _uiState.update {
                    it.copy(
                        enviando = false,
                        mensajes = it.mensajes - pregunta,
                        texto = if (accion == null) texto else it.texto,
                        errorMessageRes = mensajeDeError(error, R.string.error_enviar_pregunta),
                        pendiente = if (error is LimiteAlcanzadoException) null else PreguntaPendiente(texto, accion),
                        cuenta = if (error is LimiteAlcanzadoException) it.cuenta?.copy(mensajesRestantes = 0) else it.cuenta,
                    )
                }
            }
        }
    }

    // Guarda en Mi armario un outfit que propuso el asesor
    fun guardarOutfit(mensaje: MensajeChatUi, indice: Int) {
        val outfit = mensaje.outfits.getOrNull(indice) ?: return
        val clave = AdvisorState.claveDeOutfit(mensaje.id, indice)
        if (clave in _uiState.value.outfitsGuardados) return

        // Se marca de una vez para que dos toques seguidos no lo guarden dos veces
        _uiState.update { it.copy(outfitsGuardados = it.outfitsGuardados + clave) }
        viewModelScope.launch(ioDispatcher) {
            val result = armarioRepository.guardarOutfitDelAsesor(outfit, System.currentTimeMillis())
            if (result.isSuccess) {
                _uiState.update { it.copy(avisoRes = R.string.outfit_guardado) }
            } else {
                Log.d("AdvisorViewModel", result.exceptionOrNull()?.message.toString())
                _uiState.update {
                    it.copy(
                        outfitsGuardados = it.outfitsGuardados - clave,
                        errorMessageRes = mensajeDeError(result.exceptionOrNull(), R.string.error_guardar_outfit),
                    )
                }
            }
        }
    }

    fun avisoMostrado() {
        _uiState.update { it.copy(avisoRes = null) }
    }
}
