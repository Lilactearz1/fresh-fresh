package com.movix.transak_infield.cloudB

import android.content.Context
import android.util.Log

import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class BackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        return try {

            val cloudService = CloudBackupService(applicationContext)

            val result = cloudService.backupToSupabase()

            result.onSuccess {
                Log.d("BACKUP", "Weekly backup successful")
            }.onFailure {
                Log.e("BACKUP", "Backup failed: ${it.message}")
            }

            Result.success()

        } catch (e: Exception) {

            Log.e("BACKUP", "Worker error: ${e.message}")
            Result.retry()

        }
    }
}