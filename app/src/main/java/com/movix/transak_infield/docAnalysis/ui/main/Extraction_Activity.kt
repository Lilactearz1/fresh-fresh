package com.movix.transak_infield.docAnalysis.ui.main

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.movix.transak_infield.R
import com.movix.transak_infield.docAnalysis.AppConstants
import com.movix.transak_infield.docAnalysis.data.remote.NetworkResults
import com.movix.transak_infield.docAnalysis.data.repository.InvoiceRepository
import com.movix.transak_infield.docAnalysis.ui.adapter.ExtractionAdapter
import com.movix.transak_infield.docAnalysis.util.FileUtils
import kotlinx.coroutines.launch


class Extraction_Activity : AppCompatActivity() {


    private val repository = InvoiceRepository()
    private lateinit var recyclerView: RecyclerView
    private val pickFile =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { uploadInvoice(it) }}



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_extraction)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
val btnUpload= findViewById<Button>(R.id.uploadAi)
        btnUpload.setOnClickListener {
            pickFile.launch("*/*")
        }

    }
    private fun uploadInvoice(uri: Uri) {

        lifecycleScope.launch {

            when(val result = repository.extractInvoice(
                AppConstants.AUTH_TOKEN,
                FileUtils.prepareFilePart(this@Extraction_Activity, uri)
            )) {
                is NetworkResults.Success -> {
                    recyclerView.adapter = ExtractionAdapter(result.data, repository)
                }
                is NetworkResults.Error -> Toast.makeText(applicationContext,result.message, Toast.LENGTH_SHORT)
                else -> {}
            }
        }
    }
}