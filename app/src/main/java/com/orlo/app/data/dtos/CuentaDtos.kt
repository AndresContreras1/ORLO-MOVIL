package com.orlo.app.data.dtos

import com.orlo.app.data.model.CuentaUi

data class CuentaDto(
    val uid: String? = null,
    val premium: Boolean? = null,
    val dailyLimit: Int? = null,
    val remainingMessages: Int? = null,
)

fun CuentaDto.toCuentaUi(): CuentaUi = CuentaUi(
    uid = uid ?: "",
    premium = premium ?: false,
    limiteDiario = dailyLimit ?: 0,
    mensajesRestantes = remainingMessages ?: 0,
)

// Respuesta de DELETE /api/v1/me
data class BorrarCuentaDto(
    val deleted: Boolean? = null,
)
