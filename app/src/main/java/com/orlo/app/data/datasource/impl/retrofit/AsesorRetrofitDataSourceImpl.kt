package com.orlo.app.data.datasource.impl.retrofit

import com.orlo.app.data.datasource.AsesorRemoteDataSource
import com.orlo.app.data.datasource.services.AsesorRetrofitService
import com.orlo.app.data.dtos.MensajeAsesorDto
import com.orlo.app.data.dtos.RespuestaAsesorDto
import javax.inject.Inject

class AsesorRetrofitDataSourceImpl @Inject constructor(
    private val service: AsesorRetrofitService,
) : AsesorRemoteDataSource {

    override suspend fun enviarMensaje(mensaje: MensajeAsesorDto): RespuestaAsesorDto =
        service.enviarMensaje(mensaje)
}
