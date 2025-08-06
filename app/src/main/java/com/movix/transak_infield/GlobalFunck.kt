package com.movix.transak_infield

import android.app.DatePickerDialog
import android.content.Context
import android.icu.util.Calendar
import android.icu.util.GregorianCalendar
import android.os.Build
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal
import  com.movix.transak_infield.InvoiceInfo

class GlobalFunck {
	//    method to sum list in items
	fun getSubTotal(context: Context): Float {
		var totalSum = 0F
		val db = DatabaseHandler(context).readableDatabase
		val cursor = db.rawQuery("SELECT item_total FROM TableInvoice", null)
		if (cursor.moveToFirst()) {
			do {
				val total = cursor.getFloat(cursor.getColumnIndexOrThrow("item_total"))
				totalSum += total
			} while (cursor.moveToNext())

		}
		cursor.close()
		db.close()
		return totalSum
	}

	//        an extension function to the global functions
	fun summationOfTax(context: Context): Double {
		var cumulativeTax = 0.0
		val dbView = DatabaseHandler(context)
		var viewitem = dbView.viewProduct()

		viewitem.forEachIndexed { _, items ->

			val price = items.price
			val quantitty = items.quantity
			val taxedIndex = items.tax
			val nontax = price * quantitty
			// Multiply amount by taxed index
			val taxeditemprice = (nontax * taxedIndex * 0.01)
			//add vat data dynamically as they are entered in the table
			cumulativeTax += taxeditemprice
			println("test for cummulative total tax is $cumulativeTax")

		}
		return cumulativeTax
	}


	fun summationofTotal(context: Context): Float {
//get the summation of total plus the taxed =(taxed or non-taxed)
		val sumTax = summationOfTax(context).toFloat()
		var summedTotal = 0.00f
		var subtotalAmount = getSubTotal(context)
		println("this is subtotal $subtotalAmount")
		subtotalAmount += sumTax
//            add the collected tax total to the initial total
		summedTotal += subtotalAmount

		// total plus the taxed amount is returned for use
		return summedTotal
	}
// function to collect  name title estimate id

//function of databaseviewing
	fun creationDate(context: Context): String {
		val dbEstimateinfo = DatabaseHandler(context).viewEstimateInfo()
		var string: String = ""
		dbEstimateinfo.forEachIndexed { index, title ->
			string = title.creationDate
		}
		return string
	}

	fun dueDate(context: Context): String {
		val dbEstimateinfo = DatabaseHandler(context).viewEstimateInfo()
		var string: String = ""
		dbEstimateinfo.forEachIndexed { index, title ->
			title.dueDate
		}
		return string
	}

	fun titleINV(context: Context): String {
		val dbEstimateinfo = DatabaseHandler(context).viewEstimateInfo()
		var string: String = ""
		dbEstimateinfo.forEachIndexed { index, title ->

			string = title.titleINV
		}
		return string
	}

	fun id(context: Context): Int {
		val dbEstimateinfo = DatabaseHandler(context).viewEstimateInfo()
		var int: Int = 0

		dbEstimateinfo.forEachIndexed { index, title ->

			int = title.id
		}
		return int

	}

	fun customerId(context: Context): Int {
		val db = DatabaseHandler(context)
		val estimates = db.viewEstimateInfo()

		// Return the customerId of the most recent estimate if available
		return if (estimates.isNotEmpty()) {
			estimates.last().customerId
		} else {
			0 // Default if none exists
		}
	}

	fun customerName(context: Context): String {
		val db = DatabaseHandler(context)
		val customers = db.viewClientsInfo()
		var string=""
		customers.forEachIndexed{index,creation ->
			string=creation.name
		}

	 return string
	}


	//show date picker and utilize the return string
	@RequiresApi(Build.VERSION_CODES.O)
	fun showDatePickerDialog(context: Context, onDateSelection: (LocalDate) ->Unit){
		var dateReturn: Temporal

		val calender = GregorianCalendar()
		val year = calender.get(Calendar.YEAR)
		val month = calender.get(Calendar.MONTH)
		val day = calender.get(Calendar.DATE)

		val datePickerDialog = DatePickerDialog(
			context, { _, selectedYear, selectedMonth, selectedDate ->
				val datelist = listOf(selectedYear, selectedMonth.plus(1), selectedDate)
				dateReturn = LocalDate.of(datelist[0], datelist[1], datelist[2])

			}, year, month, day
		)
		//to restrict future dates(eg for D.O.B)
//			datePickerDialog.datePicker.maxDate=System.currentTimeMillis()


		datePickerDialog.setOnDateSetListener { view, year, month, dayOfMonth ->
			dateReturn = LocalDate.of(year, month + 1, dayOfMonth)
			println("may be this $dateReturn")
			val capturedDate: LocalDate = LocalDate.from(dateReturn)
// You can push them direct to where you want to use it or
//			to use them elsewhere in the app like this way
			onDateSelection(capturedDate)// <- call back with a result

		}

		datePickerDialog.show()

	}


	// spinner format for the terms
	fun spinner(
		context: Context, spinner: Spinner, items: List<String> = listOf(
			"select options", "1 week", "2 weeks", "1 month", "3 months"
		), onItemSelected: (String) -> Unit
	) {

		// create a default array adapter
		val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items)
		adapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
		spinner.adapter = adapter
		spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
			override fun onItemSelected(
				parent: AdapterView<*>?, view: View?, position: Int, id: Long
			) {

				val selctedItems = items[position]
				onItemSelected(selctedItems)


			}

			override fun onNothingSelected(parent: AdapterView<*>?) {
				TODO("Not yet implemented")
			}
		}

	}

	//itemselected

	fun selectionterms(dueTerms: Int): Int {
		return when (dueTerms) {
			1 -> 7
			2 -> 14
			3 -> 30
			4 -> 90
			else -> 2 // Default
		}
	}


	//calculate date for save operations
	@RequiresApi(Build.VERSION_CODES.O)
	fun calculateDueDate(baseDate:LocalDate, dueTerms: Int): LocalDate? {
		val days=selectionterms(dueTerms)
		return baseDate.plusDays(days.toLong())
	}
}