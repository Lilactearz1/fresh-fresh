package com.movix.transak_infield

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import androidx.annotation.RequiresApi

//away to avoid using the Rid is to import them ids and you can import the colors too
import com.movix.transak_infield.R.id.*


import com.movix.transak_infield.databinding.FragmentInvoiceInfoBinding
import kotlin.properties.Delegates

private lateinit var invoiceNumber: EditText
private lateinit var creationDate: DatePicker
private lateinit var spinnerTerms: Spinner
private lateinit var invoiceTitle: EditText
private var dueTerms by Delegates.notNull<Int>()


class InvoiceInfo : Fragment() {
	private var _binding: FragmentInvoiceInfoBinding? = null
	private val binding get() = _binding!!

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

	}

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
	): View? {
		_binding = FragmentInvoiceInfoBinding.inflate(inflater, container, false)
		return binding.root

	}

	@RequiresApi(Build.VERSION_CODES.O)
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		invoiceNumber = binding.addItemInvoicenumber
		creationDate = binding.pickcreationDate
		spinnerTerms = view.findViewById(add_spinnerterms)
		invoiceTitle = binding.addEdtInvoiceTitle

//


		val datePicker = creationDate.setOnClickListener { View ->
			val datePicker = GlobalFunck().showDatePickerDialog(requireContext())

			println("we are right $datePicker")

		}


// call the method of the spinner to work out
	val creationSpinner=	GlobalFunck().spinner(requireContext(), spinnerTerms) { selectedItem ->

			for (items in selectedItem) {
				dueTerms = GlobalFunck().selectionterms(items.code).code
				// use the due terms to insert terms into the database

			}

		}

//		after importing the package you just call them directly

		val saveBtn = view.findViewById<RelativeLayout>(Save)
		val backBtn = view.findViewById<LinearLayout>(back)

		saveBtn?.setOnClickListener { Save ->
			val title = invoiceTitle.text

			if (title.isNotEmpty() && creationSpinner.toString().isNotBlank()) {
				val defaultNumbering = title.toString()
			    val datepicked =GlobalFunck().showDatePickerDialog(requireContext()).toString()
				val datCalculated=GlobalFunck().dueDateCalculator(requireContext(), dueTerms)
				val db = DatabaseHandler(requireContext())
				println("picked $datepicked  calculated $datCalculated")
				db.addEstimateInfo(
					estimateinfo = Estimateinfo(
						0, defaultNumbering, datepicked, datCalculated, 0
					)
				)
			} else {
				val date = "11/22/2024"
				val defaultNumbering = "INFIELDER CLIENT of DATE $date"
				val db = DatabaseHandler(requireContext())
				db.addEstimateInfo(
					estimateinfo = Estimateinfo(
						0, defaultNumbering, "11-24-2025", "12-12-2015", 0
					)
				)
			}
		}


	}


}