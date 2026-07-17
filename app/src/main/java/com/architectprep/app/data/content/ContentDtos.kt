package com.architectprep.app.data.content

import kotlinx.serialization.Serializable

// Mirrors content/packs/ccar-f/*.json exactly. Kept as thin DTOs so a future
// content-pack format change touches only this file and ContentImporter.

@Serializable
data class DomainsFile(val track: TrackDto, val domains: List<DomainDto>)

@Serializable
data class TrackDto(
    val code: String,
    val title: String,
    val questionCount: Int,
    val timeLimitMin: Int,
    val passScore: Int,
    val scoreScale: Int,
    val contentVersion: Int
)

@Serializable
data class DomainDto(
    val id: String,
    val code: String,
    val title: String,
    val weightPct: Int,
    val orderIndex: Int,
    val summary: String
)

@Serializable
data class LessonsFile(val lessons: List<LessonDto>)

@Serializable
data class LessonDto(
    val id: String,
    val domainId: String,
    val orderIndex: Int,
    val title: String,
    val estMinutes: Int,
    val body: List<LessonBlockDto>
)

@Serializable
data class LessonBlockDto(
    val type: String,
    val value: String,
    val language: String? = null,
    val variant: String? = null
)

@Serializable
data class QuestionsFile(val questions: List<QuestionDto>)

@Serializable
data class QuestionDto(
    val id: String,
    val domainId: String,
    val type: String,
    val difficulty: String,
    val stem: String,
    val choices: List<ChoiceDto>,
    val correct: List<String>,
    val explanation: String,
    val sourceRef: String
)

@Serializable
data class ChoiceDto(val id: String, val text: String)

@Serializable
data class GlossaryFile(val terms: List<GlossaryTermDto>)

@Serializable
data class GlossaryTermDto(
    val id: String,
    val term: String,
    val category: String,
    val definition: String
)
