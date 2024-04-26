package com.bnyro.contacts.domain.enums

import android.provider.ContactsContract
import android.provider.ContactsContract.Intents
import com.bnyro.contacts.R
import com.bnyro.contacts.domain.model.ContactData
import com.bnyro.contacts.domain.model.TranslatedType
import com.bnyro.contacts.domain.model.ValueWithType
import com.bnyro.contacts.util.CalendarUtils
import com.bnyro.contacts.util.ContactsHelper

abstract class ContactAttribute<T> {
    abstract val stringRes: Int
    abstract val androidValueColumn: String
    abstract val androidContentType: String
    abstract fun set(contact: ContactData, value: T)
    abstract fun get(contact: ContactData): T
    open fun display(contact: ContactData): T = get(contact)
}

abstract class StringAttribute : ContactAttribute<String?>() {
    abstract val insertKey: String?
}

abstract class ListAttribute: ContactAttribute<List<ValueWithType>>() {
    abstract val androidTypeColumn: String
    abstract val types: List<TranslatedType>
    abstract val insertKeys: List<Pair<String, String?>>
    open val intentActionType: IntentActionType? = null
}

class Organization : StringAttribute() {
    override val stringRes: Int = R.string.organization
    override val insertKey: String = Intents.Insert.COMPANY
    override val androidValueColumn: String = ContactsContract.CommonDataKinds.Organization.COMPANY
    override val androidContentType: String = ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE

    override fun set(contact: ContactData, value: String?) {
        contact.organization = value
    }

    override fun get(contact: ContactData) = contact.organization
}

class Title : StringAttribute() {
    override val stringRes: Int = R.string.title
    override val insertKey: String = Intents.Insert.JOB_TITLE
    override val androidValueColumn: String = ContactsContract.CommonDataKinds.Organization.TITLE
    override val androidContentType: String = ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE

    override fun set(contact: ContactData, value: String?) {
        contact.organization = value
    }

    override fun get(contact: ContactData) = contact.organization
}

class Nickname : StringAttribute() {
    override val stringRes: Int = R.string.nick_name
    override val insertKey: String? = null
    override val androidValueColumn: String = ContactsContract.CommonDataKinds.Nickname.NAME
    override val androidContentType: String = ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE

    override fun set(contact: ContactData, value: String?) {
        contact.nickName = value
    }

    override fun get(contact: ContactData) = contact.nickName
}

class Events : ListAttribute() {
    override val stringRes: Int = R.string.event
    override val insertKeys: List<Pair<String, String?>> = emptyList()
    override val androidValueColumn: String = ContactsContract.CommonDataKinds.Event.START_DATE
    override val androidTypeColumn: String = ContactsContract.CommonDataKinds.Event.TYPE
    override val androidContentType: String = ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE

    override fun set(contact: ContactData, value: List<ValueWithType>) {
        contact.events = value
    }

    override fun get(contact: ContactData) = contact.events

    override fun display(contact: ContactData) = super.display(contact).map {
        it.copy(value = CalendarUtils.localizeIsoDate(it.value))
    }

    override val types: List<TranslatedType> = ContactsHelper.eventTypes
}

class Numbers : ListAttribute() {
    override val stringRes: Int = R.string.phone_number
    override val insertKeys: List<Pair<String, String?>> = listOf(
        Intents.Insert.PHONE to Intents.Insert.PHONE_TYPE,
        Intents.Insert.SECONDARY_PHONE to Intents.Insert.SECONDARY_PHONE,
        Intents.Insert.TERTIARY_EMAIL to Intents.Insert.TERTIARY_EMAIL_TYPE
    )
    override val androidValueColumn: String = ContactsContract.CommonDataKinds.Phone.NUMBER
    override val androidTypeColumn: String = ContactsContract.CommonDataKinds.Phone.TYPE
    override val androidContentType: String = ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
    override val intentActionType = IntentActionType.DIAL

    override fun set(contact: ContactData, value: List<ValueWithType>) {
        contact.numbers = value
    }

    override fun get(contact: ContactData) = contact.numbers
    override fun display(contact: ContactData) = super.display(contact).map {
        ValueWithType(ContactsHelper.normalizePhoneNumber(it.value), it.type)
    }

    override val types: List<TranslatedType> = ContactsHelper.phoneNumberTypes
}

class Emails : ListAttribute() {
    override val stringRes: Int = R.string.email
    override val insertKeys: List<Pair<String, String?>> = listOf(
        Intents.Insert.EMAIL to Intents.Insert.EMAIL_TYPE,
        Intents.Insert.SECONDARY_EMAIL to Intents.Insert.SECONDARY_EMAIL_TYPE,
        Intents.Insert.TERTIARY_EMAIL to Intents.Insert.TERTIARY_EMAIL_TYPE
    )
    override val androidValueColumn: String = ContactsContract.CommonDataKinds.Email.ADDRESS
    override val androidTypeColumn: String = ContactsContract.CommonDataKinds.Email.TYPE
    override val androidContentType: String = ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
    override val intentActionType = IntentActionType.EMAIL

    override fun set(contact: ContactData, value: List<ValueWithType>) {
        contact.emails = value
    }

    override fun get(contact: ContactData) = contact.emails

    override val types: List<TranslatedType> = ContactsHelper.emailTypes
}

class Addresses : ListAttribute() {
    override val stringRes: Int = R.string.address
    override val insertKeys: List<Pair<String, String?>> = listOf(Intents.Insert.POSTAL to null)
    override val androidValueColumn: String = ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS
    override val androidTypeColumn: String = ContactsContract.CommonDataKinds.StructuredPostal.TYPE
    override val androidContentType: String = ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
    override val intentActionType = IntentActionType.ADDRESS

    override fun set(contact: ContactData, value: List<ValueWithType>) {
        contact.addresses = value
    }

    override fun get(contact: ContactData) = contact.addresses

    override val types: List<TranslatedType> = ContactsHelper.addressTypes
}

class Notes : ListAttribute() {
    override val stringRes: Int = R.string.note
    override val insertKeys: List<Pair<String, String?>> = listOf(Intents.Insert.NOTES to null)
    override val androidValueColumn: String = ContactsContract.CommonDataKinds.Note.NOTE
    override val androidTypeColumn: String = ContactsContract.CommonDataKinds.Note.DATA2
    override val androidContentType: String = ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE
    override fun set(contact: ContactData, value: List<ValueWithType>) {
        contact.notes = value
    }

    override fun get(contact: ContactData) = contact.notes

    override val types: List<TranslatedType> = emptyList()
}

class Websites : ListAttribute() {
    override val stringRes: Int = R.string.website
    override val insertKeys: List<Pair<String, String?>> = emptyList()
    override val androidValueColumn: String = ContactsContract.CommonDataKinds.Website.URL
    override val androidTypeColumn: String = ContactsContract.CommonDataKinds.Website.TYPE
    override val androidContentType: String = ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE
    override val intentActionType = IntentActionType.WEBSITE

    override fun set(contact: ContactData, value: List<ValueWithType>) {
        contact.websites = value
    }

    override fun get(contact: ContactData) = contact.websites

    override val types: List<TranslatedType> = ContactsHelper.websiteTypes
}
