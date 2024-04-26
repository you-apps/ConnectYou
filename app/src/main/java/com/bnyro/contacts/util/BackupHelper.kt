package com.bnyro.contacts.util

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.bnyro.contacts.domain.repositories.ContactsRepository

object BackupHelper {
    val vCardMimeTypes = arrayOf("text/vcard", "text/x-vcard", "text/directory")
    val encryptBackups get() = Preferences.getBoolean(Preferences.encryptBackupsKey, false)
    val mimeType get() = if (encryptBackups) "application/zip" else "text/vcard"
    val openMimeTypes get() = if (encryptBackups) arrayOf("application/zip") else vCardMimeTypes
    val backupFileName get() = if (encryptBackups) "contacts.zip" else "contacts.vcf"

    suspend fun backup(context: Context, contactsRepository: ContactsRepository) {
        val backupDirPref = Preferences.getString(Preferences.backupDirKey, "").takeIf {
            it.orEmpty().isNotBlank()
        } ?: return

        val backupDir = DocumentFile.fromTreeUri(context, Uri.parse(backupDirPref)) ?: return
        val maxBackupAmount = Preferences.getString(Preferences.maxBackupAmountKey, "5")!!.toInt()

        val dateTime = CalendarUtils.getCurrentDateTime()

        val extension = if (encryptBackups) "zip" else "vcf"
        val fullName = "${contactsRepository.label.lowercase()}-backup-$dateTime.$extension"

        runCatching {
            backupDir.findFile(fullName)?.delete()
        }

        val file = backupDir.createFile(mimeType, fullName) ?: run {
            Log.e("BackupHelper", "error creating backup file")
            return
        }

        val contacts = contactsRepository.getContactList()
        val exportHelper = ExportHelper(context, contactsRepository)
        exportHelper.exportContacts(file.uri, contacts)

        // delete all the old backup files
        val backupFiles = backupDir.listFiles().filter {
            it.name.orEmpty().startsWith(contactsRepository.label.lowercase())
        }
        if (backupFiles.size <= maxBackupAmount) return

        backupFiles.sortedBy { it.name.orEmpty() }
            .take(backupFiles.size - maxBackupAmount)
            .forEach { it.delete() }
    }
}
