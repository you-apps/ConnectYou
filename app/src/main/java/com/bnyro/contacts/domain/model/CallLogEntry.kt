package com.bnyro.contacts.domain.model

import java.text.SimpleDateFormat
import java.util.Date

data class CallLogEntry(
    val phoneNumber: String,
    val type: Int,
    val time: Long,
    val duration: Long,
    val subscriptionId: String?
) {
    val dateString: String
    val timeString: String

    init {
        val date = Date(time)
        val dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.FULL)
        dateString = dateFormat.format(date)

        val timeFormat = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)
        timeString = timeFormat.format(date)
    }
}
