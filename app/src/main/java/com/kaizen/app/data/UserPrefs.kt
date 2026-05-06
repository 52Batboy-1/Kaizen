package com.kaizen.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "kaizen_prefs")

class UserPrefs(private val context: Context) {

    companion object {
        private val TIER_KEY            = stringPreferencesKey("current_tier")
        private val ONBOARDING_DONE_KEY = booleanPreferencesKey("onboarding_done")
        private val USER_NAME_KEY       = stringPreferencesKey("user_name")
        private val CURRENT_WEEK_KEY    = intPreferencesKey("current_week")
    }

    val currentTier: Flow<KaizenTier> = context.dataStore.data.map { prefs ->
        runCatching { KaizenTier.valueOf(prefs[TIER_KEY] ?: "") }.getOrDefault(KaizenTier.FOUNDATION)
    }

    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ONBOARDING_DONE_KEY] ?: false
    }

    val userName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[USER_NAME_KEY] ?: ""
    }

    val currentWeek: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[CURRENT_WEEK_KEY] ?: 1
    }

    suspend fun setTier(tier: KaizenTier) {
        context.dataStore.edit { it[TIER_KEY] = tier.name }
    }

    suspend fun completeOnboarding(name: String, week: Int) {
        context.dataStore.edit { prefs ->
            prefs[ONBOARDING_DONE_KEY] = true
            prefs[USER_NAME_KEY]       = name
            prefs[CURRENT_WEEK_KEY]    = week
        }
    }

    suspend fun setCurrentWeek(week: Int) {
        context.dataStore.edit { it[CURRENT_WEEK_KEY] = week }
    }
}
