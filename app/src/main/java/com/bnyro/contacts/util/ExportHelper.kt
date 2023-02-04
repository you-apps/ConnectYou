package com.bnyro.contacts.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.bnyro.contacts.obj.ContactData
import java.io.File

class ExportHelper(private val context: Context) {
    private val contactsHelper = DeviceContactsHelper(context)
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

    fun exportContacts(uri: Uri, contacts: List<ContactData>) {
        val vCardText = VcardHelper.exportVcard(contacts)
        contentResolver.openOutputStream(uri)?.use {
            it.write(vCardText.toByteArray())
        }
    }

    fun exportContact(contact: ContactData): Uri {
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
