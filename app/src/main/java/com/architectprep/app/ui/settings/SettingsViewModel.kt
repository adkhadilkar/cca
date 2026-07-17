package com.architectprep.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.architectprep.app.PrepApplication
import com.architectprep.app.data.db.AppDatabase
import com.architectprep.app.data.db.entity.FlashcardStateEntity
import com.architectprep.app.data.db.entity.LessonProgressEntity
import com.architectprep.app.data.db.entity.MockAttemptEntity
import com.architectprep.app.data.db.entity.QuestionAttemptEntity
import com.architectprep.app.data.db.entity.StreakDayEntity
import com.architectprep.app.data.prefs.ThemePref
import com.architectprep.app.data.prefs.UserPrefs
import com.architectprep.app.data.prefs.UserPrefsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ExportBundle(
    val exportedAt: Long,
    val lessonProgress: List<LessonProgressEntity>,
    val questionAttempts: List<QuestionAttemptEntity>,
    val flashcardState: List<FlashcardStateEntity>,
    val mockAttempts: List<MockAttemptEntity>,
    val streakDays: List<StreakDayEntity>
)

class SettingsViewModel(
    private val db: AppDatabase,
    private val prefs: UserPrefsRepository
) : ViewModel() {

    private val exportJson = Json { prettyPrint = true }

    val userPrefs: StateFlow<UserPrefs?> = prefs.prefs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setTheme(theme: ThemePref) = viewModelScope.launch { prefs.setTheme(theme) }
    fun setExamDate(millis: Long?) = viewModelScope.launch { prefs.setExamDate(millis) }
    fun setDailyGoalMinutes(minutes: Int) = viewModelScope.launch { prefs.setDailyGoalMinutes(minutes) }
    fun setDailyCardLimit(limit: Int) = viewModelScope.launch { prefs.setDailyCardLimit(limit) }
    fun redoOnboarding() = viewModelScope.launch { prefs.resetOnboarding() }

    suspend fun exportProgressJson(): String {
        val bundle = ExportBundle(
            exportedAt = System.currentTimeMillis(),
            lessonProgress = db.lessonProgressDao().getAll(),
            questionAttempts = db.questionAttemptDao().getAll(),
            flashcardState = db.flashcardStateDao().getAll(),
            mockAttempts = db.mockAttemptDao().getAll(),
            streakDays = db.streakDayDao().getAll()
        )
        return exportJson.encodeToString(bundle)
    }

    fun resetProgress(onDone: () -> Unit) {
        viewModelScope.launch {
            db.lessonProgressDao().clearAll()
            db.questionAttemptDao().clearAll()
            db.flashcardStateDao().clearAll()
            db.mockAttemptDao().clearAll()
            db.streakDayDao().clearAll()
            onDone()
        }
    }

    class Factory(private val app: PrepApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(app.database, app.userPrefsRepository) as T
        }
    }
}
