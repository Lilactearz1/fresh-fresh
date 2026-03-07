package com.movix.transak_infield.cloudB



import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import com.google.android.material.card.MaterialCardView
import com.movix.transak_infield.DatabaseHandler
import com.movix.transak_infield.R

import io.github.jan.supabase.storage.storage
import io.ktor.utils.io.printStack
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class BackupManagerActivity : AppCompatActivity() {

    private lateinit var dbHandler: DatabaseHandler
    private lateinit var cloudService: CloudBackupService

    private lateinit var progressBar: ProgressBar
    private lateinit var btnLocalBackup: MaterialCardView
    private lateinit var btnCloudBackup: CardView
    private lateinit var btnRestore: RelativeLayout

    private lateinit var switchScheduledBackup: Switch
    private lateinit var tvStatus: TextView
    private lateinit var listBackups: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup_manager)

        dbHandler = DatabaseHandler(this)
        cloudService = CloudBackupService(this)

        // Initialize views
        progressBar = findViewById(R.id.progressBar)
        btnLocalBackup = findViewById(R.id.btnLocalBackup)
        btnCloudBackup = findViewById(R.id.btnCloudBackup)
        btnRestore = findViewById(R.id.btnRestore)
        switchScheduledBackup = findViewById(R.id.btnSchedule)
        tvStatus = findViewById(R.id.tvStatus)
        listBackups = findViewById(R.id.listBackups)

        setupButtons()
        loadBackupList()
    }

    private fun setupButtons() {
        btnLocalBackup.setOnClickListener {
            createLocalBackup()
        }

        btnCloudBackup.setOnClickListener {

            CoroutineScope(Dispatchers.IO).launch {

                 try {
                     val buckets = SupabaseClient.client.storage.retrieveBuckets()

                     buckets.forEach {
                         println("Bucket: ${it.id}")
                     }
                } catch (e: Exception){
                    e.printStack()
                }



            }

            showCloudBackupOptions()


        }

        btnRestore.setOnClickListener {
            showRestoreDialog()
        }

        switchScheduledBackup.setOnCheckedChangeListener { _, _ ->
            toggleScheduledBackup()
        }



    }

    private fun createLocalBackup() {
        showProgress("Creating local backup...")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val backupFile = dbHandler.createBackupFile(applicationContext)

                withContext(Dispatchers.Main) {
                    hideProgress()
                    showSuccess("Backup created: ${backupFile.name}")
                    loadBackupList()

                    // Offer to share the file
                    shareBackupFile(backupFile)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    hideProgress()
                    showError("Backup failed: ${e.message}")
                }
            }
        }
    }

    private fun showCloudBackupOptions() {

        val options = arrayOf("Supabase Storage", "Google Drive", "Custom Server")

        AlertDialog.Builder(this)
            .setTitle("Upload to Cloud")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> uploadToSupabase()
                    1 -> uploadToGoogleDrive()
                    2 -> uploadToCustomServer()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun uploadToSupabase() {



        showProgress("Uploading to Supabase...")

        CoroutineScope(Dispatchers.IO).launch {

            val result = cloudService.backupToSupabase()

            withContext(Dispatchers.Main) {

                hideProgress()

                result.onSuccess {
                    showSuccess(it)
                }.onFailure {
                    showError("Upload failed: ${it.message}")
                }
            }
        }
    }

    private fun uploadToGoogleDrive() {
        // You'll need to implement Google Sign-In first
        // For now, show a message
        Toast.makeText(this, "Google Drive integration requires setup", Toast.LENGTH_LONG).show()
    }

    private fun uploadToCustomServer() {
        // Show dialog for server URL
        val editText = EditText(this).apply {
            hint = "https://your-server.com/backup"
            setText("https://your-server.com/api/backup")
        }

        AlertDialog.Builder(this)
            .setTitle("Custom Server Backup")
            .setView(editText)
            .setPositiveButton("Upload") { _, _ ->
                val serverUrl = editText.text.toString()
                if (serverUrl.isNotEmpty()) {
                    performCustomServerUpload(serverUrl)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performCustomServerUpload(serverUrl: String) {
        showProgress("Uploading to server...")

        CoroutineScope(Dispatchers.IO).launch {
            val result = cloudService.backupToCustomServer(serverUrl)

            withContext(Dispatchers.Main) {
                hideProgress()

                result.onSuccess {
                    showSuccess(it)
                }.onFailure {
                    showError("Server upload failed: ${it.message}")
                }
            }
        }
    }

     /*private fun showRestoreDialog_localBackup() {
        val backups = dbHandler.listBackups(applicationContext)
        if (backups.isEmpty()) {
            Toast.makeText(this, "No backup files found", Toast.LENGTH_SHORT).show()
            return
        }

        val backupNames = backups.map {
            val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(it.lastModified()))
            "${it.name} ($date)"
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Select Backup to Restore")
            .setItems(backupNames) { _, which ->
                confirmRestore(backups[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
    } */


    private fun showRestoreDialog() {

        showProgress("Loading cloud backups...")

        CoroutineScope(Dispatchers.IO).launch {

            try {

                val files = SupabaseClient.client.storage
                    .from("Infield_Backups")
                    .list("")
                Log.d("SUPABASE", "Files found: ${files.size}")

                withContext(Dispatchers.Main) {

                    hideProgress()

                    if (files.isEmpty()) {
                        Toast.makeText(
                            this@BackupManagerActivity,
                            "No cloud backups found",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@withContext
                    }

                    val names = files.map { it.name }.toTypedArray()

                    AlertDialog.Builder(this@BackupManagerActivity)
                        .setTitle("Restore from Cloud Backup")
                        .setItems(names) { _, which ->

                            val selectedFile = files[which].name
                            Log.d("BACKUP", "User selected backup: $selectedFile")
                            downloadAndRestore(selectedFile)

                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }

            } catch (e: Exception) {

                withContext(Dispatchers.Main) {
                    hideProgress()
                    showError("FAILED: ${parseSupabaseError(e)}")
                }

            }
        }
    }

    private fun parseSupabaseError(e: Exception): String {

        val msg = e.message ?: return "UNKNOWN_ERROR"

        return when {
            msg.contains("Unable to resolve host", true) -> "NO_INTERNET"
            msg.contains("timeout", true) -> "NETWORK_TIMEOUT"
            msg.contains("401") -> "AUTH_ERROR"
            msg.contains("403") -> "PERMISSION_DENIED"
            msg.contains("404") -> "BUCKET_NOT_FOUND"
            msg.contains("500") -> "SERVER_ERROR"
            else -> "UNKNOWN_ERROR"
        }
    }

    private fun downloadAndRestore(fileName: String) {
        Log.d("BACKUP", "downloadAndRestore called with $fileName")
        showProgress("Downloading backup...")

        CoroutineScope(Dispatchers.IO).launch {

            try {

                val bytes = SupabaseClient.client.storage
                    .from("Infield_Backups")
                    .downloadAuthenticated(path = fileName)

                val localFile = File(filesDir, fileName)

                localFile.writeBytes(bytes)

                withContext(Dispatchers.Main) {
                    hideProgress()
                    confirmRestore(localFile)
                }

            } catch (e: Exception) {

                withContext(Dispatchers.Main) {
                    hideProgress()
                    showError("Download failed: ${e.message}")
                }
            }
        }
    }

    private fun confirmRestore(backupFile: File) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Restore")
            .setMessage("Restore from ${backupFile.name}? This will replace current data.")
            .setPositiveButton("Restore") { _, _ ->

                Log.d("BACKUP", "Restore button clicked")

                showProgress("Restoring backup...")

                CoroutineScope(Dispatchers.IO).launch {

                    Log.d("BACKUP", "Starting restore coroutine")

                    val success = dbHandler.restoreBackup(applicationContext, backupFile)

                    withContext(Dispatchers.Main) {

                        hideProgress()

                        if (success) {
                            showSuccess("Restore completed")
                            recreate()
                        } else {
                            showError("Restore failed")
                        }

                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }




    private fun calculateDelayToFriday(): Long {

        val now = java.util.Calendar.getInstance()

        val nextFriday = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.FRIDAY)
            set(java.util.Calendar.HOUR_OF_DAY, 2)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)

            if (before(now)) {
                add(java.util.Calendar.WEEK_OF_YEAR, 1)
            }
        }

        return nextFriday.timeInMillis - now.timeInMillis
    }



    private fun toggleScheduledBackup() {

        val workManager = androidx.work.WorkManager.getInstance(this)

        if (switchScheduledBackup.isChecked) {

            // enable backup
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val delay = calculateDelayToFriday()

            val request = PeriodicWorkRequestBuilder<BackupWorker>(
                7,
                TimeUnit.DAYS
            )
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build()

            workManager.enqueueUniquePeriodicWork(
                "weekly_backup",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )

        } else {

            // disable backup
            workManager.cancelUniqueWork("weekly_backup")
        }
    }


    private fun loadBackupList() {
        val backups = dbHandler.listBackups(applicationContext)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            backups.map { backup ->
                val size = String.format("%.2f MB", backup.length() / (1024.0 * 1024.0))
                val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .format(Date(backup.lastModified()))
                "${backup.name}\n$date • $size"
            }
        )
        listBackups.adapter = adapter

        // Show/hide empty state
        findViewById<TextView>(R.id.tvEmptyBackups).isVisible = backups.isEmpty()
    }

    private fun shareBackupFile(file: File) {
        // Create share intent
        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "application/zip"
            putExtra(android.content.Intent.EXTRA_STREAM, androidx.core.content.FileProvider.getUriForFile(
                this@BackupManagerActivity,
                "${packageName}.provider",
                file
            ))
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(android.content.Intent.createChooser(shareIntent, "Share Backup File"))
    }

    private fun showProgress(message: String) {
        progressBar.isVisible = true
        tvStatus.text = message
    }

    private fun hideProgress() {
        progressBar.isVisible = false
    }

    private fun showSuccess(message: String) {
        tvStatus.text = "✓ $message"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        tvStatus.text = "✗ $message"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        cloudService.cancelBackupSchedule()
    }
}
