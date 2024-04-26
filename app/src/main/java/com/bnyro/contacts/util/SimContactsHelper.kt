package com.bnyro.contacts.util

import android.content.Context
import android.net.Uri
import com.bnyro.contacts.domain.model.ContactData
import com.bnyro.contacts.domain.model.ValueWithType
import com.bnyro.contacts.util.extension.stringValue

object SimContactsHelper {
    fun getSimContacts(context: Context): List<ContactData> {
        val simUri = Uri.parse("content://icc/adn")
        val cursorSim = context.contentResolver.query(simUri, null, null, null, null) ?: return emptyList()
        val contacts = mutableListOf<ContactData>()

        while (cursorSim.moveToNext()) {
            val name = cursorSim.stringValue("name")
            val phoneNumber = cursorSim.stringValue("number")
                ?.replace("\\D", "")
                ?.replace("&", "")
            // skip empty sim contacts
            if (name.isNullOrBlank() && phoneNumber.isNullOrBlank()) continue

            val nameParts = ContactsHelper.splitFullName(name)
            val contact = ContactData(
                displayName = name,
                firstName = nameParts.first,
                surName = nameParts.second,
                alternativeName = "${nameParts.first} ${nameParts.second}",
                numbers = listOfNotNull(phoneNumber?.let { ValueWithType(it, 0) })
            )
            contacts.add(contact)
        }
        cursorSim.close()
        return contacts
    }
}
