package com.bnyro.contacts.util

import android.graphics.BitmapFactory
import android.provider.ContactsContract
import com.bnyro.contacts.domain.model.ContactData
import com.bnyro.contacts.domain.model.ValueWithType
import ezvcard.Ezvcard
import ezvcard.VCard
import ezvcard.VCardVersion
import ezvcard.parameter.AddressType
import ezvcard.parameter.EmailType
import ezvcard.parameter.ImageType
import ezvcard.parameter.TelephoneType
import ezvcard.property.Address
import ezvcard.property.Anniversary
import ezvcard.property.Birthday
import ezvcard.property.FormattedName
import ezvcard.property.Photo
import ezvcard.property.StructuredName
import ezvcard.util.PartialDate

object VcardHelper {
    fun exportVcard(contacts: List<ContactData>): String {
        val vCards = contacts.map { createVcardContact(it) }

        return Ezvcard.write(vCards).version(VCardVersion.V4_0).go()
    }

    private fun createVcardContact(contact: ContactData): VCard {
        return VCard().apply {
            formattedName = FormattedName(contact.displayName)
            structuredName = StructuredName().apply {
                given = contact.firstName
                family = contact.surName
            }
            contact.nickName?.let {
                setNickname(it)
            }
            contact.organization?.let {
                setOrganization(it)
            }
            contact.numbers.forEachIndexed { index, number ->
                val type = ContactsHelper.phoneNumberTypes.firstOrNull {
                    it.id == number.type
                }?.vcardType as? TelephoneType ?: TelephoneType.HOME
                runCatching {
                    addTelephoneNumber(number.value, type).also {
                        if (index == 0) it.types.add(TelephoneType.PREF)
                    }
                }
            }
            contact.emails.forEach { email ->
                val type = ContactsHelper.emailTypes.firstOrNull {
                    it.id == email.type
                }?.vcardType as? EmailType ?: EmailType.HOME
                runCatching {
                    addEmail(email.value, type)
                }
            }
            contact.addresses.forEach { address ->
                val addressType = ContactsHelper.addressTypes.firstOrNull {
                    it.id == address.type
                }?.vcardType as? AddressType ?: AddressType.HOME
                runCatching {
                    val newAddress = Address().apply {
                        streetAddress = address.value
                        parameters.addType(addressType?.value)
                    }
                    addAddress(newAddress)
                }
            }
            contact.notes.forEach { note ->
                runCatching {
                    addNote(note.value)
                }
            }
            contact.events
                .filter { it.value.isNotBlank() }
                .forEach { event ->
                    when (event.type) {
                        ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY -> {
                            birthday = Birthday(PartialDate.parse(event.value))
                        }

                        ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY -> {
                            anniversary = Anniversary(PartialDate.parse(event.value))
                        }
                    }
                }
            (contact.photo ?: contact.thumbnail)?.let {
                val photo = Photo(ImageHelper.bitmapToByteArray(it), ImageType.PNG)
                addPhoto(photo)
            }
        }
    }

    fun importVcard(vCardString: String): List<ContactData> {
        val vCard = Ezvcard.parse(vCardString)

        return vCard.all().map {
            val photo = it.photos.firstOrNull()?.data?.let { photoBytes ->
                BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.size)
            }
            ContactData(
                displayName = it.formattedName?.value,
                firstName = it.structuredName?.given,
                surName = it.structuredName?.family,
                nickName = it.nickname?.values?.firstOrNull(),
                organization = it.organization?.values?.firstOrNull(),
                numbers = it.telephoneNumbers.orEmpty().sortedBy { tel ->
                    // rank the number labeled with pref as the first one
                    if (tel.types.contains(TelephoneType.PREF)) -1 else 1
                }.map { number ->
                    ValueWithType(
                        number.text,
                        ContactsHelper.phoneNumberTypes.firstOrNull { type ->
                            type.vcardType == number.types.firstOrNull()
                        }?.id
                    )
                },
                emails = it.emails.orEmpty().map { email ->
                    ValueWithType(
                        email.value,
                        ContactsHelper.emailTypes.firstOrNull { type ->
                            type.vcardType == email.types.firstOrNull()
                        }?.id
                    )
                },
                addresses = it.addresses.orEmpty().map { address ->
                    ValueWithType(
                        listOfNotNull(
                            address.streetAddress,
                            address.locality,
                            address.region,
                            address.postalCode,
                            address.country
                        ).filter { entry -> entry.isNotBlank() }.joinToString(" ").trim(),
                        ContactsHelper.addressTypes.firstOrNull { type ->
                            type.vcardType == address.types.firstOrNull()
                        }?.id
                    )
                },
                notes = it.notes.orEmpty().map { note ->
                    ValueWithType(note.value, null)
                },
                events = it.anniversaries.map { anniversary ->
                    ValueWithType(
                        CalendarUtils.millisToDate(
                            anniversary.date.time,
                            formatter = CalendarUtils.isoDateFormat
                        ),
                        ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY
                    )
                } + it.birthdays.map { birthday ->
                    ValueWithType(
                        CalendarUtils.millisToDate(
                            birthday.date.time,
                            formatter = CalendarUtils.isoDateFormat
                        ),
                        ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY
                    )
                },
                photo = photo,
                thumbnail = photo
            )
        }
    }
}
