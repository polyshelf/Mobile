package com.command.polyshelf.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.command.polyshelf.data.settings.SettingsStorage
import com.command.polyshelf.utils.goAsync
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BootReceiver : BroadcastReceiver(), KoinComponent {

    private val settingsStorage: SettingsStorage by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            goAsync {
                handleBootCompleted(context)
            }
        }
    }

    private suspend fun handleBootCompleted(context: Context) {
        val settings = settingsStorage.settingsFlow.first()
        if (settings.isNotificationsEnabled) {
            val scheduler = NotificationScheduler(context)
            scheduler.scheduleDailyNotification(
                settings.notificationHour,
                settings.notificationMinute
            )
        }
    }
}
