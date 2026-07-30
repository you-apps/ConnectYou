package com.bnyro.contacts.domain.model

import com.bnyro.contacts.domain.model.AccountType.Companion.fromAccountColumns
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.Assert.assertEquals

class AccountTypeTest {

    @Test
    fun deviceAccountFromNullColumns() {
        assertEquals(
            DeviceAccountType,
            fromAccountColumns(null, null)
        )
    }

    @Test
    fun deviceAccountFromLegacyAccount() {
        assertEquals(
            DeviceAccountType,
            fromAccountColumns("DEVICE", "com.android.contacts")
        )
    }

    @Test
    fun realAccountFromRealColumns() {
        assertEquals(
            RealAccountType("someone@example.com", "example.com"),
            fromAccountColumns("someone@example.com", "example.com")
        )
    }

    @Test
    fun deviceAccountToColumns() {
        assertEquals(
            AccountType.AccountColumns(null, null),
            DeviceAccountType.toAccountColumns()
        )
    }

    @Test
    fun realAccountToColumns() {
        assertEquals(
            AccountType.AccountColumns("someone@example.com", "example.com"),
            RealAccountType("someone@example.com", "example.com").toAccountColumns()
        )
    }

    @Test
    fun deviceAccountToPreferencesAndBack() {
        assertEquals(
            DeviceAccountType,
            AccountType.fromPreferencesString(DeviceAccountType.toPreferencesString())
        )
    }

    @Test
    fun realAccountToPreferencesAndBack() {
        val account = RealAccountType("someone@example.com", "example.com")
        assertEquals(
            account,
            AccountType.fromPreferencesString(account.toPreferencesString())
        )
    }

    @Test
    fun accountFromInvalidPreferencesString() {
        assertEquals(
            null,
            AccountType.fromPreferencesString("invalid.no.pipe.symbol")
        )
    }

    @Test
    fun accountFromLegacyPreferencesString() {
        assertEquals(
            DeviceAccountType,
            AccountType.fromPreferencesString("com.android.contacts|DEVICE")
        )
    }

}

