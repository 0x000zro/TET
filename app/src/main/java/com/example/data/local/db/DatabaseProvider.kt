package com.example.data.local.db

import android.content.Context

/**
 * Thread-safe provider / factory for central database access.
 * Decouples database creation from consumers and prepares the application
 * for seamless future DI framework adoption (e.g., Hilt) if needed.
 */
object DatabaseProvider {

    @Volatile
    private var instance: AppDatabase? = null

    /**
     * Initializes the database instance using Application context.
     * Safe to call multiple times; idempotent.
     */
    fun initialize(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            instance ?: AppDatabase.getInstance(context.applicationContext).also {
                instance = it
            }
        }
    }

    /**
     * Returns the active database instance, or null if not yet initialized.
     */
    fun getDatabaseOrNull(): AppDatabase? {
        return instance
    }

    /**
     * Returns the active database instance, or initializes it if context is provided.
     */
    fun getDatabase(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            instance ?: AppDatabase.getInstance(context.applicationContext).also {
                instance = it
            }
        }
    }

    /**
     * Checks if the database has been initialized.
     */
    fun isInitialized(): Boolean = instance != null

    /**
     * Test utility to inject an in-memory database or reset the instance.
     */
    internal fun setTestInstance(database: AppDatabase?) {
        synchronized(this) {
            instance = database
            AppDatabase.setTestInstance(database)
        }
    }
}
