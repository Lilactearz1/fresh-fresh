package com.movix.transak_infield.cloudB



import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.movix.transak_infield.DatabaseHandler
import com.movix.transak_infield.R
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class BackupManagerActivity : AppCompatActivity() {

    private lateinit var dbHandler: DatabaseHandler
    private lateinit var cloudService: CloudBackupService

    private lateinit var progressBar: ProgressBar
    private lateinit var btnLocalBackup: Button
    private lateinit var btnCloudBackup: Button
    private lateinit var btnRestore: Button
    private lateinit var btnSchedule: Button
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
        btnSchedule = findViewById(R.id.btnSchedule)
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
            showCloudBackupOptions()
        }

        btnRestore.setOnClickListener {
            showRestoreDialog()
        }

        btnSchedule.setOnClickListener {
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
        val options = arrayOf("Firebase Storage", "Google Drive", "Custom Server")

        AlertDialog.Builder(this)
            .setTitle("Upload to Cloud")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> uploadToFirebase()
                    1 -> uploadToGoogleDrive()
                    2 -> uploadToCustomServer()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun uploadToFirebase() {
        showProgress("Uploading to Firebase...")

        CoroutineScope(Dispatchers.IO).launch {
            val result = cloudService.backupToFirebase()

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

    private fun showRestoreDialog() {
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
    }

    private fun confirmRestore(backupFile: File) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Restore")
            .setMessage("Restore from ${backupFile.name}? This will replace current data.")
            .setPositiveButton("Restore") { _, _ ->
                // Implement restore logic here
                Toast.makeText(this, "Restore functionality to be implemented", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun toggleScheduledBackup() {
        // Toggle scheduled backups
        if (btnSchedule.text == "Enable Scheduled Backup") {
            cloudService.scheduleDailyBackup()
            btnSchedule.text = "Disable Scheduled Backup"
            Toast.makeText(this, "Daily backups enabled (2 AM)", Toast.LENGTH_SHORT).show()
        } else {
            cloudService.cancelBackupSchedule()
            btnSchedule.text = "Enable Scheduled Backup"
            Toast.makeText(this, "Scheduled backups disabled", Toast.LENGTH_SHORT).show()
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