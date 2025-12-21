package com.movix.transak_infield.ui.theme


import android.content.Context
import com.itextpdf.layout.Document
import com.movix.transak_infield.EstimatePDFData
import com.movix.transak_infield.ModelClass
import com.movix.transak_infield.TemplateLayout

interface TemplateInterface {

	fun drawHeader(
		document: Document, layout: TemplateLayout, dataEst: EstimatePDFData, context: Context
	)

	fun drawFooter(
		document: Document, layout: TemplateLayout, dataEst: EstimatePDFData, context: Context
	)

	fun drawTable(
		document: Document, layout: TemplateLayout, dataEst: EstimatePDFData, context: Context
	)
}
