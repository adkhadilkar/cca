package com.architectprep.app.ui.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.architectprep.app.PrepApplication
import com.architectprep.app.data.content.ChoiceDto
import com.architectprep.app.data.db.AppDatabase
import com.architectprep.app.data.db.entity.QuestionAttemptEntity
import com.architectprep.app.data.db.entity.QuestionEntity
import com.architectprep.app.data.prefs.UserPrefsRepository
import com.architectprep.app.domain.StreakTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.UUID

data class PracticeQuestion(
    val id: String,
    val domainId: String,
    val stem: String,
    val choices: List<ChoiceDto>,
    val correctChoiceIds: Set<String>,
    val explanation: String,
    val sourceRef: String
)

data class PracticeSessionUiState(
    val domainId: String,
    val domainCode: String,
    val domainTitle: String,
    val index: Int,
    val total: Int,
    val question: PracticeQuestion?,
    val selectedChoiceId: String?,
    val revealed: Boolean,
    val sessionComplete: Boolean,
    val sessionCorrect: Int,
    val flashcardAdded: Boolean
)

class PracticeSessionViewModel(
    private val db: AppDatabase,
    private val prefs: UserPrefsRepository,
    private val domainId: String
) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true }
    private var questions: List<PracticeQuestion> = emptyList()
    private var index = 0
    private var selectedChoiceId: String? = null
    private var revealed = false
    private var sessionCorrect = 0
    private var questionStartMs = 0L

    private val _uiState = MutableStateFlow<PracticeSessionUiState?>(null)
    val uiState: StateFlow<PracticeSessionUiState?> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val d = db.domainDao().observeById(domainId).first() ?: return@launch
            val entities = db.questionDao().getByDomain(domainId).shuffled()
            questions = entities.map { toQuestion(it) }
            questionStartMs = System.currentTimeMillis()
            emit(d.code, d.title)
        }
    }

    private fun toQuestion(e: QuestionEntity): PracticeQuestion {
        val choices = json.decodeFromString<List<ChoiceDto>>(e.choicesJson)
        val correct = json.decodeFromString<List<String>>(e.correctJson).toSet()
        return PracticeQuestion(e.id, e.domainId, e.stem, choices, correct, e.explanation, e.sourceRef)
    }

    private val addedToDeck = mutableSetOf<String>()

    private fun emit(domainCode: String, domainTitle: String) {
        _uiState.value = PracticeSessionUiState(
            domainId = domainId,
            domainCode = domainCode,
            domainTitle = domainTitle,
            index = index,
            total = questions.size,
            question = questions.getOrNull(index),
            selectedChoiceId = selectedChoiceId,
            revealed = revealed,
            sessionComplete = index >= questions.size,
            sessionCorrect = sessionCorrect,
            flashcardAdded = questions.getOrNull(index)?.id in addedToDeck
        )
    }

    /** Puts the current question at the front of the flashcard queue (due now). */
    fun addToFlashcards() {
        val q = questions.getOrNull(index) ?: return
        if (q.id in addedToDeck) return
        addedToDeck.add(q.id)
        viewModelScope.launch {
            val existing = db.flashcardStateDao().get(q.id)
            db.flashcardStateDao().upsert(
                existing?.copy(dueAt = System.currentTimeMillis())
                    ?: com.architectprep.app.data.db.entity.FlashcardStateEntity(
                        cardId = q.id,
                        ease = 2.5,
                        intervalDays = 0.0,
                        dueAt = System.currentTimeMillis(),
                        reps = 0,
                        lapses = 0,
                        lastGrade = null
                    )
            )
        }
        _uiState.value = _uiState.value?.copy(flashcardAdded = true)
    }

    fun selectChoice(choiceId: String) {
        if (revealed) return
        val q = questions.getOrNull(index) ?: return
        selectedChoiceId = choiceId
        revealed = true
        val correct = choiceId in q.correctChoiceIds
        if (correct) sessionCorrect++
        viewModelScope.launch {
            db.questionAttemptDao().insert(
                QuestionAttemptEntity(
                    id = UUID.randomUUID().toString(),
                    questionId = q.id,
                    domainId = q.domainId,
                    chosenChoiceId = choiceId,
                    correct = correct,
                    timeMs = System.currentTimeMillis() - questionStartMs,
                    ts = System.currentTimeMillis()
                )
            )
            StreakTracker.recordActivity(db, prefs.prefs.first().dailyGoalMinutes)
        }
        val s = _uiState.value ?: return
        _uiState.value = s.copy(selectedChoiceId = selectedChoiceId, revealed = true, sessionCorrect = sessionCorrect)
    }

    fun nextQuestion() {
        if (!revealed) return
        index++
        selectedChoiceId = null
        revealed = false
        questionStartMs = System.currentTimeMillis()
        val s = _uiState.value ?: return
        emit(s.domainCode, s.domainTitle)
    }

    class Factory(private val app: PrepApplication, private val domainId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PracticeSessionViewModel(app.database, app.userPrefsRepository, domainId) as T
        }
    }
}
