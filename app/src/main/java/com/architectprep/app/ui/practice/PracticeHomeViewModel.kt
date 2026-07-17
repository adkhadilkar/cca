package com.architectprep.app.ui.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.architectprep.app.PrepApplication
import com.architectprep.app.data.db.AppDatabase
import com.architectprep.app.data.db.entity.DomainEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class PracticeDomainRow(
    val id: String,
    val code: String,
    val title: String,
    val weightPct: Int,
    val questionCount: Int,
    val attempted: Int,
    val correct: Int
)

data class PracticeHomeUiState(
    val domains: List<PracticeDomainRow>,
    val flashcardsDue: Int
)

class PracticeHomeViewModel(private val db: AppDatabase) : ViewModel() {

    private val _uiState = MutableStateFlow<PracticeHomeUiState?>(null)
    val uiState: StateFlow<PracticeHomeUiState?> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            db.domainDao().observeByTrack("CCAR-F").collect { domains ->
                val rows = domains.map { d -> toRow(d) }
                val dueCount = db.flashcardStateDao().dueCount(System.currentTimeMillis())
                _uiState.value = PracticeHomeUiState(domains = rows, flashcardsDue = dueCount)
            }
        }
    }

    private suspend fun toRow(d: DomainEntity): PracticeDomainRow {
        val questions = db.questionDao().getByDomain(d.id)
        val attempted = db.questionAttemptDao().observeAttemptCountForDomain(d.id).first()
        val correct = db.questionAttemptDao().observeCorrectCountForDomain(d.id).first()
        return PracticeDomainRow(d.id, d.code, d.title, d.weightPct, questions.size, attempted, correct)
    }

    class Factory(private val app: PrepApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PracticeHomeViewModel(app.database) as T
        }
    }
}
