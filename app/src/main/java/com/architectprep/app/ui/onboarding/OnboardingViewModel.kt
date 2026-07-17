package com.architectprep.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.architectprep.app.PrepApplication
import com.architectprep.app.data.prefs.UserPrefsRepository
import kotlinx.coroutines.launch

class OnboardingViewModel(private val prefs: UserPrefsRepository) : ViewModel() {

    fun complete(examDateMillis: Long?, dailyGoalMinutes: Int, onDone: () -> Unit) {
        viewModelScope.launch {
            prefs.completeOnboarding(examDateMillis, dailyGoalMinutes)
            onDone()
        }
    }

    class Factory(private val app: PrepApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingViewModel(app.userPrefsRepository) as T
        }
    }
}
