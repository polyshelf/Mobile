package com.command.polyshelf.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.command.polyshelf.data.settings.SettingsStorage
import com.command.polyshelf.domain.product.model.FreshnessStatus
import com.command.polyshelf.domain.product.model.InventoryItem
import com.command.polyshelf.domain.product.usecase.GetInventoryUseCase
import com.command.polyshelf.utils.currentTimeMillis
import com.command.polyshelf.utils.parseEpochDays
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class NotificationAlarmReceiver : BroadcastReceiver(), KoinComponent {

    private val getInventoryUseCase: GetInventoryUseCase by inject()
    private val settingsStorage: SettingsStorage by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        try {
            runBlocking {
                val settings = settingsStorage.settingsFlow.first()
                if (settings.isNotificationsEnabled) {
                    // Reschedule for next day FIRST so alarm is guaranteed to recur daily
                    val scheduler = NotificationScheduler(context)
                    scheduler.scheduleDailyNotification(
                        settings.notificationHour,
                        settings.notificationMinute
                    )

                    try {
                        val items = getInventoryUseCase().first()
                        val (expiringSoonCount, expiredCount) = calculateExpiringCounts(
                            items,
                            settings.thresholdDays
                        )

                        if (expiringSoonCount > 0 || expiredCount > 0) {
                            val helper = NotificationHelper(context)
                            helper.showExpiringProductsNotification(expiringSoonCount, expiredCount)
                        } else {
                            val helper = NotificationHelper(context)
                            helper.showExpiringProductsNotification(1, 0)
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error calculating product expiration counts")
                        val helper = NotificationHelper(context)
                        helper.showExpiringProductsNotification(1, 0)
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error handling alarm in BroadcastReceiver")
        } finally {
            pendingResult.finish()
        }
    }

    private fun calculateExpiringCounts(
        items: List<InventoryItem>,
        thresholdDays: Int
    ): Pair<Int, Int> {
        val currentEpochDays = currentTimeMillis() / MILLIS_PER_DAY
        var expiringSoonCount = 0
        var expiredCount = 0

        for ((_, _, expirationDate, _, freshnessStatus) in items) {
            val expDays = parseEpochDays(expirationDate)
            if (expDays != null) {
                val daysRemaining = expDays - currentEpochDays
                if (daysRemaining < 0) {
                    expiredCount++
                } else if (daysRemaining <= thresholdDays) {
                    expiringSoonCount++
                }
            } else if (freshnessStatus == FreshnessStatus.EXPIRED) {
                expiredCount++
            } else if (freshnessStatus == FreshnessStatus.EXPIRING_SOON) {
                expiringSoonCount++
            }
        }

        return Pair(expiringSoonCount, expiredCount)
    }

    companion object {
        private const val MILLIS_PER_DAY = 24 * 60 * 60 * 1000L
    }
}
