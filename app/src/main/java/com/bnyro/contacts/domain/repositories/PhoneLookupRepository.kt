package com.bnyro.contacts.domain.repositories

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.PhoneLookup
import androidx.core.net.toUri
import com.bnyro.contacts.domain.model.BasicContactData
import com.bnyro.contacts.util.extension.stringValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PhoneLookupRepository(private val context: Context) {
    /**
     * Get all contacts with a number equal to or containing a given number.
     * @param number The number to search for
     * @return A list containing simple contact data
     */
    suspend fun getContactsWithNumber(number: String): List<BasicContactData> =
        withContext(Dispatchers.IO) {
            val contacts = mutableListOf<BasicContactData>()
            val uri = Phone.CONTENT_URI
            val projection = arrayOf(
                Phone.PHOTO_THUMBNAIL_URI,
                Phone.DISPLAY_NAME,
                Phone.NUMBER
            )
            val selection = "${Phone.NUMBER} LIKE ?"
            val selectionArgs = arrayOf("%$number%")

            val cursor =
                context.contentResolver.query(uri, projection, selection, selectionArgs, null)

            cursor?.use {
                while (it.moveToNext()) {
                    val name = it.stringValue(Phone.DISPLAY_NAME)
                    val phoneNumber = it.stringValue(Phone.NUMBER)
                    val photoUri = it.stringValue(Phone.PHOTO_THUMBNAIL_URI)?.toUri()

                    if (phoneNumber != null) {
                        contacts.add(
                            BasicContactData(
                                number = phoneNumber,
                                name = name,
                                thumbnail = photoUri
                            )
                        )
                    }
                }
            }

            contacts
        }

    /**
     * Get all contacts with a name containing the given query
     * @param nameQuery The name to search for
     * @return A list containing simple contact data
     */
    suspend fun getContactsWithName(nameQuery: String): List<BasicContactData> =
        withContext(Dispatchers.IO) {
            val contacts = mutableListOf<BasicContactData>()
            val uri = Phone.CONTENT_URI
            val projection = arrayOf(
                Phone.PHOTO_THUMBNAIL_URI,
                Phone.DISPLAY_NAME,
                Phone.NUMBER
            )
            val selection = "${Phone.DISPLAY_NAME} LIKE ?"
            val selectionArgs = arrayOf("%$nameQuery%")

            val cursor =
                context.contentResolver.query(uri, projection, selection, selectionArgs, null)

            cursor?.use {
                while (it.moveToNext()) {
                    val name = it.stringValue(Phone.DISPLAY_NAME)
                    val phoneNumber = it.stringValue(Phone.NUMBER)
                    val photoUri = it.stringValue(Phone.PHOTO_THUMBNAIL_URI)?.toUri()

                    if (phoneNumber != null) {
                        contacts.add(
                            BasicContactData(
                                number = phoneNumber,
                                name = name,
                                thumbnail = photoUri
                            )
                        )
                    }
                }
            }

            contacts
        }

    /**Get caller id from a number
     * @param phoneNumber The number of the caller
     * @return A simple contact data containing name phone and thumbnail
     */
    suspend fun getContactByNumber(phoneNumber: String): BasicContactData =
        withContext(Dispatchers.IO) {
            val uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
            val query =
                context.contentResolver.query(
                    uri,
                    arrayOf(PhoneLookup.DISPLAY_NAME, PhoneLookup.PHOTO_THUMBNAIL_URI),
                    null,
                    null,
                    null
                )

            query?.use { cursor ->
                if (cursor.moveToFirst()) {
                    return@withContext BasicContactData(
                        number = phoneNumber,
                        name = cursor.stringValue(
                            PhoneLookup.DISPLAY_NAME
                        ),
                        thumbnail = cursor.stringValue(PhoneLookup.PHOTO_THUMBNAIL_URI)?.toUri()
                    )
                }
            }
            BasicContactData(
                number = phoneNumber
            )
        }
}