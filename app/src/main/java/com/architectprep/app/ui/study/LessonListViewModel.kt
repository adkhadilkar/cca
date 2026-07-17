package com.architectprep.app.ui.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.architectprep.app.PrepApplication
import com.architectprep.app.data.db.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class LessonRow(
    val id: String,
    val orderIndex: Int,
    val title: String,
    val estMinutes: Int,
    val done: Boolean
)

data class LessonListUiState(
    val domainCode: String,
    val domainTitle: String,
    val lessons: List<LessonRow>
)

class LessonListViewModel(
    private val db: AppDatabase,
    private val domainId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<LessonListUiState?>(null)
    val uiState: StateFlow<LessonListUiState?> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                db.domainDao().observeById(domainId),
                db.lessonDao().observeByDomain(domainId),
                db.lessonProgressDao().observeDoneLessonIds()
            ) { domain, lessons, doneIds ->
                if (domain == null) return@combine null
                val doneSet = doneIds.toSet()
                LessonListUiState(
                    domainCode = domain.code,
                    domainTitle = domain.title,
                    lessons = lessons.map { l -> LessonRow(l.id, l.orderIndex, l.title, l.estMinutes, l.id in doneSet) }
                )
            }.collect { _uiState.value = it }
        }
    }

    class Factory(private val app: PrepApplication, private val domainId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LessonListViewModel(app.database, domainId) as T
        }
    }
}
