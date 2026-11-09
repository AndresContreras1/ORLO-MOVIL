package com.orlo.app

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

// Las pruebas e2e usan Hilt: en vez de BaseApplication (@HiltAndroidApp) arrancan con la aplicacion
// de prueba de Hilt. build.gradle.kts lo pone como testInstrumentationRunner
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}
