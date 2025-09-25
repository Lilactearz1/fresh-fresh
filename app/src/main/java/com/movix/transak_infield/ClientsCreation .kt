package com.movix.transak_infield

import android.content.Context
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.events.*
import com.itextpdf.layout.*
import com.itextpdf.layout.element.*
import com.itextpdf.kernel.pdf.canvas.PdfCanvas

import com.itextpdf.layout.properties.TextAlignment


open class ClientsCreation (val id:Int=0,  /*Default to 0 so you don’t need to pass it*/
                            val name:String,val phone:String ,vararg mail:String)

data class Estimateinfo(
	val id: Int = 0, // autoIncrement in DB
	var titleINV: String = "",
	var creationDate: String = "",
	var dueDate: String = "",
	val customerId: Int = 0,
	var status: EstimateStatus = EstimateStatus.OPEN
)



open class FooterEvent : IEventHandler {
	override fun handleEvent(event: Event) {
		val docEvent = event as PdfDocumentEvent
		val pdfDoc = docEvent.document
		val page = docEvent.page
		val pageSize = page.pageSize
		val pdfCanvas = PdfCanvas(page.newContentStreamAfter(), page.resources, pdfDoc)

		val footerText = Paragraph(
			"| Water Pump Installations | Borehole Drilling & Equipping | Solar Structures | Fabrication | Plumbing | Electrical | Irrigation | Civil Works"
		) 	 .setFontSize(9f)
			.setFontColor(DeviceRgb(67, 105, 45))
			.setTextAlignment(TextAlignment.CENTER)

		// Define position
		val x = pageSize.width / 2
		val y = pageSize.bottom + 20f

		val canvas = Canvas(pdfCanvas, pageSize)
		canvas.showTextAligned(footerText, x, y, TextAlignment.CENTER)
		canvas.close()
	}
}
object EstimateSession {
	private const val PREF_NAME = "EstimateSessionPrefs"
	private const val KEY_CURRENT_ESTIMATE_ID = "current_estimate_id"

	var currentEstimate: Int? = null

	fun saveSession(context: Context, estimateId: Int) {
		val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
		prefs.edit().putInt(KEY_CURRENT_ESTIMATE_ID, estimateId).apply()
		currentEstimate = estimateId
	}

	fun loadSession(context: Context) {
		val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
		val id = prefs.getInt(KEY_CURRENT_ESTIMATE_ID, -1)
		currentEstimate = if (id != -1) id else null
	}

	fun clearSession(context: Context) {
		val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
		prefs.edit().remove(KEY_CURRENT_ESTIMATE_ID).apply()
		currentEstimate = null
	}
}








