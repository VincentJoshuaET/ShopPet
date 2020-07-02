package com.vt.shoppet.util

import com.google.firebase.Timestamp
import java.time.*
import java.time.format.DateTimeFormatter

val zone: ZoneId = ZoneId.of("Asia/Manila")

fun Timestamp.calculateAge(): String {
    val date = LocalDateTime.ofInstant(toDate().toInstant(), zone)
    val seconds = Duration.between(date, LocalDateTime.now()).seconds
    val days = (seconds / 60 / 60 / 24).toInt()
    val weeks = days / 7
    val months = weeks / 4
    val years = months / 12

    return when {
        years >= 1 -> if (years == 1) "$years year old" else "$years years old"
        months >= 1 -> if (months == 1) "$months month old" else "$months months old"
        weeks >= 1 -> if (weeks == 1) "$weeks week old" else "$weeks weeks old"
        else -> if (days == 1) "$days yea old" else "$days days old"
    }
}

fun Timestamp.calculatePostDuration(sold: Boolean): String {
    val date = LocalDateTime.ofInstant(toDate().toInstant(), zone)
    val seconds = Duration.between(date, LocalDateTime.now()).seconds
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

fun Timestamp.calculateChatDuration(): String? {

    val timeFormat = DateTimeFormatter.ofPattern("h:mm a")
    val shortDateFormat = DateTimeFormatter.ofPattern("MMM d")
    val longDateFormat = DateTimeFormatter.ofPattern("MMMM d, yyyy")

    val date = LocalDateTime.ofInstant(toDate().toInstant(), zone)
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