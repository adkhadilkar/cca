package com.architectprep.app.domain

import com.architectprep.app.data.db.AppDatabase
import com.architectprep.app.data.db.entity.StreakDayEntity
import java.time.LocalDate

/**
 * Marks today as a study day. The app doesn't instrument precise foreground
 * time (docs/DEVELOPMENT_DESIGN.md §9.5 assumes a `studiedSeconds` timer that
 * isn't otherwise built yet), so each qualifying action — a lesson marked
 * done, a practice/flashcard answer, a mock-exam submit — nudges today's
 * count by a fixed increment. Coarser than true session timing, but enough
 * to drive the streak calendar's studied/not-studied signal per day.
 */
object StreakTracker {
    private const val INCREMENT_SECONDS = 300

    suspend fun recordActivity(db: AppDatabase, dailyGoalMinutes: Int) {
        val today = LocalDate.now().toString()
        val existing = db.streakDayDao().get(today)
        val goalSeconds = dailyGoalMinutes * 60
        val newSeconds = (existing?.studiedSeconds ?: 0) + INCREMENT_SECONDS
        db.streakDayDao().upsert(
            StreakDayEntity(
                date = today,
                studiedSeconds = newSeconds,
                goalMet = newSeconds >= goalSeconds
            )
        )
    }
}
