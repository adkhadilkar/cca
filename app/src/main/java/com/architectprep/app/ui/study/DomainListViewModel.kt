package com.architectprep.app.ui.study

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

data class DomainProgressRow(
    val id: String,
    val code: String,
    val title: String,
    val summary: String,
    val weightPct: Int,
    val lessonsDone: Int,
    val lessonsTotal: Int
)

class DomainListViewModel(private val db: AppDatabase) : ViewModel() {

    private val _domains = MutableStateFlow<List<DomainProgressRow>?>(null)
    val domains: StateFlow<List<DomainProgressRow>?> = _domains.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                db.domainDao().observeByTrack("CCAR-F"),
                db.lessonProgressDao().observeDoneLessonIds()
            ) { domains, doneIds -> domains to doneIds }
                .collect { (domains, doneIds) ->
                    _domains.value = domains.map { d -> toRow(d, doneIds.toSet()) }
                }
        }
    }

    private suspend fun toRow(d: DomainEntity, doneIds: Set<String>): DomainProgressRow {
        val lessons = db.lessonDao().observeByDomain(d.id).first()
        val done = lessons.count { it.id in doneIds }
        return DomainProgressRow(
            id = d.id,
            code = d.code,
            title = d.title,
            summary = d.summary,
            weightPct = d.weightPct,
            lessonsDone = done,
            lessonsTotal = lessons.size
        )
    }

    class Factory(private val app: PrepApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DomainListViewModel(app.database) as T
        }
    }
}
