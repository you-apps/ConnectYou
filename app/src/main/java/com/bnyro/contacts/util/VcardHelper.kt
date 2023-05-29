package com.bnyro.contacts.util

import android.graphics.BitmapFactory
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.obj.ValueWithType
import ezvcard.Ezvcard
import ezvcard.VCard
import ezvcard.VCardVersion
import ezvcard.parameter.AddressType
import ezvcard.parameter.EmailType
import ezvcard.parameter.ImageType
import ezvcard.parameter.TelephoneType
import ezvcard.property.Address
import ezvcard.property.FormattedName
import ezvcard.property.Photo
import ezvcard.property.StructuredName

object VcardHelper {
    private val addressTypes = listOf(
        Pair(StructuredPostal.TYPE_HOME, AddressType.HOME),
        Pair(StructuredPostal.TYPE_WORK, AddressType.WORK),
        Pair(StructuredPostal.TYPE_OTHER, null)
    )

    private val emailTypes = listOf(
        Pair(Email.TYPE_HOME, EmailType.HOME),
        Pair(Email.TYPE_WORK, EmailType.WORK),
        Pair(Email.TYPE_MOBILE, EmailType.PREF),
        Pair(Email.TYPE_OTHER, null)
    )

    private val phoneNumberTypes = listOf(
        Pair(Phone.TYPE_MOBILE, TelephoneType.CELL),
        Pair(Phone.TYPE_HOME, TelephoneType.HOME),
        Pair(Phone.TYPE_WORK, TelephoneType.WORK),
        Pair(Phone.TYPE_MAIN, TelephoneType.PREF),
        Pair(Phone.TYPE_FAX_HOME, TelephoneType.FAX),
        Pair(Phone.TYPE_FAX_WORK, TelephoneType.FAX),
        Pair(Phone.TYPE_OTHER, null)
    )

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
                val type = phoneNumberTypes.firstOrNull {
                    it.first == number.type
                }?.second ?: TelephoneType.HOME
                runCatching {
                    addTelephoneNumber(number.value, type).also {
                        if (index == 0) it.types.add(TelephoneType.PREF)
                    }
                }
            }
            contact.emails.forEach { email ->
                val type = emailTypes.firstOrNull {
                    it.first == email.type
                }?.second ?: EmailType.HOME
                runCatching {
                    addEmail(email.value, type)
                }
            }
            contact.addresses.forEach { address ->
                val addressType = addressTypes.firstOrNull {
                    it.first == address.type
                }?.second?.value ?: AddressType.HOME.value
                runCatching {
                    val newAddress = Address().apply {
                        streetAddress = address.value
                        parameters.addType(addressType)
                    }
                    addAddress(newAddress)
                }
            }
            contact.notes.forEach { note ->
                runCatching {
                    addNote(note.value)
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
                        phoneNumberTypes.firstOrNull { pair ->
                            pair.second == number.types.firstOrNull()
                        }?.first
                    )
                },
                emails = it.emails.orEmpty().map { email ->
                    ValueWithType(
                        email.value,
                        emailTypes.firstOrNull { pair ->
                            pair.second == email.types.firstOrNull()
                        }?.first
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
                        addressTypes.firstOrNull { pair ->
                            pair.second == address.types.firstOrNull()
                        }?.first
                    )
                },
                notes = it.notes.orEmpty().map { note ->
                    ValueWithType(note.value, null)
                },
                photo = photo,
                thumbnail = photo
            )
        }
    }
}
