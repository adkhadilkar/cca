package com.architectprep.app

import android.app.Application
import androidx.room.Room
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import com.architectprep.app.data.content.ContentImporter
import com.architectprep.app.data.content.GuideRepository
import com.architectprep.app.data.db.AppDatabase
import com.architectprep.app.data.prefs.UserPrefsRepository

/**
 * Minimal manual service locator. No DI framework yet — swap for Hilt if/when
 * the module graph grows past what a few lazy vals can hold (see
 * docs/DEVELOPMENT_DESIGN.md §3.1, which lists Hilt as the eventual choice).
 */
class PrepApplication : Application(), ImageLoaderFactory {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, AppDatabase.DB_NAME)
            // Pre-release schema (no shipped installs to preserve yet) — replace with
            // real Migration objects before the first release.
            .fallbackToDestructiveMigration()
            .build()
    }

    val contentImporter: ContentImporter by lazy {
        ContentImporter(this, database)
    }

    val guideRepository: GuideRepository by lazy {
        GuideRepository(this)
    }

    val userPrefsRepository: UserPrefsRepository by lazy {
        UserPrefsRepository(this)
    }

    // Lesson bodies reference bundled SVG diagrams (assets/content/images/) — Coil
    // has no SVG support by default, so it needs this decoder registered.
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .components { add(SvgDecoder.Factory()) }
            .build()
}
