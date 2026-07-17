package com.architectprep.app.ui.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.architectprep.app.PrepApplication
import com.architectprep.app.data.content.LessonBlockDto
import com.architectprep.app.data.db.AppDatabase
import com.architectprep.app.data.db.entity.LessonProgressEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

data class LessonDetailUiState(
    val lessonId: String,
    val domainCode: String,
    val domainTitle: String,
    val title: String,
    val estMinutes: Int,
    val indexInDomain: Int,
    val totalInDomain: Int,
    val body: List<LessonBlockDto>,
    val done: Boolean,
    val prevLessonId: String?,
    val nextLessonId: String?
)

class LessonDetailViewModel(
    private val db: AppDatabase,
    private val lessonId: String
) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true }

    private val _uiState = MutableStateFlow<LessonDetailUiState?>(null)
    val uiState: StateFlow<LessonDetailUiState?> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val lesson = db.lessonDao().get(lessonId) ?: return@launch
            combine(
                db.domainDao().observeById(lesson.domainId),
                db.lessonDao().observeByDomain(lesson.domainId),
                db.lessonProgressDao().observeDoneLessonIds()
            ) { domain, siblings, doneIds ->
                if (domain == null) return@combine null
                val idx = siblings.indexOfFirst { it.id == lessonId }
                LessonDetailUiState(
                    lessonId = lesson.id,
                    domainCode = domain.code,
                    domainTitle = domain.title,
                    title = lesson.title,
                    estMinutes = lesson.estMinutes,
                    indexInDomain = idx + 1,
                    totalInDomain = siblings.size,
                    body = json.decodeFromString<List<LessonBlockDto>>(lesson.bodyJson),
                    done = lesson.id in doneIds,
                    prevLessonId = siblings.getOrNull(idx - 1)?.id,
                    nextLessonId = if (idx in siblings.indices) siblings.getOrNull(idx + 1)?.id else null
                )
            }.collect { _uiState.value = it }
        }
    }

    fun toggleDone() {
        viewModelScope.launch {
            val current = _uiState.value ?: return@launch
            val newStatus = if (current.done) "in_progress" else "done"
            db.lessonProgressDao().upsert(
                LessonProgressEntity(
                    lessonId = current.lessonId,
                    status = newStatus,
                    lastViewedAt = System.currentTimeMillis()
                )
            )
        }
    }

    class Factory(private val app: PrepApplication, private val lessonId: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LessonDetailViewModel(app.database, lessonId) as T
        }
    }
}
