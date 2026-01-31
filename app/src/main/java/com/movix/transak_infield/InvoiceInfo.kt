package com.movix.transak_infield

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.InputType
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
import androidx.fragment.app.Fragment
import com.movix.transak_infield.R.id.Save
import com.movix.transak_infield.R.id.add_spinnerterms
import com.movix.transak_infield.R.id.back
import com.movix.transak_infield.databinding.FragmentInvoiceInfoBinding
import java.io.IOException
import java.time.LocalDate

// UI component declarations (some are lateinit, others use Delegates)
private lateinit var invoiceNumber: EditText
private lateinit var creationDate: TextView
private lateinit var spinnerTerms: Spinner
private lateinit var invoiceTitle: EditText
private lateinit var imageCalendar: TextView
private lateinit var db: DatabaseHandler
private var selectedCustomerId: Int = -1

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

        estimateId = arguments?.getInt(MainActivity.EXTRA_ESTIMATE_ID) ?: -1
        selectedCustomerId = arguments?.getInt(MainActivity.EXTRA_CUSTOMER_ID)?:-1

		db = DatabaseHandler(requireContext())
        if (selectedCustomerId <= 0) {
            Toast.makeText(requireContext(), "⚠️ Missing customer", Toast.LENGTH_SHORT).show()
        }

		if (estimateId == 0) {
			Toast.makeText(requireContext(), "⚠️ Missing estimate ID", Toast.LENGTH_SHORT).show()
		}

        // If editing existing estimate → load data
        if (estimateId > 0) {
            EstimateSession.loadSession(requireContext())
        }

		// Link UI elements to variables
		invoiceNumber = binding.addItemInvoicenumber
		creationDate = binding.pickcreationDate
		spinnerTerms = view.findViewById(add_spinnerterms)
		invoiceTitle = binding.addEdtInvoiceTitle
		imageCalendar = binding.imageCalendar


		if (estimateId != 0) {
			updateEstimateDisplay(requireContext())
		}

		//handle the key enter and back issue for crush behavior on enter or back pressed
		GlobalFunck().setUpEnterKeyNavigation(invoiceNumber, invoiceTitle)

		// Handle DatePicker click to show a date picker dialog
			creationDate.setOnClickListener {
			invoiceNumber.imeOptions = EditorInfo.IME_ACTION_DONE
			invoiceTitle.imeOptions = EditorInfo.IME_ACTION_DONE
			invoiceNumber.clearFocus()
			// set text for invoicenumber

			db.viewEstimateInfo().forEach { it ->
				invoiceNumber.setText("${it.estimateId}")
			}

			try {

				GlobalFunck().showDatePickerDialog(requireContext()) { pickedDate ->
					selectedCreationDate = pickedDate

					println("printlnDate received: $selectedCreationDate")

					creationDate.text = "$selectedCreationDate"
					// You can store or display dateUp as needed
				}
			} catch (e: IOException) {
				e.printStackTrace()
			}
		}

		imageCalendar.setOnClickListener { view ->
			creationDate.performClick()
		}
		// Setup spinner to select payment/due terms
		val creationSpinner =
			GlobalFunck().spinner(requireContext(), spinnerTerms) { selectedItems ->

				if (selectedItems.isNotEmpty()) {
					for (item in selectedItems) {

						dueTerms = GlobalFunck().selectionterms(item.code)

						// You can now use dueTerms when saving to DB
					}
				} else {
					for (item in selectedItems) {

						dueTerms = GlobalFunck().selectionterms(item.code)

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

				val title = invoiceTitle.text.toString().trim()
				invoiceNumber.inputType = InputType.TYPE_NULL // Disable keyboard
//				invoiceTitle.inputType=InputType.TYPE_NULL

				// If title and terms were filled, use selected values
				if (title.isNotEmpty() && creationSpinner.toString().isNotBlank()) {

					val pickedDate = selectedCreationDate ?: LocalDate.now()
					val dueDate = pickedDate.plusDays(dueTerms.toLong())

					// Save to DB here
					val db = DatabaseHandler(requireContext())
//				val customerId=GlobalFunck().customerId(requireContext())

					val estimateInfo = Estimateinfo(
						titleINV = title,
						creationDate = pickedDate.toString(),
						dueDate = dueDate.toString(),
                        customerId = selectedCustomerId
					)

					if (estimateId > 0) {
						val updatedEstimate = estimateInfo.copy(estimateId = estimateId)
						val rows = db.updateEstimateInfo(updatedEstimate)

						if (rows > 0) {
							Toast.makeText(
								requireContext(),
								"Estimate updated successfully",
								Toast.LENGTH_SHORT
							).show()
						} else {

							Toast.makeText(
								requireContext(),
								"No changes detected or update failed",
								Toast.LENGTH_SHORT
							).show()
						}
					}


				} else {
					invoiceNumber.inputType = InputType.TYPE_NULL // Disable keyboard
//				invoiceTitle.inputType=InputType.TYPE_NULL
					// Use default values if title or terms are missing
					val now = LocalDate.now()
					val dueDate = LocalDate.of(
						now.year, now.month, now.dayOfMonth
					).plusDays(14)




					val db = DatabaseHandler(requireContext())
					val customerId = selectedCustomerId

                    val estimate = Estimateinfo(
                        estimateId = estimateId,
                        titleINV = if (title.isNotEmpty()) title else "Untitled",
                        creationDate = selectedCreationDate.toString(),
                        dueDate = dueDate.toString(),
                        customerId = selectedCustomerId
                    )

					// Insert estimate info into database
					db.updateEstimateInfo(
						estimateinfo = estimate
					)
				}

				requireActivity().supportFragmentManager.popBackStack()
			} catch (e: IOException) {
				e.printStackTrace()
			}
		}
	}

    fun updateEstimateDisplay(context: Context) {
        val estimate = DatabaseHandler(context).getEstimateById(estimateId)

        estimate?.let {
            invoiceTitle.setText(it.titleINV ?: "")
            creationDate.text = it.creationDate
            binding.pickcreationDate.text = it.creationDate
            selectedCreationDate = LocalDate.parse(it.creationDate)
            selectedCustomerId = it.customerId?:-1
        }
    }


}