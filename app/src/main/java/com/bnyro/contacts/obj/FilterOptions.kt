package com.bnyro.contacts.obj

import com.bnyro.contacts.enums.SortOrder
import com.bnyro.contacts.util.Preferences

data class FilterOptions(
    var sortOder: SortOrder,
    var hiddenAccountIdentifiers: List<String>,
    var visibleGroups: List<ContactsGroup>
) {
    companion object {
        fun default(): FilterOptions {
            val sortOrder = SortOrder.fromInt(Preferences.getInt(Preferences.sortOrderKey, 0))
            val hiddenAccounts = Preferences.getStringSet(Preferences.hiddenAccountsKey, emptySet())!!.toList()
            return FilterOptions(sortOrder, hiddenAccounts, listOf())
        }
    }
}
