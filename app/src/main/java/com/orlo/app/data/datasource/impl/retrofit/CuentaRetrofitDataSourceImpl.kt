package com.orlo.app.data.datasource.impl.retrofit

import com.orlo.app.data.datasource.CuentaRemoteDataSource
import com.orlo.app.data.datasource.services.CuentaRetrofitService
import com.orlo.app.data.dtos.BorrarCuentaDto
import com.orlo.app.data.dtos.CuentaDto
import javax.inject.Inject

class CuentaRetrofitDataSourceImpl @Inject constructor(
    private val service: CuentaRetrofitService,
) : CuentaRemoteDataSource {

    override suspend fun getCuenta(): CuentaDto = service.getCuenta()

    override suspend fun borrarCuenta(): BorrarCuentaDto = service.borrarCuenta()
}
