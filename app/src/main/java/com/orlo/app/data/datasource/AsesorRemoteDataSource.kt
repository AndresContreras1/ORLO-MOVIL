package com.orlo.app.data.datasource

import com.orlo.app.data.dtos.CuentaDto
import com.orlo.app.data.dtos.MensajeAsesorDto
import com.orlo.app.data.dtos.RespuestaAsesorDto

// Contrato del asesor con IA y de la cuenta en el backend (premium y limite de mensajes)
interface AsesorRemoteDataSource {

    suspend fun enviarMensaje(mensaje: MensajeAsesorDto): RespuestaAsesorDto

    suspend fun getCuenta(): CuentaDto

    suspend fun borrarCuenta()
}
