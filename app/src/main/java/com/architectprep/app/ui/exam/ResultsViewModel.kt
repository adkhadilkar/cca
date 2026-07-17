package com.architectprep.app.ui.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.architectprep.app.PrepApplication
import com.architectprep.app.data.content.ChoiceDto
import com.architectprep.app.data.db.AppDatabase
import com.architectprep.app.domain.PerDomainScore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

data class MissedQuestionUi(
    val stem: String,
    val choices: List<ChoiceDto>,
    val correctChoiceIds: Set<String>,
    val chosenChoiceId: String?,
    val explanation: String
)

data class ResultsUiState(
    val score: Int,
    val passScore: Int,
    val passed: Boolean,
    val correct: Int,
    val total: Int,
    val perDomain: List<PerDomainScore>,
    val missed: List<MissedQuestionUi>
)

class ResultsViewModel(private val db: AppDatabase, private val attemptId: String) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true }

    private val _uiState = MutableStateFlow<ResultsUiState?>(null)
    val uiState: StateFlow<ResultsUiState?> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val attempt = db.mockAttemptDao().get(attemptId) ?: return@launch
            val track = db.trackDao().get(attempt.trackCode) ?: return@launch
            val perDomain = attempt.perDomainJson?.let { json.decodeFromString<List<PerDomainScore>>(it) } ?: emptyList()
            val answers = json.decodeFromString<Map<String, String>>(attempt.answersJson)

            val totalCorrect = perDomain.sumOf { it.correct }
            val totalQuestions = perDomain.sumOf { it.total }

            val missed = answers.entries.mapNotNull { (questionId, chosenId) ->
                val q = db.questionDao().get(questionId) ?: return@mapNotNull null
                val correctIds = json.decodeFromString<List<String>>(q.correctJson).toSet()
                if (chosenId in correctIds) return@mapNotNull null
                MissedQuestionUi(
                    stem = q.stem,
                    choices = json.decodeFromString(q.choicesJson),
                    correctChoiceIds = correctIds,
                    chosenChoiceId = chosenId,
                    explanation = q.explanation
                )
            }

            _uiState.value = ResultsUiState(
                score = attempt.score ?: 0,
                passScore = track.passScore,
                passed = (attempt.score ?: 0) >= track.passScore,
                correct = totalCorrect,
                total = totalQuestions,
                perDomain = perDomain,
                missed = missed
            )
        }
    }

    class Factory(private val app: PrepApplication, private val attemptId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ResultsViewModel(app.database, attemptId) as T
        }
    }
}
