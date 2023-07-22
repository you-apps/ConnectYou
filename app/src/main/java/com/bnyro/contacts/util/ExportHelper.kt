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
    private val encryptBackups get() = Preferences.getBoolean(Preferences.encryptBackupsKey, false)
    private val password get() = Preferences.getString(Preferences.encryptBackupPasswordKey, "").orEmpty()

    @SuppressLint("MissingPermission")
    suspend fun importContacts(uri: Uri) {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            val input = if (!encryptBackups) {
                inputStream
            } else {
                ZipUtils.getPlainInputStream(password, inputStream)
            }
            val content = input.bufferedReader().readText()
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
            if (!encryptBackups) {
                it.write(vCardText.toByteArray())
            } else {
                ZipUtils.writeToEncryptedZip(password, vCardText.toByteArray(), it)
            }
        }
    }

    suspend fun exportTempContact(minimalContact: ContactData, isFullContact: Boolean = false): Uri {
        val contact = if (isFullContact) {
            minimalContact
        } else contactsHelper.loadAdvancedData(
            minimalContact
        )
        val vCardText = VcardHelper.exportVcard(listOf(contact))
        val outputDir = File(context.cacheDir, "contacts").also {
            if (!it.exists()) it.mkdir()
        }
        val outFile = withContext(Dispatchers.IO) {
            val contactName = contact.displayName.orEmpty().replace(" ", "_")
            File.createTempFile("$contactName-contact-export", ".vcf", outputDir)
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
