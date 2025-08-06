package com.movix.transak_infield

import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.Email
import androidx.compose.ui.text.font.Font
import androidx.tv.material3.Border
import com.itextpdf.kernel.colors.DeviceRgb
import java.sql.Date
import com.itextpdf.kernel.events.*
import com.movix.transak_infield.MainActivity
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.*
import com.itextpdf.layout.*
import com.itextpdf.layout.element.*
import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.kernel.pdf.canvas.PdfCanvas

import com.itextpdf.layout.properties.TextAlignment


open class ClientsCreation (var id:Int,val name:String,val phone:String) {}
class Estimateinfo(val id:Int,var titleINV:String,var creationDate:String,val dueDate: String,val customerId:Int)



open class FooterEvent : IEventHandler {
	override fun handleEvent(event: Event) {
		val docEvent = event as PdfDocumentEvent
		val pdfDoc = docEvent.document
		val page = docEvent.page
		val pageSize = page.pageSize
		val pdfCanvas = PdfCanvas(page.newContentStreamAfter(), page.resources, pdfDoc)

		val footerText = Paragraph(
			"| Water Pump Installations | Borehole Drilling & Equipping | Solar Structures | Plumbing | Electrical | Irrigation | Civil Works"
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



