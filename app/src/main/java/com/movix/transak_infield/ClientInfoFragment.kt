package com.movix.transak_infield

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.lifecycle.lifecycleScope

import com.movix.transak_infield.databinding.FragmentClientInfoBinding
import java.lang.Exception
import com.movix.transak_infield.R.id
import kotlinx.coroutines.async


/**
 * A simple [Fragment] subclass.
 * Use the [clientinfoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class clientinfoFragment : Fragment() {
	//todo collect the editext field and save them to the database wher they are accessed for creation of the estimates
	private lateinit var name: EditText
	private lateinit var phoneNumber: EditText
	private lateinit var emailAddress: EditText
	private var _binding: FragmentClientInfoBinding? = null
	private val binding get() = _binding!!


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

	}

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment
		_binding = FragmentClientInfoBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		name = binding.editClientName
		phoneNumber = binding.editPhone
		emailAddress = binding.editEmail

		val savebtn=view.findViewById<RelativeLayout>(R.id.SaveclientDetails)

		val backButton = view.findViewById<RelativeLayout>(R.id.backLayout)
/// Set click listener for the back button
		backButton.setOnClickListener { view ->
			// When the back button is pressed, go back to the previous fragment in the back stack
			requireActivity().supportFragmentManager.popBackStack()
		}

		fun clientInput(view: View) {
			// Get and trim text input from fields
			val clientName = name.text.toString().trim()
			val clientPhone = phoneNumber.text.toString().trim()
			val clientMail = emailAddress.text.toString().trim()

			// Validate that the name field is not empty (required)
			if (clientName.isEmpty()) {
				name.error = "Name is required"
				return
			}

			// Create the client object (modify constructor if email should be included)
			val addDetail = ClientsCreation(0, clientName, clientPhone /* , clientMail if needed */)

			// Save the client details to the database
			val dbStatus = DatabaseHandler(requireContext()).addClientsInformations(addDetail)

			// If insert was successful, show confirmation and clear fields
			if (dbStatus > -1) {
				Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
				name.text.clear()
				phoneNumber.text.clear()
				emailAddress.text.clear()
			} else {
				// If insert failed, notify the user
				Toast.makeText(requireContext(), "Failed to save", Toast.LENGTH_SHORT).show()
			}
		}


//		button to save the inputs
		savebtn.setOnClickListener{view ->

		  clientInput(view)
		}
	}

}