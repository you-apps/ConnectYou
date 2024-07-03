package com.bnyro.contacts.presentation.screens.contacts.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.bnyro.contacts.domain.model.ContactData
import com.bnyro.contacts.domain.model.FilterOptions
import com.bnyro.contacts.presentation.components.CharacterHeader
import my.nanihadesuka.compose.LazyColumnScrollbar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactsList(
    contacts: List<ContactData>,
    filterOptions: FilterOptions,
    scrollConnection: NestedScrollConnection?,
    selectedContacts: MutableList<ContactData>
) {
    val state = rememberLazyListState()
    val contactGroups = remember(contacts, filterOptions) {
        contacts.asSequence().filter {
            !filterOptions.hiddenAccountIdentifiers.contains(it.accountIdentifier)
        }.filter {
            filterOptions.visibleGroups.isEmpty() || filterOptions.visibleGroups.any { group ->
                it.groups.contains(group)
            }
        }.filter {
            !filterOptions.favoritesOnly || it.favorite
        }.sortedBy {
            it.getNameBySortOrder(filterOptions.sortOder)
        }.groupBy {
            it.getNameBySortOrder(filterOptions.sortOder)?.firstOrNull()?.uppercase()
        }
    }
    LazyColumnScrollbar(
        listState = state,
        thumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
        thumbSelectedColor = MaterialTheme.colorScheme.primary,
        thickness = 8.dp
    ) {
        LazyColumn(
            state = state,
            modifier = Modifier
                .padding(end = 5.dp)
                .let { modifier ->
                    scrollConnection?.let { modifier.nestedScroll(it) } ?: modifier
                }
        ) {
            contactGroups.forEach { (firstLetter, groupedContacts) ->
                stickyHeader {
                    CharacterHeader(firstLetter.orEmpty())
                }
                items(groupedContacts, key = ContactData::contactId) {
                    ContactItem(
                        modifier = Modifier.padding(horizontal = 10.dp),
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
}
