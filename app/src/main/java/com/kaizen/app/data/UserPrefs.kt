package com.kaizen.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "kaizen_prefs")

class UserPrefs(private val context: Context) {

    companion object {
        private val TIER_KEY = stringPreferencesKey("current_tier")
    }

    val currentTier: Flow<KaizenTier> = context.dataStore.data.map { prefs ->
        runCatching { KaizenTier.valueOf(prefs[TIER_KEY] ?: "") }.getOrDefault(KaizenTier.FOUNDATION)
    }

    suspend fun setTier(tier: KaizenTier) {
        context.dataStore.edit { it[TIER_KEY] = tier.name }
    }
}
