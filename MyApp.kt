package com.xdd.cam

import android.app.Application

class MyApp : Application() {
    private val currentPhotoPath = AtomicReference<String>("")

    fun getCurrentPhotoPath(): String = currentPhotoPath.get()
    fun setCurrentPhotoPath(path: String) = currentPhotoPath.set(path)

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: MyApp
            private set
    }
}
