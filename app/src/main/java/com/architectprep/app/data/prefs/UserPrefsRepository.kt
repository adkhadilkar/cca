package com.architectprep.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

enum class ThemePref { SYSTEM, LIGHT, DARK }

data class UserPrefs(
    val onboarded: Boolean,
    val theme: ThemePref,
    val examDateMillis: Long?,
    val dailyGoalMinutes: Int,
    val dailyCardLimit: Int
)

/** Settings & onboarding state — DataStore, not Room (docs/DEVELOPMENT_DESIGN.md §3.1, §5.2). */
class UserPrefsRepository(private val context: Context) {
    private object Keys {
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val THEME = stringPreferencesKey("theme")
        val EXAM_DATE = longPreferencesKey("exam_date")
        val DAILY_GOAL_MIN = intPreferencesKey("daily_goal_min")
        val DAILY_CARD_LIMIT = intPreferencesKey("daily_card_limit")
    }

    val prefs: Flow<UserPrefs> = context.dataStore.data.map { p ->
        UserPrefs(
            onboarded = p[Keys.ONBOARDED] ?: false,
            theme = p[Keys.THEME]?.let { runCatching { ThemePref.valueOf(it) }.getOrNull() } ?: ThemePref.SYSTEM,
            examDateMillis = p[Keys.EXAM_DATE],
            dailyGoalMinutes = p[Keys.DAILY_GOAL_MIN] ?: 20,
            dailyCardLimit = p[Keys.DAILY_CARD_LIMIT] ?: 20
        )
    }

    suspend fun completeOnboarding(examDateMillis: Long?, dailyGoalMinutes: Int) {
        context.dataStore.edit { p ->
            p[Keys.ONBOARDED] = true
            p[Keys.DAILY_GOAL_MIN] = dailyGoalMinutes
            if (examDateMillis != null) p[Keys.EXAM_DATE] = examDateMillis
        }
    }

    suspend fun setTheme(theme: ThemePref) {
        context.dataStore.edit { it[Keys.THEME] = theme.name }
    }

    suspend fun setExamDate(millis: Long?) {
        context.dataStore.edit { p -> if (millis == null) p.remove(Keys.EXAM_DATE) else p[Keys.EXAM_DATE] = millis }
    }

    suspend fun setDailyGoalMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.DAILY_GOAL_MIN] = minutes }
    }

    suspend fun setDailyCardLimit(limit: Int) {
        context.dataStore.edit { it[Keys.DAILY_CARD_LIMIT] = limit }
    }

    suspend fun resetOnboarding() {
        context.dataStore.edit { it[Keys.ONBOARDED] = false }
    }
}
