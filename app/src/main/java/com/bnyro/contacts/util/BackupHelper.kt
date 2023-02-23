package com.bnyro.contacts.util

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile

object BackupHelper {
    private const val VCARD_MIME = "text/v-card"

    suspend fun backup(context: Context, contactsHelper: ContactsHelper) {
        val backupDirPref = Preferences.getString(Preferences.backupDirKey, "").takeIf {
            it.orEmpty().isNotBlank()
        } ?: return

        val backupDir = DocumentFile.fromTreeUri(context, Uri.parse(backupDirPref)) ?: return
        val maxBackupAmount = Preferences.getString(Preferences.maxBackupAmountKey, "5")!!.toInt()

        val dateTime = CalendarUtils.getCurrentDateTime()

        val fullName = "${contactsHelper.label.lowercase()}-backup-$dateTime.vcf"

        runCatching {
            backupDir.findFile(fullName)?.delete()
        }

        val file = backupDir.createFile(VCARD_MIME, fullName) ?: run {
            Log.e("BackupHelper", "error creating backup file")
            return
        }

        val contacts = contactsHelper.getContactList()
        val exportHelper = ExportHelper(context, contactsHelper)
        exportHelper.exportContacts(file.uri, contacts)

        // delete all the old backup files
        val backupFiles = backupDir.listFiles().filter {
            it.name.orEmpty().startsWith(contactsHelper.label.lowercase())
        }
        if (backupFiles.size <= maxBackupAmount) return

        backupFiles.sortedBy { it.name.orEmpty() }
            .take(backupFiles.size - maxBackupAmount)
            .forEach { it.delete() }
    }
}
