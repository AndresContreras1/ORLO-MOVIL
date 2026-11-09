package com.orlo.app.data.injection

import com.orlo.app.data.datasource.ArmarioRemoteDataSource
import com.orlo.app.data.datasource.AsesorRemoteDataSource
import com.orlo.app.data.datasource.ConversacionRemoteDataSource
import com.orlo.app.data.datasource.CuentaRemoteDataSource
import com.orlo.app.data.datasource.CuestionarioRemoteDataSource
import com.orlo.app.data.datasource.ExploraRemoteDataSource
import com.orlo.app.data.datasource.GuardadoRemoteDataSource
import com.orlo.app.data.datasource.LeccionRemoteDataSource
import com.orlo.app.data.datasource.OutfitDelDiaRemoteDataSource
import com.orlo.app.data.datasource.TiendaRemoteDataSource
import com.orlo.app.data.datasource.UsuarioRemoteDataSource
import com.orlo.app.data.datasource.impl.firestore.ArmarioFirestoreDataSourceImpl
import com.orlo.app.data.datasource.impl.firestore.ConversacionFirestoreDataSourceImpl
import com.orlo.app.data.datasource.impl.firestore.GuardadoFirestoreDataSourceImpl
import com.orlo.app.data.datasource.impl.firestore.OutfitDelDiaFirestoreDataSourceImpl
import com.orlo.app.data.datasource.impl.firestore.UsuarioFirestoreDataSourceImpl
import com.orlo.app.data.datasource.impl.retrofit.AsesorRetrofitDataSourceImpl
import com.orlo.app.data.datasource.impl.retrofit.CuentaRetrofitDataSourceImpl
import com.orlo.app.data.datasource.impl.retrofit.CuestionarioRetrofitDataSourceImpl
import com.orlo.app.data.datasource.impl.retrofit.ExploraRetrofitDataSourceImpl
import com.orlo.app.data.datasource.impl.retrofit.LeccionRetrofitDataSourceImpl
import com.orlo.app.data.datasource.impl.retrofit.TiendaRetrofitDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// Los repositorios reciben el contrato y aqui se decide que implementacion entrega Hilt: los datos
// privados del usuario van a Firestore y el contenido, el asesor y la cuenta al backend por Retrofit
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
    abstract fun bindCuestionarioRemoteDataSource(
        impl: CuestionarioRetrofitDataSourceImpl
    ): CuestionarioRemoteDataSource

    @Binds
    abstract fun bindLeccionRemoteDataSource(impl: LeccionRetrofitDataSourceImpl): LeccionRemoteDataSource

    @Binds
    abstract fun bindAsesorRemoteDataSource(impl: AsesorRetrofitDataSourceImpl): AsesorRemoteDataSource

    @Binds
    abstract fun bindCuentaRemoteDataSource(impl: CuentaRetrofitDataSourceImpl): CuentaRemoteDataSource

    @Binds
    abstract fun bindExploraRemoteDataSource(impl: ExploraRetrofitDataSourceImpl): ExploraRemoteDataSource

    @Binds
    abstract fun bindTiendaRemoteDataSource(impl: TiendaRetrofitDataSourceImpl): TiendaRemoteDataSource

    @Binds
    abstract fun bindGuardadoRemoteDataSource(impl: GuardadoFirestoreDataSourceImpl): GuardadoRemoteDataSource

    @Binds
    abstract fun bindOutfitDelDiaRemoteDataSource(impl: OutfitDelDiaFirestoreDataSourceImpl): OutfitDelDiaRemoteDataSource
}
