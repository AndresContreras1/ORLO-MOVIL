package com.orlo.app.data.datasource.services

import com.orlo.app.data.dtos.BorrarCuentaDto
import com.orlo.app.data.dtos.CuentaDto
import retrofit2.http.DELETE
import retrofit2.http.GET

// La cuenta en el backend: premium y mensajes del asesor que quedan
interface CuentaRetrofitService {

    @GET("api/v1/me")
    suspend fun getCuenta(): CuentaDto

    // El backend responde con un JSON y no con 204 vacio: Retrofit falla al leer un cuerpo vacio
    @DELETE("api/v1/me")
    suspend fun borrarCuenta(): BorrarCuentaDto
}
