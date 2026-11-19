package com.command.polyshelf.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.command.polyshelf.domain.settings.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsStorage(
    private val dataStore: DataStore<Preferences>
) {
    val settingsFlow: Flow<UserSettings> = dataStore.data.map { preferences ->
        UserSettings(
            isNotificationsEnabled = preferences[KEY_ENABLED] ?: UserSettings.DEFAULT_ENABLED,
            notificationHour = preferences[KEY_HOUR] ?: UserSettings.DEFAULT_HOUR,
            notificationMinute = preferences[KEY_MINUTE] ?: UserSettings.DEFAULT_MINUTE,
            thresholdDays = preferences[KEY_THRESHOLD_DAYS] ?: UserSettings.DEFAULT_THRESHOLD_DAYS
        )
    }

    suspend fun updateEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_ENABLED] = enabled
        }
    }

    suspend fun updateTime(hour: Int, minute: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_HOUR] = hour
            preferences[KEY_MINUTE] = minute
        }
    }

    suspend fun updateThresholdDays(days: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_THRESHOLD_DAYS] = days
        }
    }

    companion object {
        private val KEY_ENABLED = booleanPreferencesKey("notification_enabled")
        private val KEY_HOUR = intPreferencesKey("notification_hour")
        private val KEY_MINUTE = intPreferencesKey("notification_minute")
        private val KEY_THRESHOLD_DAYS = intPreferencesKey("expiring_threshold_days")
    }
}
