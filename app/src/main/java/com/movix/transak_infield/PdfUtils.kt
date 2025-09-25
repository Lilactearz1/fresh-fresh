package com.movix.transak_infield

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.movix.transak_infield.MainActivity
import java.io.File

object PdfUtils {

	fun generateEstimatePdf(context: Context): File? {
		return try {
			MainActivity.estimatePdf(context)
		} catch (e: Exception) {
			e.printStackTrace()
			null
		}
	}


	fun generatePdfPreview(context: Context, pdfFile: File): Bitmap? {
		return try {
			val fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
			val renderer = PdfRenderer(fileDescriptor)
			val page = renderer.openPage(0)

			val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
			page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
			page.close()
			renderer.close()
			fileDescriptor.close()

			bitmap
		} catch (e: Exception) {
			e.printStackTrace()
			null
		}
	}
	fun sharePdf(context: Context, file: File) {
		val uri = FileProvider.getUriForFile(
			context,
			"${context.packageName}.provider",
			file
		)

		val shareIntent = Intent(Intent.ACTION_SEND).apply {
			type = "application/pdf"
			putExtra(Intent.EXTRA_STREAM, uri)
			addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
		}

		context.startActivity(Intent.createChooser(shareIntent, "Share PDF"))
	}

	fun previewPdfFormat(context: Context, pdfFile: File) {
		try {
			val intent = Intent(context, PdfViewerActivity::class.java)
			intent.putExtra("PDF_PATH", pdfFile.absolutePath)
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			context.startActivity(intent)
		} catch (e: Exception) {
			e.printStackTrace()
			Handler(Looper.getMainLooper()).post {
				Toast.makeText(context, "Unable to preview PDF", Toast.LENGTH_SHORT).show()
			}
		}
	}


}

