package com.architectprep.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.architectprep.app.PrepApplication
import com.architectprep.app.data.content.ContentImporter
import com.architectprep.app.data.db.AppDatabase
import com.architectprep.app.data.prefs.UserPrefsRepository
import com.architectprep.app.domain.ExamDate
import com.architectprep.app.domain.ReadinessCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class DomainSegment(val code: String, val shortLabel: String, val weightPct: Int)

data class ContinueLesson(
    val lessonId: String,
    val title: String,
    val domainCode: String,
    val domainTitle: String,
    val indexInDomain: Int,
    val totalInDomain: Int
)

data class HomeUiState(
    val dateLabel: String,
    val greeting: String,
    val examDaysLabel: String?,
    val examDateLabel: String?,
    val readinessPct: Int,
    val streakDays: Int,
    val todayGoalMinutes: Int,
    val dailyGoalMinutes: Int,
    val trackCode: String,
    val trackTitle: String,
    val questionCount: Int,
    val timeLimitMin: Int,
    val passScore: Int,
    val lessonsDone: Int,
    val lessonsTotal: Int,
    val domainSegments: List<DomainSegment>,
    val continueLesson: ContinueLesson?
)

private val SHORT_LABELS = mapOf(
    "D1" to "Agentic",
    "D2" to "MCP",
    "D3" to "Code",
    "D4" to "Prompt",
    "D5" to "Context"
)

class HomeViewModel(
    private val db: AppDatabase,
    private val importer: ContentImporter,
    private val prefs: UserPrefsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState?>(null)
    val uiState: StateFlow<HomeUiState?> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            importer.importIfNeeded()
            observe()
        }
    }

    private suspend fun observe() {
        val trackCode = "CCAR-F"
        combine(
            db.trackDao().observe(trackCode),
            db.domainDao().observeByTrack(trackCode),
            db.lessonProgressDao().observeDoneLessonIds(),
            prefs.prefs
        ) { track, domains, doneIds, userPrefs ->
                if (track == null) return@combine null
                val doneSet = doneIds.toSet()

                val today = LocalDate.now()
                val hour = java.time.LocalTime.now().hour
                val greeting = when {
                    hour < 12 -> "Good morning"
                    hour < 18 -> "Good afternoon"
                    else -> "Good evening"
                }
                val dateLabel = today.format(DateTimeFormatter.ofPattern("EEEE, MMM d")).uppercase()

                val examDate = userPrefs.examDateMillis?.let { ExamDate.toLocalDate(it) }
                val examDaysLabel = examDate?.let { d ->
                    val days = java.time.temporal.ChronoUnit.DAYS.between(today, d)
                    when {
                        days > 0 -> "$days day${if (days == 1L) "" else "s"}"
                        days == 0L -> "Today"
                        else -> "Past"
                    }
                }
                val examDateLabel = examDate?.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))

                val readinessPct = ReadinessCalculator.computePercent(db, trackCode)

                val streakRows = db.streakDayDao().observeRecent(28).first()
                val byDate = streakRows.associateBy { it.date }
                var streak = 0
                var cursor = today
                while (byDate[cursor.toString()]?.goalMet == true) {
                    streak++
                    cursor = cursor.minusDays(1)
                }
                val todaySeconds = byDate[today.toString()]?.studiedSeconds ?: 0

                var lessonsTotal = 0
                var lessonsDone = 0
                var continueLesson: ContinueLesson? = null
                val segments = mutableListOf<DomainSegment>()
                for (d in domains) {
                    segments.add(DomainSegment(d.code, SHORT_LABELS[d.code] ?: d.code, d.weightPct))
                    val lessons = db.lessonDao().observeByDomain(d.id).first()
                    lessonsTotal += lessons.size
                    lessonsDone += lessons.count { it.id in doneSet }
                    if (continueLesson == null) {
                        val idx = lessons.indexOfFirst { it.id !in doneSet }
                        if (idx >= 0) {
                            val l = lessons[idx]
                            continueLesson = ContinueLesson(
                                lessonId = l.id,
                                title = l.title,
                                domainCode = d.code,
                                domainTitle = d.title,
                                indexInDomain = idx + 1,
                                totalInDomain = lessons.size
                            )
                        }
                    }
                }

                HomeUiState(
                    dateLabel = dateLabel,
                    greeting = greeting,
                    examDaysLabel = examDaysLabel,
                    examDateLabel = examDateLabel,
                    readinessPct = readinessPct,
                    streakDays = streak,
                    todayGoalMinutes = todaySeconds / 60,
                    dailyGoalMinutes = userPrefs.dailyGoalMinutes,
                    trackCode = track.code,
                    trackTitle = track.title,
                    questionCount = track.questionCount,
                    timeLimitMin = track.timeLimitMin,
                    passScore = track.passScore,
                    lessonsDone = lessonsDone,
                    lessonsTotal = lessonsTotal,
                    domainSegments = segments,
                    continueLesson = continueLesson
                )
        }.collect { state -> if (state != null) _uiState.value = state }
    }

    class Factory(private val app: PrepApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(app.database, app.contentImporter, app.userPrefsRepository) as T
        }
    }
}
