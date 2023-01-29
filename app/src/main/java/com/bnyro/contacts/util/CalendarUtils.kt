package com.bnyro.contacts.util

import android.annotation.SuppressLint
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar

object CalendarUtils {
    @SuppressLint("SimpleDateFormat")
    val isoDateFormat = SimpleDateFormat("yyyy-MM-dd")
    private val localizedFormat: DateFormat = SimpleDateFormat.getInstance()

    fun formatMillisToDate(milliSeconds: String, formatter: DateFormat = localizedFormat): String {
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds.toLong()
        return formatter.format(calendar.time)
    }

    fun dateToMillis(date: String) = isoDateFormat.parse(date)?.time
}
