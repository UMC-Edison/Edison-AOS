package com.umc.edison

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EdisonApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}