package com.bnyro.contacts.util

import android.content.Context
import android.text.format.DateUtils

object TextUtils {
    fun formatDateTimestamp(context: Context, timestampMillis: Long): String {
        return DateUtils.getRelativeDateTimeString(
            context,
            timestampMillis,
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.WEEK_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_ALL
        ).split(", ").first()
    }
}
