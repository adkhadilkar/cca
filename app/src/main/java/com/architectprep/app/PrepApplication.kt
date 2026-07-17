package com.architectprep.app

import android.app.Application
import androidx.room.Room
import com.architectprep.app.data.content.ContentImporter
import com.architectprep.app.data.db.AppDatabase

/**
 * Minimal manual service locator. No DI framework yet — swap for Hilt if/when
 * the module graph grows past what a few lazy vals can hold (see
 * docs/DEVELOPMENT_DESIGN.md §3.1, which lists Hilt as the eventual choice).
 */
class PrepApplication : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, AppDatabase.DB_NAME).build()
    }

    val contentImporter: ContentImporter by lazy {
        ContentImporter(this, database)
    }
}
