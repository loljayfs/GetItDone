package com.loljay.getitdone

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// The Material date picker returns midnight UTC, so due dates must be formatted in UTC
// or they display as the previous day in timezones behind UTC.
fun formatDueDate(utcMillis: Long): String =
    SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
        .apply { timeZone = TimeZone.getTimeZone("UTC") }
        .format(Date(utcMillis))

fun formatLocalDate(millis: Long): String =
    SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault()).format(Date(millis))

fun todayAsUtcMillis(): Long {
    val local = Calendar.getInstance()
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(local[Calendar.YEAR], local[Calendar.MONTH], local[Calendar.DAY_OF_MONTH])
    }.timeInMillis
}

fun isOverdue(utcDueMillis: Long): Boolean = utcDueMillis < todayAsUtcMillis()
