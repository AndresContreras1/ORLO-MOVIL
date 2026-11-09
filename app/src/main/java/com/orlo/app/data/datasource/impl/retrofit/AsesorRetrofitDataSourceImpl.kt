package com.orlo.app.data.datasource.impl.retrofit

import com.orlo.app.data.datasource.AsesorRemoteDataSource
import com.orlo.app.data.datasource.services.AsesorRetrofitService
import com.orlo.app.data.datasource.services.CuentaRetrofitService
import com.orlo.app.data.dtos.CuentaDto
import com.orlo.app.data.dtos.MensajeAsesorDto
import com.orlo.app.data.dtos.RespuestaAsesorDto
import javax.inject.Inject

class AsesorRetrofitDataSourceImpl @Inject constructor(
    private val asesorService: AsesorRetrofitService,
    private val cuentaService: CuentaRetrofitService,
) : AsesorRemoteDataSource {

    override suspend fun enviarMensaje(mensaje: MensajeAsesorDto): RespuestaAsesorDto =
        asesorService.enviarMensaje(mensaje)

    override suspend fun getCuenta(): CuentaDto = cuentaService.getCuenta()

    override suspend fun borrarCuenta() = cuentaService.borrarCuenta()
}
