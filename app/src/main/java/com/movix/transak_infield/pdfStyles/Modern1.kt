package com.movix.transak_infield.pdfStyles

import android.content.Context
import android.graphics.Color.rgb
import android.util.Log
import androidx.annotation.ColorRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.Rgb
import androidx.core.content.ContextCompat
import androidx.tv.material3.Border
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.UnitValue
import com.movix.transak_infield.ColorHelper
import com.movix.transak_infield.CompanyDetail
import com.movix.transak_infield.EstimatePDFData
import com.movix.transak_infield.MainActivity.Companion.getFontGerhana
import com.movix.transak_infield.MainActivity.Companion.latoBold
import com.movix.transak_infield.MainActivity.Companion.latoRegularFont
import com.movix.transak_infield.MainActivity.Companion.queensFont
import com.movix.transak_infield.PdfUtils
import com.movix.transak_infield.TemplateLayout
import com.movix.transak_infield.Templatepdf1
import com.movix.transak_infield.ui.theme.*
import com.itextpdf.layout.borders.*
import com.itextpdf.layout.properties.TextAlignment

private val cname1: String = CompanyDetail.COMPANY_NAME_1.text
private val cname2 = CompanyDetail.COMPANY_NAME_2.text
private val c1quoteNo = CompanyDetail.QUOTE_NO.text
private val c1quoteTo = CompanyDetail.QUOTE_TO.text
private val c1quoteFor = CompanyDetail.QUOTE_FOR.text
private val c1quoteHeader = CompanyDetail.QUOTE_HEADER.text
private val c1date = CompanyDetail.DATE.text
private var pdf = Templatepdf1()



class Modern1(context: Context) : TemplateInterface {
	//	class fonts
	val latoBold: PdfFont = latoBold(context)
	val latoRegular = latoRegularFont(context)
	val gerhanaFont: PdfFont = getFontGerhana(context)
	val queensFont: PdfFont = queensFont(context)

	override fun drawHeader(
		document: Document, layout: TemplateLayout, dataEst: EstimatePDFData, context: Context
	) {
		// COMPANY NAME

//		val companyText1 = cname1
//		document.add(
//
//			com.itextpdf.layout.element.Paragraph(companyText1).setFontSize(18f).setBold()
//				.setFixedPosition(
//					layout.company.x, layout.company.y, layout.company.width ?: 200f
//				).setFontColor(
//					ColorHelper.rgb(138, 0, 0))
//		)
//
//		val companyText2= cname2
//		document.add(
//
//			com.itextpdf.layout.element.Paragraph(companyText2).setFontSize(18f)
//				.setFixedPosition(
//					layout.company.x, layout.company.y, layout.company.width ?: 200f
//				).setFontColor(
//					ColorHelper.rgb(0, 92, 92))
//		)
//
//		// QUOTATION TITLE
//		document.add(
//			com.itextpdf.layout.element.Paragraph(c1quoteHeader).setFontSize(19f).setBold()
//				.setFixedPosition(
//					layout.title.x, layout.title.y, layout.title.width ?: 200f
//				).setFontColor(
//					ColorHelper.rgb(10, 63, 93))
//		)
		val clientName =dataEst.customerName
		val clientId=dataEst.customerId
		val dueDate = dataEst.dueDate
		 val items =dataEst.items
		items.forEachIndexed { v,t ->
			t.total
		}

		//bar code insertion
		val barcodeImg = pdf.barCodeGenerator("${dataEst.customerName}_${dataEst.estimateId}")

		document.add(
			barcodeImg.setFixedPosition(
				layout.qr.x, layout.qr.y
			)
		)

		// QUOTE NUMBER
		document.add(
			Paragraph("$c1quoteNo ${dataEst.estimateId}").setFontSize(13f).setFixedPosition(
				layout.quoteNo.x, layout.quoteNo.y, layout.quoteNo.width ?: 150f
			).setFontColor(
				ColorHelper.rgb(10, 63, 93)
			).setFont(latoBold)
		)

		// CUSTOMER NAME
		document.add(
			Paragraph("$c1quoteTo ${dataEst.customerName}").setFontSize(13f).setFixedPosition(
				layout.customer.x, layout.customer.y, layout.customer.width ?: 200f
			).setFont(latoRegular)
		)

		// DATE
		document.add(
			Paragraph("$c1date ${dataEst.estimateDate}").setFontSize(13f).setFixedPosition(
				layout.created.x, layout.created.y, layout.created.width ?: 150f
			).setFontColor(
				DeviceRgb(10, 63, 93)
			).setFont(latoBold)
		)

		// DUE DATE
		document.add(
			Paragraph("Due: ${dataEst.dueDate}").setFontSize(13f).setFontColor(
				DeviceRgb(10, 63, 93)
			).setFont(latoBold)

				.setFixedPosition(
					layout.due.x, layout.due.y, layout.due.width ?: 150f
				)
		)

	}


