package com.architectprep.app.domain

import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
data class PerDomainScore(val domainCode: String, val domainTitle: String, val correct: Int, val total: Int)

/** Scoring per docs/DEVELOPMENT_DESIGN.md §9.2: scaled to /1000, pass = score >= track.passScore. */
object MockExamScoring {
    fun score(correct: Int, total: Int, scoreScale: Int): Int {
        if (total == 0) return 0
        return (scoreScale.toDouble() * correct / total).roundToInt()
    }
}
