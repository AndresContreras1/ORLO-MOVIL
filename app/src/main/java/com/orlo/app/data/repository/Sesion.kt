package com.orlo.app.data.repository

import com.orlo.app.data.datasource.AuthRemoteDataSource

// El uid de la sesion actual para los repositorios. El data source retorna null si no hay sesion y
// el repositorio decide el error: un solo lugar en vez de repetirlo en cada repositorio
fun AuthRemoteDataSource.uidOError(): String = currentUser?.uid ?: throw SinSesionException()
