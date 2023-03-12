package com.bnyro.contacts

import com.bnyro.contacts.util.CalendarUtils
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun dateConversion() {
        val date = "2023-05-09"
        val str = CalendarUtils.dateToMillis(date)
        assertEquals(date, CalendarUtils.millisToDate(str.toString(), CalendarUtils.isoDateFormat))
    }
}
