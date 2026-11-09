package com.orlo.app.ui.screens.advisor

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orlo.app.R
import com.orlo.app.data.injection.IoDispatcher
import com.orlo.app.data.model.AccionRapida
import com.orlo.app.data.model.MensajeChatUi
import com.orlo.app.data.model.OpcionesDelPerfil
import com.orlo.app.data.model.PerfilUi
import com.orlo.app.data.model.PrendaUi
import com.orlo.app.data.model.RolMensaje
import com.orlo.app.data.repository.ArmarioRepository
import com.orlo.app.data.repository.AsesorRepository
import com.orlo.app.data.repository.ContextoAsesor
import com.orlo.app.data.repository.CuentaRepository
import com.orlo.app.data.repository.LimiteAlcanzadoException
import com.orlo.app.data.repository.UsuarioRepository
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

// El chat con el asesor: recibe una duda, envia la pregunta con el perfil y las prendas del armario,
// y muestra 2 o 3 outfits que se pueden guardar
@HiltViewModel
class AdvisorViewModel @Inject constructor(
    private val asesorRepository: AsesorRepository,
    private val cuentaRepository: CuentaRepository,
    private val usuarioRepository: UsuarioRepository,
    private val armarioRepository: ArmarioRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdvisorState())
    val uiState: StateFlow<AdvisorState> = _uiState.asStateFlow()

    // La cuenta, el perfil, las prendas y (si viene del historial) la conversacion son
    // independientes: se piden al tiempo
    fun cargar(conversacionId: String?) {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isLoading = true, errorCargaRes = null) }

            coroutineScope {
                val cuentaAsync = async { cuentaRepository.getCuenta() }
                val perfilAsync = async { usuarioRepository.getPerfil() }
                val prendasAsync = async { armarioRepository.getPrendas() }
                val mensajesAsync = async {
                    if (conversacionId == null) Result.success(emptyList()) else asesorRepository.getMensajes(conversacionId)
                }

                val cuenta = cuentaAsync.await()
                val perfil = perfilAsync.await()
                val prendas = prendasAsync.await()
                val mensajes = mensajesAsync.await()

                // Sin la cuenta no se sabe si puede usar el asesor: es el unico error que bloquea
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
                            perfil = perfil.getOrNull() ?: PerfilUi(),
                            // Si fallan las prendas el asesor responde igual, sin el armario
                            prendas = prendas.getOrNull() ?: emptyList(),
                            // Al volver a la pestana sin conversacion elegida, sigue la que estaba abierta
                            conversacionId = conversacionId ?: it.conversacionId,
                            mensajes = if (conversacionId == null) it.mensajes else mensajes.getOrNull() ?: emptyList(),
                        )
                    }
                }
            }
        }
    }

    fun updateTexto(texto: String) {
        _uiState.update { it.copy(texto = texto, errorMessageRes = null) }
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
                contexto = contextoDe(estado.perfil, estado.prendas),
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
        val clave = "${mensaje.id}-$indice"
        if (clave in _uiState.value.outfitsGuardados) return

        viewModelScope.launch(ioDispatcher) {
            val result = armarioRepository.guardarOutfitDelAsesor(outfit, System.currentTimeMillis())
            if (result.isSuccess) {
                _uiState.update { it.copy(outfitsGuardados = it.outfitsGuardados + clave, avisoRes = R.string.outfit_guardado) }
            } else {
                Log.d("AdvisorViewModel", result.exceptionOrNull()?.message.toString())
                _uiState.update { it.copy(errorMessageRes = mensajeDeError(result.exceptionOrNull(), R.string.error_guardar_outfit)) }
            }
        }
    }

    fun avisoMostrado() {
        _uiState.update { it.copy(avisoRes = null) }
    }

    // Lo que el asesor sabe del usuario, en palabras: nada de correo, apellidos ni identificadores
    private fun contextoDe(perfil: PerfilUi, prendas: List<PrendaUi>): ContextoAsesor = ContextoAsesor(
        nombre = perfil.nombre.split(" ").firstOrNull().orEmpty(),
        rangoEdad = OpcionesDelPerfil.paraAsesor(OpcionesDelPerfil.rangosDeEdad, perfil.rangoEdad),
        tratoPrendas = OpcionesDelPerfil.paraAsesor(OpcionesDelPerfil.tratoPrendas, perfil.tratoPrendas),
        estiloPrincipal = perfil.resultadoActual?.principal?.nombre,
        estiloSecundario = perfil.resultadoActual?.secundario?.nombre,
        tonoPiel = OpcionesDelPerfil.colorParaAsesor(OpcionesDelPerfil.tonosDePiel, perfil.tonoPiel),
        colorCabello = OpcionesDelPerfil.colorParaAsesor(OpcionesDelPerfil.coloresDeCabello, perfil.colorCabello),
        colorOjos = OpcionesDelPerfil.colorParaAsesor(OpcionesDelPerfil.coloresDeOjos, perfil.colorOjos),
        clima = OpcionesDelPerfil.paraAsesor(OpcionesDelPerfil.climas, perfil.clima),
        presupuesto = OpcionesDelPerfil.paraAsesor(OpcionesDelPerfil.presupuestos, perfil.presupuesto),
        ocasiones = perfil.ocasiones.mapNotNull { OpcionesDelPerfil.paraAsesor(OpcionesDelPerfil.ocasiones, it) },
        prendas = prendas.map { Triple(it.nombre, it.categoria.paraAsesor, it.color) },
    )
}
