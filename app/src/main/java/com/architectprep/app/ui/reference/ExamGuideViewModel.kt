package com.architectprep.app.ui.reference

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.architectprep.app.PrepApplication
import com.architectprep.app.data.content.GuideDto
import com.architectprep.app.data.content.GuideRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExamGuideViewModel(repository: GuideRepository) : ViewModel() {

    private val _guide = MutableStateFlow<GuideDto?>(null)
    val guide: StateFlow<GuideDto?> = _guide.asStateFlow()

    init {
        viewModelScope.launch {
            _guide.value = repository.load()
        }
    }

    class Factory(private val app: PrepApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExamGuideViewModel(app.guideRepository) as T
        }
    }
}
