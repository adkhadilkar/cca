package com.architectprep.app.data.content

import kotlinx.serialization.Serializable

// Mirrors content/packs/ccar-f/guide.json. Static reference content — read
// straight from assets by GuideRepository, no Room table (see
// docs/DEVELOPMENT_DESIGN.md §4 screen 09: "static (from content pack)").

@Serializable
data class GuideDto(
    val examCode: String,
    val title: String,
    val format: GuideFormatDto,
    val domainWeights: List<GuideDomainWeightDto>,
    val proctoringNotes: List<String>,
    val preparation: List<String> = emptyList(),
    val disclaimer: String
)

@Serializable
data class GuideFormatDto(
    val questions: Int,
    val timeLimitMin: Int,
    val passScore: Int,
    val scoreScale: String,
    val examFeeUSD: Int,
    val delivery: String,
    val openBook: Boolean,
    val aiAssistanceAllowed: Boolean,
    val structure: String,
    val resultReporting: String,
    val validityPeriod: String
)

@Serializable
data class GuideDomainWeightDto(
    val code: String,
    val title: String,
    val weightPct: Int
)
