package com.orlo.app.data.injection

import com.orlo.app.data.datasource.ArmarioRemoteDataSource
import com.orlo.app.data.datasource.AsesorRemoteDataSource
import com.orlo.app.data.datasource.ContenidoRemoteDataSource
import com.orlo.app.data.datasource.ConversacionRemoteDataSource
import com.orlo.app.data.datasource.UsuarioRemoteDataSource
import com.orlo.app.data.datasource.impl.firestore.ArmarioFirestoreDataSourceImpl
import com.orlo.app.data.datasource.impl.firestore.ConversacionFirestoreDataSourceImpl
import com.orlo.app.data.datasource.impl.firestore.UsuarioFirestoreDataSourceImpl
import com.orlo.app.data.datasource.impl.retrofit.AsesorRetrofitDataSourceImpl
import com.orlo.app.data.datasource.impl.retrofit.ContenidoRetrofitDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// Los repositorios reciben el contrato y aqui se decide que implementacion entrega Hilt: los datos
// privados del usuario van a Firestore y el contenido y el asesor al backend por Retrofit
@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    abstract fun bindUsuarioRemoteDataSource(impl: UsuarioFirestoreDataSourceImpl): UsuarioRemoteDataSource

    @Binds
    abstract fun bindArmarioRemoteDataSource(impl: ArmarioFirestoreDataSourceImpl): ArmarioRemoteDataSource

    @Binds
    abstract fun bindConversacionRemoteDataSource(
        impl: ConversacionFirestoreDataSourceImpl
    ): ConversacionRemoteDataSource

    @Binds
    abstract fun bindContenidoRemoteDataSource(impl: ContenidoRetrofitDataSourceImpl): ContenidoRemoteDataSource

    @Binds
    abstract fun bindAsesorRemoteDataSource(impl: AsesorRetrofitDataSourceImpl): AsesorRemoteDataSource
}
