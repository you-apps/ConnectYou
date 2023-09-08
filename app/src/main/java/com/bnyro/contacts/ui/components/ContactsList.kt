package com.bnyro.contacts.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.obj.FilterOptions
import com.bnyro.contacts.ui.components.modifier.scrollbar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactsList(
    contacts: List<ContactData>,
    filterOptions: FilterOptions,
    scrollConnection: NestedScrollConnection?,
    selectedContacts: MutableList<ContactData>
) {
    val state = rememberLazyListState()
    val contactGroups = remember(contacts) {
        contacts.asSequence().filter {
            !filterOptions.hiddenAccountNames.contains(it.accountName)
        }.filter {
            if (filterOptions.visibleGroups.isEmpty()) {
                true
            } else {
                filterOptions.visibleGroups.any { group ->
                    it.groups.contains(group)
                }
            }
        }.sortedBy {
            it.getNameBySortOrder(filterOptions.sortOder)
        }.groupBy {
            it.getNameBySortOrder(filterOptions.sortOder)?.firstOrNull()?.uppercase()
        }
    }
    LazyColumn(
        state = state,
        modifier = Modifier
            .padding(end = 5.dp)
            .scrollbar(state, false)
            .let { modifier ->
                scrollConnection?.let { modifier.nestedScroll(it) } ?: modifier
            }
    ) {
        contactGroups.forEach { (firstLetter, groupedContacts) ->
            stickyHeader {
                CharacterHeader(firstLetter.orEmpty())
            }
            items(groupedContacts) {
                ContactItem(
                    contact = it,
                    sortOrder = filterOptions.sortOder,
                    selected = selectedContacts.contains(it),
                    onSinglePress = {
                        if (selectedContacts.isEmpty()) {
                            false
                        } else {
                            if (selectedContacts.contains(it)) {
                                selectedContacts.remove(it)
                            } else {
                                selectedContacts.add(it)
                            }
                            true
                        }
                    },
                    onLongPress = {
                        if (!selectedContacts.contains(it)) selectedContacts.add(it)
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
