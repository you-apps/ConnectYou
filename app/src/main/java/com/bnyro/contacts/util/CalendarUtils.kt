package com.bnyro.contacts.util

import android.annotation.SuppressLint
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar

object CalendarUtils {
    @SuppressLint("SimpleDateFormat")
    val isoDateFormat = SimpleDateFormat("yyyy-MM-dd")

    @SuppressLint("SimpleDateFormat")
    val isoTimeFormat = SimpleDateFormat("yyyy-MM-dd-HH:mm:ss")
    private val localizedFormat: DateFormat = SimpleDateFormat.getInstance()

    fun millisToDate(milliSeconds: String, formatter: DateFormat = localizedFormat): String {
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds.toLong()
        return formatter.format(calendar.time)
    }

    fun dateToMillis(date: String) = isoDateFormat.parse(date)?.time

    fun localizeIsoDate(isoDate: String): String {
        val date = isoDateFormat.parse(isoDate) ?: return isoDate
        return localizedFormat.format(date).split(" ").firstOrNull() ?: isoDate
    }

    fun getCurrentDateTime(): String {
        val calendar = Calendar.getInstance()
        return isoTimeFormat.format(calendar.time)
    }
}
