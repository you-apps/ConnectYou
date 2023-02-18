package com.bnyro.contacts.obj

import com.bnyro.contacts.enums.SortOrder
import com.bnyro.contacts.util.Preferences

data class FilterOptions(
    var sortOder: SortOrder,
    var hiddenAccountNames: List<String>
) {
    companion object {
        fun default(): FilterOptions {
            val sortOrder = SortOrder.fromInt(Preferences.getInt(Preferences.sortOrder, 0))
            return FilterOptions(sortOrder, listOf())
        }
    }
}
