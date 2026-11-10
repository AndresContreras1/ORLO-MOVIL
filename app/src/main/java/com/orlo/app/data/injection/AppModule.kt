package com.orlo.app.data.injection

import com.orlo.app.BuildConfig
import com.orlo.app.data.datasource.services.AsesorRetrofitService
import com.orlo.app.data.datasource.services.CuentaRetrofitService
import com.orlo.app.data.datasource.services.CuestionarioRetrofitService
import com.orlo.app.data.datasource.services.FirebaseTokenInterceptor
import com.orlo.app.data.datasource.services.LeccionRetrofitService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

// Todo lo de Retrofit: la instancia con la URL del backend y un servicio por recurso. Lo de
// Firebase va en FirebaseHiltModule
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // El asesor puede tardar varios segundos en responder: el tiempo de lectura es mas largo que
    // el que trae OkHttp por defecto
    @Singleton
    @Provides
    fun providesOkHttpClient(tokenInterceptor: FirebaseTokenInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(tokenInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Singleton
    @Provides
    fun providesRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            // Debug: el backend del computador (10.0.2.2 desde el emulador). Release: el real
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun providesCuestionarioRetrofitService(retrofit: Retrofit): CuestionarioRetrofitService =
        retrofit.create(CuestionarioRetrofitService::class.java)

    @Singleton
    @Provides
    fun providesLeccionRetrofitService(retrofit: Retrofit): LeccionRetrofitService =
        retrofit.create(LeccionRetrofitService::class.java)

    @Singleton
    @Provides
    fun providesAsesorRetrofitService(retrofit: Retrofit): AsesorRetrofitService =
        retrofit.create(AsesorRetrofitService::class.java)

    @Singleton
    @Provides
    fun providesCuentaRetrofitService(retrofit: Retrofit): CuentaRetrofitService =
        retrofit.create(CuentaRetrofitService::class.java)
}
