package com.movix.transak_infield.pdfStyles

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color.rgb
import android.util.Log
import androidx.annotation.ColorRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.colorspace.Rgb
import androidx.core.content.ContextCompat
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.io.source.OutputStream
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
import com.itextpdf.layout.element.AreaBreak
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.LineSeparator
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.VerticalAlignment
import com.movix.transak_infield.GlobalFunck
import kotlinx.serialization.StringFormat
import java.io.ByteArrayOutputStream


private val cname1: String = CompanyDetail.COMPANY_NAME_1.text
private val cname2 = CompanyDetail.COMPANY_NAME_2.text
private val c1quoteNo = CompanyDetail.QUOTE_NO.text
private val c1quoteTo = CompanyDetail.QUOTE_TO.text
private val c1quoteFor = CompanyDetail.QUOTE_FOR.text
private val c1quoteHeader = CompanyDetail.QUOTE_HEADER.text
private val c1date = CompanyDetail.DATE.text
private var pdf = Templatepdf1()
private val sFormat = "%,.2f"


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
        val global_Functions = GlobalFunck()

        val title = dataEst.estimateTitle
        val clientName = dataEst.customerName
        val safeClientId = dataEst.customerId


        val dueDate = dataEst.dueDate
        val items = dataEst.items
        items.forEachIndexed { v, t ->
            t.total
        }

        //bar code insertion
        val barcodeImg = pdf.barCodeGenerator("${clientName}_${title.trim()}_${safeClientId}")

        document.add(
            barcodeImg.setFixedPosition(
                layout.qr.x, layout.qr.y
            )
        )

        // QUOTE NUMBER
        document.add(
            Paragraph("$c1quoteNo EQT-${dataEst.estimateId}").setFontSize(13f).setFixedPosition(
                layout.quoteNo.x, layout.quoteNo.y, layout.quoteNo.width ?: 150f
            ).setFontColor(
                ColorHelper.rgb(10, 63, 93)
            ).setFont(latoBold)
        )

        // CUSTOMER NAME
        document.add(
            Paragraph("$c1quoteTo ${clientName.uppercase()}").setFontSize(12f)
                .setFixedPosition(
                    layout.customer.x, layout.customer.y, layout.customer.width ?: 250f
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
            Paragraph("DUE : ${dataEst.dueDate}").setFontSize(13f).setFontColor(
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
        val x = layout.amountBox.x


        val subTotal = String.format(sFormat, dataEst.subtotal)
        val taxTotal = String.format(sFormat, dataEst.taxTotal)
        val grandTotal = String.format(sFormat, dataEst.grandTotal)

        val columnWidths = floatArrayOf(y)
// used separate map inorder to perform different formating
// and styles otherwise could have use on map variables

        val mapSubtotal = hashMapOf("Subtotal" to subTotal)
        val mapTax = hashMapOf("Tax" to taxTotal)
        val mapTotals = hashMapOf("Grand Total" to grandTotal)

        val table = Table(columnWidths)

        mapSubtotal.forEach { k, it ->
            table.addCell(
                Cell().add(Paragraph("${k}  $it")).setMaxWidth(10f)
                    .setTextAlignment(TextAlignment.RIGHT).setFont(latoRegular)
                    .setBorder(Border.NO_BORDER)
            ).setMarginTop(30f).setHorizontalAlignment(HorizontalAlignment.RIGHT)


        }
        mapTax.forEach { k, it ->

            table.addCell(
                Cell().add(Paragraph("${k}  $it")).setTextAlignment(TextAlignment.RIGHT)
                    .setFont(latoRegular).setBorder(Border.NO_BORDER)
            ).setMarginTop(30f).setHorizontalAlignment(HorizontalAlignment.RIGHT)

        }

        mapTotals.forEach { k, it ->

            table.addCell(
                Cell().add(Paragraph("${k}  $it")).setMaxWidth(10f)
                    .setTextAlignment(TextAlignment.RIGHT).setFont(latoBold)
                    .setBackgroundColor(DeviceRgb(252, 252, 252)).setBorder(Border.NO_BORDER)
            ).setMarginTop(30f).setHorizontalAlignment(HorizontalAlignment.RIGHT)

        }

        document.add(table)
        // insert the infield stamp
        // load bitmap
        val image = Templatepdf1().loadStamp(context)
        // convert bitmap to byte array
        val stream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
//        create imagedata
        val imageData = ImageDataFactory.create(byteArray)
        val imagdt = Image(imageData)
//        image sizing and position
        imagdt.setMarginLeft(250f).setMarginTop(5f)
        document.add(imagdt)
    }


    override fun drawTable(
        document: Document, layout: TemplateLayout, dataEst: EstimatePDFData, context: Context
    ) {

        val columnWidths = floatArrayOf(40f, 200f, 70f, 70f, 80f)
        val textColor = DeviceRgb(44, 45, 47)
        val border = SolidBorder(DeviceRgb(224, 224, 244), 0.9f)
        val headerColor = DeviceRgb(10, 63, 93)

        val maxRowsPerPage = 14
        var rowCounter = 0
        var isFirstPage = true

        // --- TABLE BUILDER ---
        fun createTable(isFirst: Boolean): Table {
            val t = Table(columnWidths).setWidth(UnitValue.createPercentValue(100f))
                .setFont(gerhanaFont).setFontColor(textColor)

            // only first page gets top margin
            if (isFirst) {
                t.setMarginTop(200f)
            }

            // only first page gets header cells
            if (isFirst) {
                listOf("S/N", "DESCRIPTION", "QUANTITY", "PRICE", "TOTAL").forEach { header ->
                    t.addHeaderCell(
                        Cell().add(Paragraph(header)).setBackgroundColor(headerColor)
                            .setFontSize(13f).setFont(latoBold).setFontColor(DeviceRgb.WHITE)
                            .setBorder(Border.NO_BORDER)
                    )
                }
            }

            return t
        }

        var table = createTable(true)

        // --- ADD ROWS ---
        dataEst.items.forEachIndexed { index, item ->

            if (rowCounter >= maxRowsPerPage) {

                // add current table to page
                document.add(table.setBorder(border))

                // new page
                document.add(AreaBreak())

                // create a table WITHOUT margin and WITHOUT header
                isFirstPage = false
                table = createTable(false)

                rowCounter = 0
            }

            // TABLE DATA ROWS
            table.addCell(Cell().add(Paragraph("${index + 1}")).setBorder(Border.NO_BORDER))
            table.addCell(Cell().add(Paragraph(item.itemName.uppercase())).setBorder(border))
            table.addCell(
                Cell().add(
                    Paragraph(item.quantity.toString()).setTextAlignment(TextAlignment.CENTER)
                ).setBorder(border)
            )
            table.addCell(
                Cell().add(
                    Paragraph(String.format(sFormat, item.price)).setFont(gerhanaFont)
                        .setTextAlignment(TextAlignment.CENTER)
                ).setBorder(border)
            )

            val amount = item.quantity * item.price
            val formattedTotal = String.format(sFormat, amount)

            table.addCell(
                Cell().add(
                    Paragraph(formattedTotal).setFont(gerhanaFont)
                        .setTextAlignment(TextAlignment.RIGHT)
                ).setBorder(border)
            ).setFixedLayout()

            rowCounter++
        }

        // add final table
        document.add(table.setBorder(border))

    }


    class Minimal(context: Context) : TemplateInterface {
        override fun drawHeader(
            document: Document, layout: TemplateLayout, dataEst: EstimatePDFData, context: Context
        ) {
            // COMPANY NAME
            val companyText = "$cname1 $cname2"
            document.add(
                Paragraph(companyText).setFontSize(18f).setBold().setFixedPosition(
                    layout.company.x, layout.company.y, layout.company.width ?: 200f
                )
            )

            // QUOTATION TITLE
            document.add(
                Paragraph(c1quoteHeader).setFontSize(16f).setBold().setFixedPosition(
                    layout.title.x, layout.title.y, layout.title.width ?: 200f
                )
            )

            // QUOTE NUMBER
            document.add(
                Paragraph("$c1quoteNo ${dataEst.estimateId}")
                    .setFontSize(12f).setFixedPosition(
                        layout.quoteNo.x, layout.quoteNo.y, layout.quoteNo.width ?: 150f
                    )
            )

            // CUSTOMER NAME
            document.add(
                Paragraph("$c1quoteTo ${dataEst.customerName.uppercase()}")
                    .setFontSize(12f).setFixedPosition(
                        layout.customer.x, layout.customer.y, layout.customer.width ?: 200f
                    )
            )

            // DATE
            document.add(
                Paragraph("$c1date ${dataEst.estimateDate}")
                    .setFontSize(12f).setFixedPosition(
                        layout.created.x, layout.created.y, layout.created.width ?: 150f
                    )
            )

            // DUE DATE
            document.add(
                Paragraph("Due: ${dataEst.dueDate}").setFontSize(12f)
                    .setFixedPosition(
                        layout.due.x, layout.due.y, layout.due.width ?: 150f
                    )
            )

        }

        override fun drawFooter(
            document: Document, layout: TemplateLayout, dataEst: EstimatePDFData, context: Context
        ) {


            document.add(
                Paragraph("Subtotal: ${dataEst.subtotal}").setFontSize(12f)
                    .setFont(latoRegularFont(context))
            )

            document.add(
                Paragraph("Tax: ${dataEst.taxTotal}").setFontSize(12f)

            )

            document.add(
                Paragraph("Grand Total: ${dataEst.grandTotal}").setBold().setFontSize(14f)
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
                Paragraph(c1quoteHeader).setFontSize(16f).setBold().setFixedPosition(
                    layout.title.x, layout.title.y, layout.title.width ?: 200f
                )
            )

            // QUOTE NUMBER
            document.add(
                Paragraph("$c1quoteNo ${dataEst.estimateId}")
                    .setFontSize(12f).setFixedPosition(
                        layout.quoteNo.x, layout.quoteNo.y, layout.quoteNo.width ?: 150f
                    )
            )

            // CUSTOMER NAME
            document.add(
                Paragraph("$c1quoteTo ${dataEst.customerName}")
                    .setFontSize(12f).setFixedPosition(
                        layout.customer.x, layout.customer.y, layout.customer.width ?: 200f
                    )
            )

            // DATE
            document.add(
                Paragraph("$c1date ${dataEst.estimateDate}")
                    .setFontSize(12f).setFixedPosition(
                        layout.created.x, layout.created.y, layout.created.width ?: 150f
                    )
            )

            // DUE DATE
            document.add(
                Paragraph("Due: ${dataEst.dueDate}").setFontSize(12f)
                    .setFixedPosition(
                        layout.due.x, layout.due.y, layout.due.width ?: 150f
                    )
            )

        }

        override fun drawFooter(
            document: Document, layout: TemplateLayout, dataEst: EstimatePDFData, context: Context
        ) {
            val y = layout.amountBox.y
            val subTotal = String.format("%,.2f", dataEst.subtotal)
            val taxTotal = String.format("%,.2f", dataEst.taxTotal)
            val grandTotal = String.format("%,.2f", dataEst.grandTotal)


            document.add(
                Paragraph("Subtotal: $subTotal").setFontSize(14f)
                    .setFixedPosition(layout.amountBox.x, y, 200f).setFont(latoRegularFont(context))
            )

            document.add(
                Paragraph("Tax: $taxTotal").setFontSize(14f)
                    .setFixedPosition(layout.amountBox.x, y - 20f, 200f)
            )

            document.add(
                Paragraph("Grand Total: $grandTotal}").setFont(latoBold(context)).setFontSize(14f)
                    .setFixedPosition(layout.amountBox.x, y - 40f, 200f)
                    .setBackgroundColor(DeviceRgb(181, 176, 141))
            )
        }


        override fun drawTable(
            document: Document, layout: TemplateLayout, dataEst: EstimatePDFData, context: Context
        ) {

        }
    }
}


