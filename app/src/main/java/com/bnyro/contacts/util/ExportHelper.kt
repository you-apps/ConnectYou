package com.bnyro.contacts.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.bnyro.contacts.ext.pmap
import com.bnyro.contacts.obj.ContactData
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExportHelper(
    private val context: Context,
    private val contactsHelper: ContactsHelper
) {
    private val contentResolver = context.contentResolver

    @SuppressLint("MissingPermission")
    suspend fun importContacts(uri: Uri) {
        contentResolver.openInputStream(uri)?.use {
            val content = it.bufferedReader().readText()
            val contacts = VcardHelper.importVcard(content)
            contacts.forEach { contact ->
                contactsHelper.createContact(contact)
            }
        }
    }

    fun exportContacts(uri: Uri, minimalContacts: List<ContactData>) {
        val contacts = minimalContacts.pmap { contactsHelper.loadAdvancedData(it) }
        val vCardText = VcardHelper.exportVcard(contacts)
        contentResolver.openOutputStream(uri)?.use {
            it.write(vCardText.toByteArray())
        }
    }

    suspend fun exportContact(minimalContact: ContactData, isFullContact: Boolean = false): Uri {
        val contact = if (isFullContact) minimalContact else contactsHelper.loadAdvancedData(
            minimalContact
        )
        val vCardText = VcardHelper.exportVcard(listOf(contact))
        val outputDir = File(context.cacheDir, "contacts").also {
            if (!it.exists()) it.mkdir()
        }
        val outFile = withContext(Dispatchers.IO) {
            val fileName = contact.displayName.orEmpty().ifEmpty { "contact-export" }
            File.createTempFile(fileName, ".vcf", outputDir)
        }
        contentResolver.openOutputStream(Uri.fromFile(outFile))?.use {
            it.write(vCardText.toByteArray())
        }
        return FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + ".provider",
            outFile
        )
    }
}
