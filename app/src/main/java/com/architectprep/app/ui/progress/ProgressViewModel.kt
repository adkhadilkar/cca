package com.architectprep.app.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.architectprep.app.PrepApplication
import com.architectprep.app.data.db.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

data class MockScorePoint(val label: String, val score: Int, val passed: Boolean)
data class WeakDomain(val code: String, val title: String, val accuracyPct: Int)
data class StreakCell(val date: LocalDate, val goalMet: Boolean, val studiedSeconds: Int)

data class ProgressUiState(
    val readinessPct: Int,
    val mockScores: List<MockScorePoint>,
    val passScore: Int,
    val questionsDone: Int,
    val accuracyPct: Int,
    val studyTimeHours: Double,
    val streakDays: Int,
    val last28Days: List<StreakCell>,
    val weakDomains: List<WeakDomain>
)

class ProgressViewModel(private val db: AppDatabase) : ViewModel() {

    private val _uiState = MutableStateFlow<ProgressUiState?>(null)
    val uiState: StateFlow<ProgressUiState?> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val trackCode = "CCAR-F"
        val track = db.trackDao().get(trackCode) ?: return

        val attempts = db.mockAttemptDao().observeSubmitted(trackCode).first()
        val fmt = DateTimeFormatter.ofPattern("MMM d")
        val mockScores = attempts.takeLast(6).map { a ->
            val date = java.time.Instant.ofEpochMilli(a.submittedAt ?: a.startedAt)
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
            MockScorePoint(date.format(fmt), a.score ?: 0, (a.score ?: 0) >= track.passScore)
        }

        val questionsDone = db.questionAttemptDao().observeTotalCount().first()
        val correct = db.questionAttemptDao().observeTotalCorrect().first()
        val accuracyPct = if (questionsDone == 0) 0 else (correct * 100 / questionsDone)

        val streakRows = db.streakDayDao().observeRecent(28).first()
        val byDate = streakRows.associateBy { it.date }
        val today = LocalDate.now()
        val last28 = (27 downTo 0).map { offset ->
            val d = today.minusDays(offset.toLong())
            val row = byDate[d.toString()]
            StreakCell(d, row?.goalMet ?: false, row?.studiedSeconds ?: 0)
        }
        var streak = 0
        for (i in last28.indices.reversed()) {
            if (last28[i].goalMet) streak++ else break
        }
        val studyTimeHours = streakRows.sumOf { it.studiedSeconds } / 3600.0

        val domains = db.domainDao().observeByTrack(trackCode).first()
        val weak = domains.mapNotNull { d ->
            val attempted = db.questionAttemptDao().observeAttemptCountForDomain(d.id).first()
            if (attempted == 0) return@mapNotNull null
            val domainCorrect = db.questionAttemptDao().observeCorrectCountForDomain(d.id).first()
            val pct = domainCorrect * 100 / attempted
            if (pct < 60) WeakDomain(d.code, d.title, pct) else null
        }.sortedBy { it.accuracyPct }

        val totalLessons = domains.sumOf { db.lessonDao().observeByDomain(it.id).first().size }
        val doneLessons = db.lessonProgressDao().observeDoneLessonIds().first().size
        val lessonCompletion = if (totalLessons == 0) 0.0 else doneLessons.toDouble() / totalLessons

        val avgMockScore = if (attempts.isEmpty()) 0.0 else attempts.map { it.score ?: 0 }.average() / track.scoreScale

        val totalQuestions = domains.sumOf { db.questionDao().getByDomain(it.id).size }
        val gradedCards = db.flashcardStateDao().getAll().count { it.reps > 0 }
        val flashcardMastery = if (totalQuestions == 0) 0.0 else gradedCards.toDouble() / totalQuestions

        val readiness = (0.5 * avgMockScore + 0.3 * lessonCompletion + 0.2 * flashcardMastery) * 100

        _uiState.value = ProgressUiState(
            readinessPct = readiness.roundToInt().coerceIn(0, 100),
            mockScores = mockScores,
            passScore = track.passScore,
            questionsDone = questionsDone,
            accuracyPct = accuracyPct,
            studyTimeHours = studyTimeHours,
            streakDays = streak,
            last28Days = last28,
            weakDomains = weak
        )
    }

    class Factory(private val app: PrepApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProgressViewModel(app.database) as T
        }
    }
}
