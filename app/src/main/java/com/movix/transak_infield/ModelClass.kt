package com.movix.transak_infield

import java.time.LocalDate

//the data model class
open class ModelClass(
	val id: Int,
	val quantity: Int,
	val itemName: String,
	val price: Double,
	val total: Float,
	val tax: Float,
	val customerId: Int,
	val estimateId: Int
)


enum class EstimateStatus {
	OPEN,
	COMPLETED
}

data class TemplateLayout(
	val name: String,
	val background: String,
	val company: Position,
	val customer: Position,
	val logo: Position,
	val title: Position,
	val quoteNo: Position,
	val created: Position,
	val due: Position,
	val tableStart: Position,
	val amountBox: Position,
	val qr: Position
)

data class Position(
	val x: Float,
	val y: Float,
	val width: Float? = null,
	val height: Float? = null
)

enum class PdfTemplateDRW(val jsonResId: Int, val previewRes: Int) {

	CLASSIC(R.raw.classic_template,R.drawable.pic1),
	MODERN(R.raw.modern_template,R.drawable.pic2),
	MINIMAL(R.raw.minimal_template,R.drawable.pic4),

//	CLEAN("Ancient", R.drawable.backpdf)
}

data class TemplateItem(val templateItem: PdfTemplateDRW, val selected: Boolean = false)



data class EstimatePDFData(
	val estimateId: Int,
	val customerId: Int,
	val customerName: String,
	val customerPhone: String?,
	val estimateTitle: String,
	val estimateDate: String,
	val dueDate: String,
	val items: List<ModelClass>,
	val subtotal: Double,
	val taxTotal: Double,
	val grandTotal: Double
)
enum class CompanyDetail(val text: String) {
	COMPANY_NAME_1("INFIELD"),
	COMPANY_NAME_2("ENGINEERING"),
	QUOTE_NO("QUOTE NO:"),
	QUOTE_TO("INFIELD-ER:"),
	DATE("DATE:"),
	QUOTE_FOR("FOR:"),
	QUOTE_HEADER("QUOTATION");
}


