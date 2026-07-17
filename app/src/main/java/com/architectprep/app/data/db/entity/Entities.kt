package com.architectprep.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

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
@Entity(tableName = "lesson_progress")
data class LessonProgressEntity(
    @PrimaryKey val lessonId: String,
    val status: String, // unseen | in_progress | done
    val lastViewedAt: Long?
)
