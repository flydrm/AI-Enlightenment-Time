package com.enlightenment

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EnlightenmentApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Initialize any global configurations here
    }
}