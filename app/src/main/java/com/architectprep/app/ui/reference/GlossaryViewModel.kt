package com.architectprep.app.ui.reference

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.architectprep.app.PrepApplication
import com.architectprep.app.data.db.AppDatabase
import com.architectprep.app.data.db.entity.GlossaryTermEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

val GLOSSARY_CATEGORIES = listOf("Agents", "MCP", "Claude Code", "API")

@OptIn(ExperimentalCoroutinesApi::class)
class GlossaryViewModel(private val db: AppDatabase) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _category = MutableStateFlow<String?>(null)
    val category: StateFlow<String?> = _category.asStateFlow()

    private val allMatching: StateFlow<List<GlossaryTermEntity>> = _query
        .flatMapLatest { q -> db.glossaryDao().search(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val terms: StateFlow<List<GlossaryTermEntity>> = combine(allMatching, _category) { list, cat ->
        if (cat == null) list else list.filter { it.category == cat }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onQueryChange(q: String) {
        _query.value = q
    }

    fun onCategoryChange(cat: String?) {
        _category.value = cat
    }

    class Factory(private val app: PrepApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GlossaryViewModel(app.database) as T
        }
    }
}
