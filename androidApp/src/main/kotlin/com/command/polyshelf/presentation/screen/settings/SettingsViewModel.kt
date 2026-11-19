package com.command.polyshelf.presentation.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.command.polyshelf.data.notification.NotificationScheduler
import com.command.polyshelf.data.settings.SettingsStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsStorage: SettingsStorage,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    val state: StateFlow<SettingsState>
        field = MutableStateFlow(SettingsState())

    init {
        viewModelScope.launch {
            settingsStorage.settingsFlow.collect { settings ->
                state.update {
                    it.copy(
                        isNotificationsEnabled = settings.isNotificationsEnabled,
                        notificationHour = settings.notificationHour,
                        notificationMinute = settings.notificationMinute,
                        thresholdDays = settings.thresholdDays
                    )
                }
            }
        }
    }

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.ToggleNotifications -> handleToggleNotifications(intent.enabled)
            is SettingsIntent.SetNotificationTime -> handleSetNotificationTime(
                intent.hour,
                intent.minute
            )

            is SettingsIntent.SetThresholdDays -> handleSetThresholdDays(intent.days)
        }
    }

    private fun handleToggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsStorage.updateEnabled(enabled)
            val s = state.value
            if (enabled) {
                notificationScheduler.scheduleDailyNotification(
                    s.notificationHour,
                    s.notificationMinute
                )
            } else {
                notificationScheduler.cancelNotification()
            }
        }
    }

    private fun handleSetNotificationTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            settingsStorage.updateTime(hour, minute)
            if (state.value.isNotificationsEnabled) {
                notificationScheduler.scheduleDailyNotification(hour, minute)
            }
        }
    }

    private fun handleSetThresholdDays(days: Int) {
        viewModelScope.launch {
            settingsStorage.updateThresholdDays(days)
        }
    }
}

data class SettingsState(
    val isNotificationsEnabled: Boolean = false,
    val notificationHour: Int = 9,
    val notificationMinute: Int = 0,
    val thresholdDays: Int = 3
)

sealed interface SettingsIntent {
    data class ToggleNotifications(val enabled: Boolean) : SettingsIntent
    data class SetNotificationTime(val hour: Int, val minute: Int) : SettingsIntent
    data class SetThresholdDays(val days: Int) : SettingsIntent
}
