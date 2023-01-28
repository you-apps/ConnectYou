package com.bnyro.contacts.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.bnyro.contacts.ext.pmap
import com.bnyro.contacts.obj.ContactData
import java.io.File

class ExportHelper(private val context: Context) {
    private val contactsHelper = ContactsHelper(context)
    private val contentResolver = context.contentResolver

    @SuppressLint("MissingPermission")
    fun importContacts(uri: Uri) {
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
        Log.e("vCard", vCardText)
        contentResolver.openOutputStream(uri)?.use {
            it.write(vCardText.toByteArray())
        }
    }

    fun exportContact(minimalContact: ContactData): Uri {
        val contact = contactsHelper.loadAdvancedData(minimalContact)
        val vCardText = VcardHelper.exportVcard(listOf(contact))
        val outputDir = File(context.cacheDir, "contacts").also {
            if (!it.exists()) it.mkdir()
        }
        val outFile = File.createTempFile(contact.displayName.orEmpty(), ".vcf", outputDir)
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
