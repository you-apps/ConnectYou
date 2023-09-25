package com.bnyro.contacts.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.bnyro.contacts.ext.pmap
import com.bnyro.contacts.obj.ContactData
import com.bnyro.contacts.repo.ContactsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ExportHelper(
    private val context: Context,
    private val contactsRepository: ContactsRepository
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
                contactsRepository.createContact(contact)
            }
        }
    }

    fun exportContacts(uri: Uri, minimalContacts: List<ContactData>) {
        val contacts = minimalContacts.pmap { contactsRepository.loadAdvancedData(it) }
        val vCardText = VcardHelper.exportVcard(contacts)
        contentResolver.openOutputStream(uri)?.use {
            if (!encryptBackups) {
                it.write(vCardText.toByteArray())
            } else {
                ZipUtils.writeToEncryptedZip(password, vCardText.toByteArray(), it)
            }
        }
    }

    suspend fun exportTempContact(contacts: List<ContactData>): Uri {
        val vCardText = VcardHelper.exportVcard(contacts)
        val outputDir = File(context.cacheDir, "contacts").also {
            if (!it.exists()) it.mkdir()
        }
        val outFile = withContext(Dispatchers.IO) {
            var fileName: String
            if (contacts.size == 1) {
                fileName = contacts
                    .firstOrNull()
                    ?.displayName
                    ?.replace(" ", "_")
                    .orEmpty()

                // if the file name is too short, this can lead to crashes
                if (fileName.length < 10) fileName += "-$CONTACTS_EXPORT_FILE_SUFFIX"
            } else {
                fileName = CONTACTS_EXPORT_FILE_SUFFIX
            }
            File.createTempFile("${fileName}_", ".vcf", outputDir)
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

    companion object {
        private const val CONTACTS_EXPORT_FILE_SUFFIX = "contacts-export"
    }
}
