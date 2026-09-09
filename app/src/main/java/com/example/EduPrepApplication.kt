package com.example

import android.app.Application
import com.example.data.local.db.DatabaseProvider

/**
 * Application entry point responsible for process-level initialization.
 * Initializes the Room database provider safely with application context.
 */
class EduPrepApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize the central singleton Room database provider
        DatabaseProvider.initialize(this)
    }
}
