package com.orlo.app.data.datasource

import com.orlo.app.data.dtos.BorrarCuentaDto
import com.orlo.app.data.dtos.CuentaDto

// Contrato de la cuenta en el backend: premium, mensajes del asesor y borrado de la cuenta
interface CuentaRemoteDataSource {

    suspend fun getCuenta(): CuentaDto

    suspend fun borrarCuenta(): BorrarCuentaDto
}
