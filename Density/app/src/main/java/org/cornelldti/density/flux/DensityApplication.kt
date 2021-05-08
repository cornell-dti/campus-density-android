package org.cornelldti.density.flux

import android.app.Application
import android.content.Context

class DensityApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        myInstance = this
    }

    companion object {
        private lateinit var myInstance: DensityApplication
        fun getAppContext(): Context = myInstance.applicationContext
    }
}

