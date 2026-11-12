package com.command.polyshelf.domain.settings.model

data class UserSettings(
    val isNotificationsEnabled: Boolean = DEFAULT_ENABLED,
    val notificationHour: Int = DEFAULT_HOUR,
    val notificationMinute: Int = DEFAULT_MINUTE,
    val thresholdDays: Int = DEFAULT_THRESHOLD_DAYS
) {
    companion object {
        const val DEFAULT_ENABLED = false
        const val DEFAULT_HOUR = 9
        const val DEFAULT_MINUTE = 0
        const val DEFAULT_THRESHOLD_DAYS = 3
    }
}
