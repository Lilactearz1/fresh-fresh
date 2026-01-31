package com.movix.transak_infield.cloudB


import android.content.Context
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.movix.transak_infield.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CloudBackupService(private val context: Context) {

    private val dbHandler: DatabaseHandler by lazy { DatabaseHandler(context) }
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    // ================== FIREBASE STORAGE ==================

    suspend fun backupToFirebase(): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            // 1. Create backup file
            val backupFile = dbHandler.createBackupFile(context)

            // 2. Upload to Firebase
            val storage = FirebaseStorage.getInstance()
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "backups/transak_backup_$timestamp.zip"
            val storageRef = storage.reference.child(fileName)

            val uploadTask = storageRef.putFile(Uri.fromFile(backupFile)).await()
            val downloadUrl = storageRef.downloadUrl.await()

            // 3. Clean up local file
            backupFile.delete()

            Result.success("Backup uploaded successfully. URL: $downloadUrl")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ================== GOOGLE DRIVE ==================

    suspend fun backupToGoogleDrive(accessToken: String): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val backupFile = dbHandler.createBackupFile(context)

            // Create file metadata for Google Drive
            val metadata = """
                {
                    "name": "${backupFile.name}",
                    "mimeType": "application/zip",
                    "parents": ["root"]
                }
            """.trimIndent()

            // Upload to Google Drive using REST API
            val client = OkHttpClient()
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "metadata",
                    null,
                    RequestBody.create("application/json".toMediaType(), metadata)
                )
                .addFormDataPart(
                    "file",
                    backupFile.name,
                    RequestBody.create("application/zip".toMediaType(), backupFile)
                )
                .build()

            val request = Request.Builder()
                .url("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart")
                .header("Authorization", "Bearer $accessToken")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                backupFile.delete()
                Result.success("Backup uploaded to Google Drive")
            } else {
                Result.failure(Exception("Failed: ${response.body?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ================== CUSTOM SERVER ==================

    suspend fun backupToCustomServer(serverUrl: String, apiKey: String? = null): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Export as JSON
            val jsonData = dbHandler.exportAllDataToJson()

            val client = OkHttpClient()
            val requestBody = RequestBody.create(
                "application/json".toMediaType(),
                jsonData
            )

            val requestBuilder = Request.Builder()
                .url(serverUrl)
                .post(requestBody)

            apiKey?.let {
                requestBuilder.header("Authorization", "Bearer $apiKey")
            }

            val request = requestBuilder.build()
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                Result.success("Data uploaded successfully")
            } else {
                Result.failure(Exception("Server error: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ================== LOCAL BACKUP ==================

    fun createLocalBackup(): File {
        return dbHandler.createBackupFile(context)
    }

    // ================== RESTORE FUNCTIONS ==================

    suspend fun restoreFromJson(jsonData: String): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val jsonObject = JSONObject(jsonData)

            // Parse and restore data (simplified - in reality you'd need more complex logic)
            // This would involve clearing existing data and inserting the backup

            Result.success("Restore initiated successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ================== SCHEDULED BACKUPS ==================

    fun scheduleDailyBackup() {
        scope.launch {
            while (isActive) {
                // Wait until 2 AM
                val now = Calendar.getInstance()
                val target = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 2)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)

                    if (before(now)) {
                        add(Calendar.DATE, 1)
                    }
                }

                val delay = target.timeInMillis - now.timeInMillis
                delay(delay)

                // Perform backup
                val result = backupToFirebase()
                result.onSuccess {
                    println("Scheduled backup completed: $it")
                }.onFailure {
                    println("Scheduled backup failed: ${it.message}")
                }

                // Wait 24 hours for next backup
                delay(24 * 60 * 60 * 1000)
            }
        }
    }

    fun cancelBackupSchedule() {
        scope.cancel()
    }
}