package com.command.polyfood

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform