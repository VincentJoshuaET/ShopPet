package com.vt.shoppet.util

import com.google.android.material.datepicker.CalendarConstraints
import com.google.firebase.Timestamp
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

val localZoneId: ZoneId = ZoneId.of("Asia/Manila")
val dateOfBirthFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMMM d, yyyy").withZone(localZoneId)

fun Timestamp.calculateAge(): String {
    val instant = Instant.ofEpochSecond(seconds)
    val seconds = Duration.between(instant, Instant.now()).seconds
    val days = (seconds / 60 / 60 / 24).toInt()
    val weeks = days / 7
    val months = weeks / 4
    val years = months / 12

    return when {
        years >= 1 -> if (years == 1) "$years year old" else "$years years old"
        months >= 1 -> if (months == 1) "$months month old" else "$months months old"
        weeks >= 1 -> if (weeks == 1) "$weeks week old" else "$weeks weeks old"
        else -> if (days == 1) "$days day old" else "$days days old"
    }
}

fun Timestamp.calculateEditableAge(): Pair<String, String> {
    val instant = Instant.ofEpochSecond(seconds)
    val seconds = Duration.between(instant, Instant.now()).seconds
    val days = (seconds / 60 / 60 / 24).toInt()
    val weeks = days / 7
    val months = weeks / 4
    val years = months / 12

    return when {
        years >= 1 -> Pair(years.toString(), if (years == 1) "year" else "years")
        months >= 1 -> Pair(months.toString(), if (months == 1) "month" else "months")
        weeks >= 1 -> Pair(weeks.toString(), if (weeks == 1) "week" else "weeks")
        else -> Pair(days.toString(), if (days == 1) "day" else "days")
    }
}

fun Timestamp.calculatePostDuration(sold: Boolean): String {
    val instant = Instant.ofEpochSecond(seconds)
    val seconds = Duration.between(instant, Instant.now()).seconds
    val minutes = (seconds / 60).toInt()
    val hours = minutes / 60
    val days = hours / 24
    val weeks = days / 7
    val months = weeks / 4
    val years = months / 12

    val status = if (sold) "Sold" else "Posted"

    return when {
        years >= 1 -> if (years == 1) "$status $years year ago" else "$status $years years ago"
        months >= 1 -> if (months == 1) "$status $months month ago" else "$status $months months ago"
        weeks >= 1 -> if (weeks == 1) "$status $weeks week ago" else "$status $weeks weeks ago"
        days >= 1 -> if (days == 1) "$status $days day ago" else "$status $days days ago"
        hours >= 1 -> if (hours == 1) "$status $hours hour ago" else "$status $hours hours ago"
        minutes >= 1 -> if (minutes == 1) "$status $minutes minute ago" else "$status $minutes minutes ago"
        else -> if (seconds == 1L) "$status $seconds second ago" else "$status $seconds seconds ago"
    }
}

private fun Timestamp.calculateDate(
    timeFormat: DateTimeFormatter,
    shortDateFormat: DateTimeFormatter,
    longDateFormat: DateTimeFormatter
): String {
    val instant = Instant.ofEpochSecond(seconds)
    val date = LocalDateTime.ofInstant(instant, localZoneId)
    val seconds = Duration.between(date, LocalDateTime.now()).seconds
    val minutes = (seconds / 60).toInt()
    val hours = minutes / 60
    val days = hours / 24
    val weeks = days / 7
    val months = weeks / 4
    val years = months / 12

    return when {
        years >= 1 -> longDateFormat.format(date)
        months >= 1 || weeks >= 1 || days >= 1 -> shortDateFormat.format(date)
        else -> timeFormat.format(date)
    }
}

fun Timestamp.calculateChatDate(): String {
    val timeFormat = DateTimeFormatter.ofPattern("h:mm a")
    val shortDateFormat = DateTimeFormatter.ofPattern("MMM d")
    val longDateFormat = DateTimeFormatter.ofPattern("MMMM d, yyyy")

    return calculateDate(timeFormat, shortDateFormat, longDateFormat)
}

fun Timestamp.calculateMessageDate(): String {
    val timeFormat = DateTimeFormatter.ofPattern("h:mm a")
    val shortDateFormat = DateTimeFormatter.ofPattern("h:mm a, MMMM d")
    val longDateFormat = DateTimeFormatter.ofPattern("h:mm a, MMMM d, yyyy")

    return calculateDate(timeFormat, shortDateFormat, longDateFormat)
}

fun setCalendarConstraints(date: Long) =
    CalendarConstraints.Builder()
        .setOpenAt(date)
        .setEnd(LocalDateTime.now().minusYears(18).atZone(localZoneId).toInstant().toEpochMilli())
        .build()