package com.command.polyshelf.utils

import android.content.BroadcastReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

fun BroadcastReceiver.goAsync(
    context: CoroutineContext = Dispatchers.IO,
    block: suspend CoroutineScope.() -> Unit
) {
    val pendingResult = goAsync()
    CoroutineScope(context).launch {
        try {
            block()
        } catch (e: Exception) {
            Timber.e(e, "Error executing BroadcastReceiver asynchronously")
        } finally {
            pendingResult.finish()
        }
    }
}
