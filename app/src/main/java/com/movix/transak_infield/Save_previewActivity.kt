package com.movix.transak_infield

import android.os.Binder
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.movix.transak_infield.MainActivity
import com.movix.transak_infield.PdfUtils



class Save_previewActivity : AppCompatActivity() {
	private lateinit var    _binding:Binder
	private lateinit var    db:DatabaseHandler
	private lateinit var    downloadbtn:ImageView
	private lateinit var    edit:ImageView
	private lateinit var    print:ImageView
	private lateinit var    more:ImageView

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()

		setContentView(R.layout.activity_save_preview)
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
			insets
		}

// use the file from the globalfunc to set the image preview
//		val pdfFile = File(context.filesDir, "sample.pdf")
//		val previewBitmap = pdfToImage(pdfFile, 0)  // 0 = first page
//
//		if (previewBitmap != null) {
//			imageView.setImageBitmap(previewBitmap)
//		}

		// DOWNLOAD THE PDF FROM THIS SIDE
//		 THE PDF FROM THIS SIDE TO OTHER APP IN PDF FORM

		downloadbtn = findViewById<ImageView>(R.id.Download1)

		downloadbtn.setOnClickListener{view->
			view.isHovered
			PdfUtils.generateEstimatePdf(applicationContext)

			Toast.makeText(applicationContext,"Success...",Toast.LENGTH_LONG).show()
			closeCurrentEstimate()



		}


	}
	fun closeCurrentEstimate() {
		db =DatabaseHandler(applicationContext)
		val id = EstimateSession.currentEstimate ?: return
		db.updateEstimateStatus(id, EstimateStatus.CLOSED)
		EstimateSession.clearSession(this)
	}

}