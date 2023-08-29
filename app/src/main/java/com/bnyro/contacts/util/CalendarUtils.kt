package com.bnyro.contacts.util

import android.annotation.SuppressLint
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar

object CalendarUtils {
    @SuppressLint("SimpleDateFormat")
    val isoDateFormat = SimpleDateFormat("yyyy-MM-dd")

    @SuppressLint("SimpleDateFormat")
    val isoDateFormatWithoutYear = SimpleDateFormat("MM-dd")

    @SuppressLint("SimpleDateFormat")
    val isoTimeFormat = SimpleDateFormat("yyyy-MM-dd-HH:mm:ss")
    private val localizedFormat get() = DateFormat.getDateInstance()

    fun millisToDate(milliSeconds: String, formatter: DateFormat = localizedFormat): String {
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds.toLong()
        return formatter.format(calendar.time)
    }

    fun dateToMillis(date: String) = isoDateFormat.parse(date)?.time

    fun localizeIsoDate(isoDate: String): String {
        val isDateWithoutYear = isoDate.startsWith('-')
        val newIsoDate = if (isDateWithoutYear) isoDate.replace("--", "") else isoDate
        val formatter = if (isDateWithoutYear) isoDateFormatWithoutYear else isoDateFormat

        val date = runCatching {
            formatter.parse(newIsoDate)
        }.getOrNull() ?: return newIsoDate
        return localizedFormat.format(date).let {
            if (isDateWithoutYear) {
                // Due to the limitations of the API we're getting 1970 when there's no year
                it.replace("1970", "")
            } else {
                it
            }
        }
    }

    fun getCurrentDateTime(): String {
        val calendar = Calendar.getInstance()
        return isoTimeFormat.format(calendar.time)
    }
}
