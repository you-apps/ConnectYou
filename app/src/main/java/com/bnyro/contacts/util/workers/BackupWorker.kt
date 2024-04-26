package com.bnyro.contacts.util.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.bnyro.contacts.App
import com.bnyro.contacts.domain.enums.BackupType
import com.bnyro.contacts.util.BackupHelper
import com.bnyro.contacts.util.Preferences
import java.util.concurrent.TimeUnit

class BackupWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as App
        val contactHelpers = when (Preferences.getBackupType()) {
            BackupType.BOTH -> listOf(
                app.deviceContactsRepository,
                app.localContactsRepository
            )

            BackupType.DEVICE -> listOf(app.deviceContactsRepository)
            BackupType.LOCAL -> listOf(app.localContactsRepository)
            else -> return Result.success()
        }
        contactHelpers.forEach {
            BackupHelper.backup(applicationContext, it)
        }
        return Result.success()
    }

    companion object {
        private const val workerName = "BackupWorker"

        fun enqueue(context: Context, force: Boolean = false) {
            val delayInHours = Preferences.getString(Preferences.backupIntervalKey, "12")
                .orEmpty().toLong()
            val request = PeriodicWorkRequestBuilder<BackupWorker>(delayInHours, TimeUnit.HOURS)
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    workerName,
                    if (force) ExistingPeriodicWorkPolicy.UPDATE else ExistingPeriodicWorkPolicy.KEEP,
                    request.build()
                )
        }
    }
}
