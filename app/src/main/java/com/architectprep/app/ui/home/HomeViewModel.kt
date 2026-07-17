package com.architectprep.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.architectprep.app.data.content.ContentImporter
import com.architectprep.app.data.db.AppDatabase
import com.architectprep.app.data.db.entity.DomainEntity
import com.architectprep.app.data.db.entity.TrackEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class DomainRow(
    val code: String,
    val title: String,
    val weightPct: Int,
    val lessonsDone: Int,
    val lessonsTotal: Int
)

data class HomeUiState(
    val trackTitle: String,
    val questionCount: Int,
    val timeLimitMin: Int,
    val passScore: Int,
    val scoreScale: Int,
    val domains: List<DomainRow>
)

class HomeViewModel(
    private val db: AppDatabase,
    private val importer: ContentImporter
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
            db.lessonProgressDao().observeDoneLessonIds()
        ) { track, domains, doneIds -> Triple(track, domains, doneIds) }
            .collect { (track, domains, doneIds) ->
                if (track == null) return@collect
                val doneSet = doneIds.toSet()
                val rows = domains.map { d -> toRow(d, doneSet) }
                _uiState.value = HomeUiState(
                    trackTitle = track.title,
                    questionCount = track.questionCount,
                    timeLimitMin = track.timeLimitMin,
                    passScore = track.passScore,
                    scoreScale = track.scoreScale,
                    domains = rows
                )
            }
    }

    private suspend fun toRow(d: DomainEntity, doneIds: Set<String>): DomainRow {
        val lessons = db.lessonDao().observeByDomain(d.id).first()
        val done = lessons.count { it.id in doneIds }
        return DomainRow(d.code, d.title, d.weightPct, lessonsDone = done, lessonsTotal = lessons.size)
    }

    class Factory(private val app: com.architectprep.app.PrepApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(app.database, app.contentImporter) as T
        }
    }
}
