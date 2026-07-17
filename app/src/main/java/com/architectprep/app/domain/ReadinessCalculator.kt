package com.architectprep.app.domain

import com.architectprep.app.data.db.AppDatabase
import kotlinx.coroutines.flow.first
import kotlin.math.roundToInt

/** Blended readiness signal per docs/DEVELOPMENT_DESIGN.md §9.4. Shared by Home and Progress. */
object ReadinessCalculator {
    suspend fun computePercent(db: AppDatabase, trackCode: String): Int {
        val track = db.trackDao().get(trackCode) ?: return 0
        val attempts = db.mockAttemptDao().observeSubmitted(trackCode).first()
        val avgMockScore = if (attempts.isEmpty()) 0.0 else attempts.map { it.score ?: 0 }.average() / track.scoreScale

        val domains = db.domainDao().observeByTrack(trackCode).first()
        val totalLessons = domains.sumOf { db.lessonDao().observeByDomain(it.id).first().size }
        val doneLessons = db.lessonProgressDao().observeDoneLessonIds().first().size
        val lessonCompletion = if (totalLessons == 0) 0.0 else doneLessons.toDouble() / totalLessons

        val totalQuestions = domains.sumOf { db.questionDao().getByDomain(it.id).size }
        val gradedCards = db.flashcardStateDao().getAll().count { it.reps > 0 }
        val flashcardMastery = if (totalQuestions == 0) 0.0 else gradedCards.toDouble() / totalQuestions

        val readiness = (0.5 * avgMockScore + 0.3 * lessonCompletion + 0.2 * flashcardMastery) * 100
        return readiness.roundToInt().coerceIn(0, 100)
    }
}