	override fun drawFooter(
		document: Document, layout: TemplateLayout, dataEst: EstimatePDFData, context: Context
	) {
		val y = layout.amountBox.y

		val subTotal=String.format("%,.2f",dataEst.subtotal)
		val taxTotal=String.format("%,.2f",dataEst.taxTotal)
		val grandTotal=String.format("%,.2f",dataEst.grandTotal)


		document.add(
			Paragraph("Subtotal: $subTotal").setFontSize(12f)
				.setFixedPosition(layout.amountBox.x, y, 200f)
		)

		document.add(
			Paragraph("Tax: $taxTotal").setFontSize(12f)
				.setFixedPosition(layout.amountBox.x, y - 20f, 200f)
		)

		document.add(
			Paragraph("Grand Total: $grandTotal").setBold().setFontSize(14f)
				.setFixedPosition(layout.amountBox.x, y - 40f, 200f).setBackgroundColor(DeviceRgb(237, 237, 237))
		)
	}


	override fun drawTable(
		document: Document, layout: TemplateLayout, dataEst: EstimatePDFData, context: Context
	) {

		// Define column widths (No, Description, Qty, Unit Price, Total)
		val columnWidths = floatArrayOf(40f, 200f, 70f, 70f, 80f)

		val table =
			Table(columnWidths).setWidth(UnitValue.createPercentValue(100f)).setMarginTop(200f)

		// ----- HEADER ROW -----
		val headerColor = DeviceRgb(10, 63, 93)
		listOf("S/N", "DESCRIPTION", "QUANTITY", "PRICE", "TOTAL").forEach { header ->
			table.addHeaderCell(
				Cell().add(Paragraph(header)).setBackgroundColor(headerColor).setFontSize(13f)
					.setFont(latoBold).setFontColor(DeviceRgb.WHITE)
			)
		}

		// ----- TABLE DATA -----
		dataEst.items.forEachIndexed { index, item ->

			table.addCell(Cell().add(Paragraph("${index + 1}").setFont(gerhanaFont)))
			table.addCell(Cell().add(Paragraph(item.itemName ?: "").setFont(gerhanaFont)))
			table.addCell(Cell().add(Paragraph(item.quantity.toString()).setFont(gerhanaFont).setTextAlignment(
				TextAlignment.CENTER)))
			table.addCell(
				Cell().add(
					Paragraph(String.format("%,.2f", item.price)).setFont(
						gerhanaFont
					).setTextAlignment(
						TextAlignment.CENTER)
				)
			)
			table.addCell(
				Cell().add(
					Paragraph(
					 "${item.quantity * item.price}"
					).setFont(gerhanaFont).setTextAlignment(
						TextAlignment.RIGHT)
				)
			).setFixedLayout().setBorder (SolidBorder.NO_BORDER)
		}

		// ----- ADD TABLE TO DOCUMENT -----
		document.add(table)
	}


	class Minimal(context: Context) : TemplateInterface {
		override fun drawHeader(
			document: Document, layout: TemplateLayout, dataEst: EstimatePDFData, context: Context
		) {
			// COMPANY NAME
			val companyText = "$cname1 $cname2"
			document.add(
				com.itextpdf.layout.element.Paragraph(companyText).setFontSize(18f).setBold()
					.setFixedPosition(
						layout.company.x, layout.company.y, layout.company.width ?: 200f
					)
			)

			// QUOTATION TITLE
			document.add(
				com.itextpdf.layout.element.Paragraph(c1quoteHeader).setFontSize(16f).setBold()
					.setFixedPosition(
						layout.title.x, layout.title.y, layout.title.width ?: 200f
					)
			)

			// QUOTE NUMBER
			document.add(
				com.itextpdf.layout.element.Paragraph("$c1quoteNo ${dataEst.estimateId}")
					.setFontSize(12f).setFixedPosition(
						layout.quoteNo.x, layout.quoteNo.y, layout.quoteNo.width ?: 150f
					)
			)

			// CUSTOMER NAME
			document.add(
				com.itextpdf.layout.element.Paragraph("$c1quoteTo ${dataEst.customerName}")
					.setFontSize(12f).setFixedPosition(
						layout.customer.x, layout.customer.y, layout.customer.width ?: 200f
					)
			)

			// DATE
			document.add(
				com.itextpdf.layout.element.Paragraph("$c1date ${dataEst.estimateDate}")
					.setFontSize(12f).setFixedPosition(
						layout.created.x, layout.created.y, layout.created.width ?: 150f
					)
			)

			// DUE DATE
			document.add(
				com.itextpdf.layout.element.Paragraph("Due: ${dataEst.dueDate}").setFontSize(12f)
					.setFixedPosition(
						layout.due.x, layout.due.y, layout.due.width ?: 150f
					)
			)

		}

