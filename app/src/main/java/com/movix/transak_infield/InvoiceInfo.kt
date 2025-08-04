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
import com.movix.transak_infield.R.id.*
import com.movix.transak_infield.databinding.FragmentInvoiceInfoBinding
import java.time.LocalDateTime
import java.time.temporal.Temporal
import kotlin.properties.Delegates

// UI component declarations (some are lateinit, others use Delegates)
private lateinit var invoiceNumber: EditText
private lateinit var creationDate: DatePicker
private lateinit var spinnerTerms: Spinner
lateinit var invoiceTitle: EditText
private var dueTerms by Delegates.notNull<Int>()

class InvoiceInfo : Fragment() {
	// ViewBinding to access layout views
	private var _binding: FragmentInvoiceInfoBinding? = null
	private val binding get() = _binding!!

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		// You can initialize arguments or logic here if needed
	}

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
	): View? {
		// Inflate the view using view binding
		_binding = FragmentInvoiceInfoBinding.inflate(inflater, container, false)
		return binding.root
	}

	@RequiresApi(Build.VERSION_CODES.O)
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		// Link UI elements to variables
		invoiceNumber = binding.addItemInvoicenumber
		creationDate = binding.pickcreationDate
		spinnerTerms = view.findViewById(add_spinnerterms)
		invoiceTitle = binding.addEdtInvoiceTitle

		// Handle DatePicker click to show a date picker dialog
		creationDate.setOnClickListener {
			GlobalFunck().showDatePickerDialog(requireContext()) { pickedDate ->
				val dateUp: Temporal = pickedDate
				println("Date received: $dateUp")
				// You can store or display dateUp as needed
			}
		}

		// Setup spinner to select payment/due terms
		val creationSpinner = GlobalFunck().spinner(requireContext(), spinnerTerms) { selectedItems ->
			for (item in selectedItems) {
				dueTerms = GlobalFunck().selectionterms(item.code)
				// You can now use dueTerms when saving to DB
			}
		}

		// Handle Back button functionality to pop the fragment
		val backBtn = view.findViewById<LinearLayout>(back)
		backBtn.setOnClickListener {
			requireActivity().supportFragmentManager.popBackStack()
		}

		// Handle Save button functionality
		val saveBtn = view.findViewById<RelativeLayout>(Save)
		saveBtn.setOnClickListener {
			val title = invoiceTitle.text

			// If title and terms were filled, use selected values
			if (title.isNotEmpty() && creationSpinner.toString().isNotBlank()) {
				// Call method to calculate due date and store it
				val calculatedDate = GlobalFunck().dueDateCalculator(requireContext(), dueTerms)
				calculatedDate // (Optional: do something with this date)
			} else {
				// Use default values if title or terms are missing
				val now = LocalDateTime.now()
				val dueDate = LocalDateTime.of(
					now.year, now.month, now.dayOfMonth.plus(14), now.hour, now.minute
				)
				val defaultNumbering = "INFIELDER CLIENT OF DATE $now"
				val db = DatabaseHandler(requireContext())
				val customerId = GlobalFunck().customerId(requireContext())

				// Insert estimate info into database
				db.addEstimateInfo(
					estimateinfo = Estimateinfo(
						0, defaultNumbering, "$now", dueDate.toString(), customerId
					)
				)
			}
		}
	}
}
