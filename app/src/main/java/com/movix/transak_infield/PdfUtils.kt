package com.movix.transak_infield

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.layout.Document

import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter

import java.io.File
import com.movix.transak_infield.MainActivity
import com.movix.transak_infield.ui.theme.TemplateInterface

object PdfUtils {

	fun generateEstimatePdf(context: Context,estimateId:Int,customerId:Int,templateDRW: PdfTemplateDRW): File? {
		return try {
			MainActivity.estimatePdf(context, estimateId, customerId, templateDRW)

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
// function that load json templates

	fun loadTemplateFromJson(context: Context, rawResId: Int): TemplateLayout {
		val inputStream = context.resources.openRawResource(rawResId)
		val json = inputStream.bufferedReader().use { it.readText() }
		return Gson().fromJson(json, TemplateLayout::class.java)
	}

	fun saveTemplate(context: Context, templateName: String) {
		context.getSharedPreferences("templates", Context.MODE_PRIVATE)
			.edit()
			.putString("selected", templateName)
			.apply()
	}

	fun loadTemplate(context: Context): PdfTemplateDRW {
		val name = context.getSharedPreferences("templates", Context.MODE_PRIVATE)
			.getString("selected", PdfTemplateDRW.CLASSIC.name)

		return PdfTemplateDRW.valueOf(name!!)
	}

	fun load(context: Context, estimateId: Int, customerId: Int): EstimatePDFData {
		val db = DatabaseHandler(context)

		// Load customer details safely
		val customer = db.getCustomerById(customerId)
			?: throw IllegalArgumentException("Customer with ID $customerId not found")

		// Load estimate items
		val items = db.getItemsForEstimate(estimateId, customerId)

		// Compute totals
		val subtotal = items.sumOf { it.price * it.quantity }
		val taxTotal = items.sumOf { it.tax.toDouble() }
		val grandTotal = subtotal + taxTotal

		// Load estimate header safely
		val estimate = db.getEstimateById(estimateId)
			?: throw IllegalArgumentException("Estimate with ID $estimateId not found")

		return EstimatePDFData(
			estimateId = estimateId,
			customerId = customerId,
			customerName = customer.name,
			customerPhone = customer.phone,
			estimateTitle = estimate.titleINV ?: "Untitled",
			estimateDate = estimate.creationDate ?: "N/A",
			dueDate = estimate.dueDate,
			items = items,
			subtotal = subtotal,
			taxTotal = taxTotal,
			grandTotal = grandTotal
		)
	}

	/** Generate PDF with iText */
	fun generate(
		context: Context,
		template: TemplateInterface,
		layout: TemplateLayout,
		data: EstimatePDFData
	): File {

		val file = File(
			context.getExternalFilesDir(null),
			"estimate_${data.estimateId}.pdf"
		)

		// Open background template PDF
		val bgStream = context.resources.openRawResource(
			context.resources.getIdentifier(layout.background.removeSuffix(".pdf"), "raw", context.packageName)
		)

		val reader = PdfReader(bgStream)
		val writer = PdfWriter(file)
		val pdfDoc = PdfDocument(reader, writer)
		val document = Document(pdfDoc)

		// Draw the PDF content
		template.drawHeader(document, layout, data,context)
		template.drawTable(document, layout, data,context)
		template.drawFooter(document, layout, data,context)

		document.close()
		pdfDoc.close()

		return file
	}


}




object ColorHelper {

	fun color(context: Context, colorRes: Int): DeviceRgb {
		val intColor = ContextCompat.getColor(context, colorRes)

		val r = (intColor shr 16) and 0xFF
		val g = (intColor shr 8) and 0xFF
		val b = intColor and 0xFF

		return DeviceRgb(r, g, b)
	}

	fun rgb(r: Int, g: Int, b: Int): DeviceRgb {
		return DeviceRgb(r, g, b)
	}
}
