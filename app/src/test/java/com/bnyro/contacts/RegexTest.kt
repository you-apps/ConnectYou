package com.bnyro.contacts

import com.bnyro.contacts.util.emailRegex
import com.bnyro.contacts.util.linkRegex
import com.bnyro.contacts.util.phoneRegex
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegexTests {

    @Test
    fun linkRegex() {
        val regex = linkRegex

        // Valid Links
        assertTrue(regex.matches("www.google.com"))
        assertTrue(regex.matches("https://www.google.com"))
        assertTrue(regex.matches("http://www.example.com/path/to/file"))
        assertTrue(regex.matches("example.com/path/to/file"))
        assertTrue(regex.matches("http://www.example.com/path/to/file?t=123&v=1g"))
        assertTrue(regex.matches("example.com/path/to/file?t=1234&v=5"))
        assertTrue(regex.matches("github.com/you-apps/RecordYou"))

        // Invalid Links
        assertFalse(regex.matches("ftp.mozilla.org/pub/firefox/releases/"))
        assertFalse(regex.matches("ftp://ftp.mozilla.org/pub/firefox/releases/"))
    }

    @Test
    fun emailRegex() {
        val regex = emailRegex

        // Valid E-mails
        assertTrue(regex.matches("john.doe@example.com"))
        assertTrue(regex.matches("john.doe@is-a.dev"))
        assertTrue(regex.matches("jane_doe@example.net"))
        assertTrue(regex.matches("test+test@example.org"))

        // Invalid E-mails
        assertFalse(regex.matches("johndoe@example"))
        assertFalse(regex.matches("janedoe@example."))
        assertFalse(regex.matches("test@example"))
    }

    @Test
    fun phoneRegex() {
        val regex = phoneRegex

        // Valid Phone Numbers
        assertTrue(regex.matches("+1 555 555 5555"))
        assertTrue(regex.matches("5555555555"))
        assertTrue(regex.matches("(555) 555-5555"))
        assertTrue(regex.matches("555-555-5555"))

        // Invalid Phone Numbers
        assertFalse(regex.matches("123 456 789"))
        assertFalse(regex.matches("+123 456 7890"))
    }
}