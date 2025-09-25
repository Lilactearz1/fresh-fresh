package com.movix.transak_infield

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class PdfViewerActivity : AppCompatActivity() {

	private lateinit var pdfImageView: ImageView
	private var pdfRenderer: PdfRenderer? = null
	private var currentPage: PdfRenderer.Page? = null
	private var fileDescriptor: ParcelFileDescriptor? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_pdf_viewer)

		pdfImageView = findViewById(R.id.pdfImageView)

		val pdfPath = intent.getStringExtra("PDF_PATH")
		pdfPath?.let {
			displayPdf(File(it))
		}
	}

	private fun displayPdf(pdfFile: File) {
		try {
			fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
			pdfRenderer = PdfRenderer(fileDescriptor!!)
			openPage(0)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	private fun openPage(index: Int) {
		pdfRenderer?.let { renderer ->
			currentPage?.close()
			currentPage = renderer.openPage(index)

			val bitmap = Bitmap.createBitmap(
				currentPage!!.width,
				currentPage!!.height,
				Bitmap.Config.ARGB_8888
			)

			currentPage!!.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
			pdfImageView.setImageBitmap(bitmap)
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		currentPage?.close()
		pdfRenderer?.close()
		fileDescriptor?.close()
	}
}
