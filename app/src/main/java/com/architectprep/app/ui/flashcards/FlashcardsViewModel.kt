package com.architectprep.app.ui.flashcards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.architectprep.app.PrepApplication
import com.architectprep.app.data.content.ChoiceDto
import com.architectprep.app.data.db.AppDatabase
import com.architectprep.app.data.db.entity.FlashcardStateEntity
import com.architectprep.app.data.db.entity.QuestionEntity
import com.architectprep.app.data.prefs.UserPrefsRepository
import com.architectprep.app.domain.Grade
import com.architectprep.app.domain.SchedulerState
import com.architectprep.app.domain.SpacedRepetitionScheduler
import com.architectprep.app.domain.StreakTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

data class FlashcardUi(
    val cardId: String,
    val domainCode: String,
    val front: String,
    val back: String
)

data class FlashcardsUiState(
    val totalInSession: Int,
    val index: Int,
    val card: FlashcardUi?,
    val flipped: Boolean,
    val sessionComplete: Boolean,
    val newCount: Int,
    val dueCount: Int
)

class FlashcardsViewModel(private val db: AppDatabase, private val prefs: UserPrefsRepository) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true }
    private var deck: List<FlashcardUi> = emptyList()
    private var index = 0
    private var flipped = false
    private var dueCount = 0
    private var newCount = 0

    private val _uiState = MutableStateFlow<FlashcardsUiState?>(null)
    val uiState: StateFlow<FlashcardsUiState?> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val due = db.flashcardStateDao().dueCards(now, limit = 50)
            val newIds = db.flashcardStateDao().newCardIds(limit = 10)
            dueCount = due.size
            newCount = newIds.size

            val cardIds = (due.map { it.cardId } + newIds).distinct().shuffled()
            deck = cardIds.mapNotNull { id -> db.questionDao().get(id)?.let { toCard(it) } }
            emit()
        }
    }

    private fun toCard(q: QuestionEntity): FlashcardUi {
        val choices = json.decodeFromString<List<ChoiceDto>>(q.choicesJson)
        val correctIds = json.decodeFromString<List<String>>(q.correctJson).toSet()
        val correctText = choices.firstOrNull { it.id in correctIds }?.text ?: ""
        return FlashcardUi(
            cardId = q.id,
            domainCode = q.domainId.uppercase(),
            front = q.stem,
            back = "$correctText\n\n${q.explanation}"
        )
    }

    private fun emit() {
        _uiState.value = FlashcardsUiState(
            totalInSession = deck.size,
            index = index,
            card = deck.getOrNull(index),
            flipped = flipped,
            sessionComplete = index >= deck.size,
            newCount = newCount,
            dueCount = dueCount
        )
    }

    fun flip() {
        if (_uiState.value?.sessionComplete == true) return
        flipped = !flipped
        emit()
    }

    fun grade(grade: Grade) {
        val card = deck.getOrNull(index) ?: return
        viewModelScope.launch {
            val existing = db.flashcardStateDao().get(card.cardId)
            val state = existing?.let {
                SchedulerState(it.ease, it.intervalDays, it.reps, it.lapses)
            } ?: SchedulerState.NEW
            val next = SpacedRepetitionScheduler.grade(state, grade)
            val now = System.currentTimeMillis()
            db.flashcardStateDao().upsert(
                FlashcardStateEntity(
                    cardId = card.cardId,
                    ease = next.ease,
                    intervalDays = next.intervalDays,
                    dueAt = SpacedRepetitionScheduler.dueAt(now, next.intervalDays),
                    reps = next.reps,
                    lapses = next.lapses,
                    lastGrade = grade.name.lowercase()
                )
            )
            StreakTracker.recordActivity(db, prefs.prefs.first().dailyGoalMinutes)
        }
        index++
        flipped = false
        emit()
    }

    class Factory(private val app: PrepApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FlashcardsViewModel(app.database, app.userPrefsRepository) as T
        }
    }
}
