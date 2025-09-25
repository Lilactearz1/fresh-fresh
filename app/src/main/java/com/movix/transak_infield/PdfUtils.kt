package com.movix.transak_infield

import android.content.Context
import com.movix.transak_infield.MainActivity

object PdfUtils {

	fun generateEstimatePdf(context: Context) {
		try {
			// Call the MainActivity companion object function
			MainActivity.estimatePdf(context)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}
}
