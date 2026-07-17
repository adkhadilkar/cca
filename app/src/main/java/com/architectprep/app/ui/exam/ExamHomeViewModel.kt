package com.architectprep.app.ui.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.architectprep.app.PrepApplication
import com.architectprep.app.data.db.AppDatabase
import com.architectprep.app.data.db.entity.MockAttemptEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class ExamHomeUiState(
    val trackCode: String,
    val questionCount: Int,
    val timeLimitMin: Int,
    val passScore: Int,
    val inProgress: MockAttemptEntity?,
    val history: List<MockAttemptEntity>
)

class ExamHomeViewModel(private val db: AppDatabase) : ViewModel() {

    private val _uiState = MutableStateFlow<ExamHomeUiState?>(null)
    val uiState: StateFlow<ExamHomeUiState?> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val trackCode = "CCAR-F"
            val track = db.trackDao().get(trackCode) ?: return@launch
            db.mockAttemptDao().observeSubmitted(trackCode).collect { history ->
                val inProgress = db.mockAttemptDao().latestInProgress(trackCode)
                _uiState.value = ExamHomeUiState(
                    trackCode = trackCode,
                    questionCount = track.questionCount,
                    timeLimitMin = track.timeLimitMin,
                    passScore = track.passScore,
                    inProgress = inProgress,
                    history = history
                )
            }
        }
    }

    suspend fun startNewAttempt(): String {
        val trackCode = "CCAR-F"
        val id = UUID.randomUUID().toString()
        db.mockAttemptDao().upsert(
            MockAttemptEntity(
                id = id,
                trackCode = trackCode,
                startedAt = System.currentTimeMillis(),
                submittedAt = null,
                score = null,
                perDomainJson = null,
                answersJson = "{}",
                flaggedJson = "[]",
                status = "in_progress"
            )
        )
        return id
    }

    class Factory(private val app: PrepApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExamHomeViewModel(app.database) as T
        }
    }
}