		override fun drawFooter(
			document: Document, layout: TemplateLayout, dataEst: EstimatePDFData, context: Context
		) {
			val y = layout.amountBox.y

			document.add(
			 Paragraph("Subtotal: ${dataEst.subtotal}")
					.setFontSize(12f).setFixedPosition(layout.amountBox.x, y, 200f).setFont(latoRegularFont(context))
			)

			document.add(
				com.itextpdf.layout.element.Paragraph("Tax: ${dataEst.taxTotal}").setFontSize(12f)
					.setFixedPosition(layout.amountBox.x, y - 20f, 200f)
			)

			document.add(
				com.itextpdf.layout.element.Paragraph("Grand Total: ${dataEst.grandTotal}")
					.setBold().setFontSize(14f).setFixedPosition(layout.amountBox.x, y - 40f, 200f)
			)
		}


		override fun drawTable(
			document: Document, layout: TemplateLayout, dataEst: EstimatePDFData, context: Context
		) {

		}
	}


	class Classic1(context: Context) : TemplateInterface {
		override fun drawHeader(
			document: Document, layout: TemplateLayout, dataEst: EstimatePDFData, context: Context
		) {
			// COMPANY NAME
			val companyText = "$cname1 $cname2"
			document.add(
				com.itextpdf.layout.element.Paragraph(companyText).setFontSize(18f).setBold()
					.setFixedPosition(
						layout.company.x, layout.company.y, layout.company.width ?: 200f
					)
			)

			// QUOTATION TITLE
			document.add(
			 Paragraph(c1quoteHeader).setFontSize(16f).setBold()
					.setFixedPosition(
						layout.title.x, layout.title.y, layout.title.width ?: 200f
					)
			)

			// QUOTE NUMBER
			document.add(
				com.itextpdf.layout.element.Paragraph("$c1quoteNo ${dataEst.estimateId}")
					.setFontSize(12f).setFixedPosition(
						layout.quoteNo.x, layout.quoteNo.y, layout.quoteNo.width ?: 150f
					)
			)

			// CUSTOMER NAME
			document.add(
				com.itextpdf.layout.element.Paragraph("$c1quoteTo ${dataEst.customerName}")
					.setFontSize(12f).setFixedPosition(
						layout.customer.x, layout.customer.y, layout.customer.width ?: 200f
					)
			)

			// DATE
			document.add(
				com.itextpdf.layout.element.Paragraph("$c1date ${dataEst.estimateDate}")
					.setFontSize(12f).setFixedPosition(
						layout.created.x, layout.created.y, layout.created.width ?: 150f
					)
			)

			// DUE DATE
			document.add(
				com.itextpdf.layout.element.Paragraph("Due: ${dataEst.dueDate}").setFontSize(12f)
					.setFixedPosition(
						layout.due.x, layout.due.y, layout.due.width ?: 150f
					)
			)

		}

		override fun drawFooter(
			document: Document, layout: TemplateLayout, dataEst: EstimatePDFData, context: Context
		) {
			val y = layout.amountBox.y
			val subTotal=String.format("%,.2f",dataEst.subtotal)
			val taxTotal=String.format("%,.2f",dataEst.taxTotal)
			val grandTotal=String.format("%,.2f",dataEst.grandTotal)


			document.add(
			 Paragraph("Subtotal: $subTotal")
					.setFontSize(14f).setFixedPosition(layout.amountBox.x, y, 200f).setFont(latoRegularFont(context))
			)
			Log.d("tag message", "drawFooter: $taxTotal ,$subTotal ,$grandTotal")
			document.add(
			 Paragraph("Tax: $taxTotal").setFontSize(14f)
					.setFixedPosition(layout.amountBox.x, y - 20f, 200f)
			)

			document.add(
			 Paragraph("Grand Total: $grandTotal}").setFont(latoBold(context))
					.setFontSize(14f).setFixedPosition(layout.amountBox.x, y - 40f, 200f)
				 .setBackgroundColor(DeviceRgb(181, 176, 141))
			)
		}


		override fun drawTable(
			document: Document, layout: TemplateLayout, dataEst: EstimatePDFData, context: Context
		) {

		}
	}
}


