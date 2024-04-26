package com.bnyro.contacts.presentation.screens.contacts.model.state

import com.bnyro.contacts.domain.model.ContactData

sealed interface ContactListState {
    object Loading : ContactListState
    data class Success(var contacts: List<ContactData>) : ContactListState
    object Error : ContactListState
    object Empty : ContactListState
}
