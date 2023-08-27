package com.bnyro.contacts.ui.models.state

import com.bnyro.contacts.obj.ContactData

sealed interface ContactListState {
    object Loading : ContactListState
    data class Success(var contacts: List<ContactData>) : ContactListState
    object Error : ContactListState
    object Empty : ContactListState
}
