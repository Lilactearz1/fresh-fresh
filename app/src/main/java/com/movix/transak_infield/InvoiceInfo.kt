package com.movix.transak_infield

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.movix.transak_infield.R.id.*
import com.movix.transak_infield.databinding.FragmentInvoiceInfoBinding
import java.io.IOException
import java.time.LocalDate
import kotlin.properties.Delegates

// UI component declarations (some are lateinit, others use Delegates)
private lateinit var invoiceNumber: EditText
private lateinit var creationDate: TextView
private lateinit var spinnerTerms: Spinner
private lateinit var invoiceTitle: EditText
private lateinit var imageCalendar:TextView
private var estimateId: Int = 0


  var dueTerms: Int = 14

class InvoiceInfo : Fragment() {
	// ViewBinding to access layout views
	private var _binding: FragmentInvoiceInfoBinding? = null
	private val binding get() = _binding!!
	private var selectedCreationDate: LocalDate? = null


	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
	): View {
		// Inflate the view using view binding
		_binding = FragmentInvoiceInfoBinding.inflate(inflater, container, false)
		return binding.root
	}


	@RequiresApi(Build.VERSION_CODES.O)
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		estimateId = arguments?.getInt("estimate_id") ?: 0

		if (estimateId == 0) {
			Toast.makeText(requireContext(), "⚠️ Missing estimate ID", Toast.LENGTH_SHORT).show()
		}


		// Link UI elements to variables
		invoiceNumber = binding.addItemInvoicenumber
		creationDate = binding.pickcreationDate
		spinnerTerms = view.findViewById(add_spinnerterms)
		invoiceTitle = binding.addEdtInvoiceTitle
		imageCalendar=binding.imageCalendar


//		if (estimateId != 0){ updateEstimateDisplay(requireContext())}

		//handle the key enter and back issue for crush behavior on enter or back pressed
		GlobalFunck().setUpEnterKeyNavigation(invoiceNumber, invoiceTitle)

		// Handle DatePicker click to show a date picker dialog
	 	creationDate.setOnClickListener {
			invoiceNumber.imeOptions=EditorInfo.IME_ACTION_DONE
			invoiceTitle.imeOptions=EditorInfo.IME_ACTION_DONE
			invoiceNumber.clearFocus()
		try {

			GlobalFunck().showDatePickerDialog(requireContext()) { pickedDate ->
				selectedCreationDate= pickedDate

				println("printlnDate received: $selectedCreationDate")

				creationDate.text = "$selectedCreationDate"
				// You can store or display dateUp as needed
			}
		}catch (e:IOException){
			e.printStackTrace()
		}
		}

		imageCalendar .setOnClickListener{view->
		  creationDate.performClick()
		}
		// Setup spinner to select payment/due terms
		val creationSpinner = GlobalFunck().spinner(requireContext(), spinnerTerms) { selectedItems ->

			if (selectedItems.isNotEmpty()){
				for (item in selectedItems) {

				dueTerms = GlobalFunck().selectionterms(item.code)

					// You can now use dueTerms when saving to DB
				}
			}else{
				for (item in selectedItems){

					dueTerms=GlobalFunck().selectionterms(item.code)

				}


			}


		}

		// Handle Back button functionality to pop the fragment
		val backBtn = view.findViewById<RelativeLayout>(back)
		backBtn?.setOnClickListener {
			requireActivity().supportFragmentManager.popBackStack()
		}

		// Handle Save button functionality
		val saveBtn = view.findViewById<RelativeLayout>(Save)


		saveBtn.setOnClickListener {

			try {

			var title = invoiceTitle.text
				invoiceNumber.inputType = InputType.TYPE_NULL // Disable keyboard
				invoiceTitle.inputType=InputType.TYPE_NULL

			// If title and terms were filled, use selected values
			if (title.isNotEmpty() && creationSpinner.toString().isNotBlank()) {

				val pickedDate = selectedCreationDate ?: LocalDate.now()
				val dueDate = GlobalFunck().calculateDueDate(pickedDate, dueTerms)

				// Save to DB here
				val db = DatabaseHandler(requireContext())
				val customerId=GlobalFunck().customerId(requireContext())

				val estimateInfo = Estimateinfo(titleINV = invoiceTitle.text.toString(),
					creationDate = pickedDate.toString(), dueDate = dueDate.toString()
				)

				if (estimateId != 0) {
					db.updateEstimateInfo(
					estimateinfo = estimateInfo
					)

					Toast.makeText(requireContext(), "Estimate updated successfully", Toast.LENGTH_SHORT).show()
				} else {
					val newId = db.addEstimateInfo(estimateInfo)
					EstimateSession.saveSession(requireContext(), newId.toInt())
					Toast.makeText(requireContext(), "New estimate created", Toast.LENGTH_SHORT).show()
				}


				Toast.makeText(context,"Activity is done",Toast.LENGTH_LONG).show()
				invoiceTitle.text.clear()


			} else {
				invoiceNumber.inputType = InputType.TYPE_NULL // Disable keyboard
				invoiceTitle.inputType=InputType.TYPE_NULL
				// Use default values if title or terms are missing
				val now = LocalDate.now()
				val dueDate = LocalDate.of(
					now.year, now.month, now.dayOfMonth).plusDays(14)

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

			requireActivity().supportFragmentManager.popBackStack()
			}catch (e:IOException){
				e.printStackTrace()
			}
		}
	}
	fun updateEstimateDisplay(context: Context) {

		val db: ArrayList<Estimateinfo> =DatabaseHandler(context).viewEstimateInfo()

		for (it in db){
			creationDate.setText(it.creationDate)
			invoiceTitle.setText(it.titleINV)

			it.status.name
		}
	}


}