package com.command.polyshelf.utils

actual object Logger {
    actual fun d(tag: String, message: String) {
        println("[$tag] DEBUG: $message")
    }

    actual fun e(tag: String, message: String, throwable: Throwable?) {
        println("[$tag] ERROR: $message ${throwable?.message ?: ""}")
    }
}
