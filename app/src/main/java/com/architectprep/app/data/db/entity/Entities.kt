package com.architectprep.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val code: String,
    val title: String,
    val questionCount: Int,
    val timeLimitMin: Int,
    val passScore: Int,
    val scoreScale: Int,
    val contentVersion: Int
)

@Entity(tableName = "domains")
data class DomainEntity(
    @PrimaryKey val id: String,
    val trackCode: String,
    val code: String,
    val title: String,
    val weightPct: Int,
    val orderIndex: Int,
    val summary: String
)

/** [bodyJson] is the lesson's ordered content blocks (text/code/callout), stored as raw JSON. */
@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: String,
    val domainId: String,
    val orderIndex: Int,
    val title: String,
    val estMinutes: Int,
    val bodyJson: String
)

/** [choicesJson] and [correctJson] are raw JSON arrays; kept denormalized since they're read-only content. */
@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: String,
    val domainId: String,
    val type: String,
    val difficulty: String,
    val stem: String,
    val choicesJson: String,
    val correctJson: String,
    val explanation: String,
    val sourceRef: String
)

@Entity(tableName = "glossary_terms")
data class GlossaryTermEntity(
    @PrimaryKey val id: String,
    val term: String,
    val category: String,
    val definition: String
)

/** User progress — never touched by a content-pack re-import. */
@Serializable
@Entity(tableName = "lesson_progress")
data class LessonProgressEntity(
    @PrimaryKey val lessonId: String,
    val status: String, // unseen | in_progress | done
    val lastViewedAt: Long?
)

/** One practice-question attempt. User data — never touched by a content-pack re-import. */
@Serializable
@Entity(tableName = "question_attempts")
data class QuestionAttemptEntity(
    @PrimaryKey val id: String,
    val questionId: String,
    val domainId: String,
    val chosenChoiceId: String,
    val correct: Boolean,
    val timeMs: Long,
    val ts: Long
)

/**
 * SM-2-variant spaced-repetition state, one row per flashcard. The flashcard
 * deck is generated from the question bank (stem = front, correct choice +
 * explanation = back) — see docs/DEVELOPMENT_DESIGN.md §9.1. [cardId] is the
 * underlying question's id. User data — never touched by a content-pack
 * re-import.
 */
@Serializable
@Entity(tableName = "flashcard_state")
data class FlashcardStateEntity(
    @PrimaryKey val cardId: String,
    val ease: Double,
    val intervalDays: Double,
    val dueAt: Long,
    val reps: Int,
    val lapses: Int,
    val lastGrade: String? // again | hard | good | easy
)

/**
 * One mock-exam attempt. [answersJson]/[flaggedJson]/[perDomainJson] are raw
 * JSON — persisted on every answer so an in-progress attempt survives process
 * death (docs/DEVELOPMENT_DESIGN.md §9.2). User data — never touched by a
 * content-pack re-import.
 */
@Serializable
@Entity(tableName = "mock_attempts")
data class MockAttemptEntity(
    @PrimaryKey val id: String,
    val trackCode: String,
    val startedAt: Long,
    val submittedAt: Long?,
    val score: Int?,
    val perDomainJson: String?,
    val answersJson: String,
    val flaggedJson: String,
    val status: String // in_progress | submitted
)

/** One calendar day's study activity, for the streak calendar (docs §9.5). */
@Serializable
@Entity(tableName = "streak_days")
data class StreakDayEntity(
    @PrimaryKey val date: String, // ISO yyyy-MM-dd
    val studiedSeconds: Int,
    val goalMet: Boolean
)
