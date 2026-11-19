package com.command.polyshelf.utils

fun parseExpirationDate(dateStr: String): String? {
    if (dateStr.isBlank()) return null
    val datePart = dateStr.trim().split("T").firstOrNull() ?: return null
    val parts = datePart.split("-")
    if (parts.size < 3) return null
    val year = parts[0].padStart(4, '0')
    val month = parts[1].padStart(2, '0')
    val day = parts[2].substring(0, 2).padStart(2, '0')
    return "$year-$month-$day"
}

fun parseEpochDays(dateStr: String): Long? {
    if (dateStr.isBlank()) return null
    val datePart = dateStr.trim().split("T").firstOrNull() ?: return null
    val parts = datePart.split("-")
    if (parts.size < 3) return null
    val year = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val day = parts[2].substring(0, 2).toIntOrNull() ?: return null
    return toEpochDays(year, month, day)
}

fun toEpochDays(year: Int, month: Int, day: Int): Long {
    var y = year.toLong()
    var m = month.toLong()
    if (m <= 2) {
        y -= 1
        m += 12
    }
    val era = (if (y >= 0) y else y - 399) / 400
    val yoe = y - era * 400
    val doy = (153 * (m - 3) + 2) / 5 + day - 1
    val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
    return era * 146097 + doe - 719468
}
