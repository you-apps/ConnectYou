package com.bnyro.contacts.util

import android.annotation.SuppressLint
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

object CalendarUtils {
    @SuppressLint("SimpleDateFormat")
    val isoDateFormat = SimpleDateFormat("yyyy-MM-dd")

    @SuppressLint("SimpleDateFormat")
    val isoDateFormatWithoutDash = SimpleDateFormat("yyyyMMdd")

    @SuppressLint("SimpleDateFormat")
    val isoTimeFormat = SimpleDateFormat("HH:mm:ss")

    @SuppressLint("SimpleDateFormat")
    val isoDateFormatWithoutYear = SimpleDateFormat("MM-dd")

    @SuppressLint("SimpleDateFormat")
    val isoDateTimeFormat = SimpleDateFormat("yyyy-MM-dd-HH:mm:ss")
    private val localizedFormat get() = DateFormat.getDateInstance()

    fun millisToDate(milliSeconds: Long, formatter: DateFormat = localizedFormat): String {
        val calendar = Calendar.getInstance()
        calendar.clear(Calendar.ZONE_OFFSET)
        calendar.timeInMillis = milliSeconds

        return formatter.format(calendar.time)
    }

    private fun parseDateString(dateStr: String): Date? {
        val isDateWithoutYear = dateStr.startsWith("--")
        val newIsoDate = dateStr.removePrefix("--")
        val formatter = when {
            isDateWithoutYear -> isoDateFormatWithoutYear
            !dateStr.contains("-") -> isoDateFormatWithoutDash
            else -> isoDateFormat
        }

       val dateObject = runCatching {
            formatter.parse(newIsoDate)
        }.getOrNull() ?: return null

        val timeObject = GregorianCalendar().apply {
            this.time = dateObject
        }
        if (isDateWithoutYear) {
            val currentYear = GregorianCalendar.getInstance().get(Calendar.YEAR)
            timeObject.set(Calendar.YEAR, currentYear)
        }

        return timeObject.time
    }

    fun dateToMillis(date: String) = parseDateString(date)?.time

    fun localizeIsoDate(isoDate: String): String {
        val date = parseDateString(isoDate) ?: return isoDate

        return localizedFormat.format(date)
    }

    fun getCurrentDateTime(): String {
        val calendar = Calendar.getInstance()
        return isoDateTimeFormat.format(calendar.time)
    }

    fun getCurrentDateAndTime(): Pair<String, String> {
        val calendar = Calendar.getInstance()

        return isoDateFormat.format(calendar.time) to isoTimeFormat.format(calendar.time)
    }
}
