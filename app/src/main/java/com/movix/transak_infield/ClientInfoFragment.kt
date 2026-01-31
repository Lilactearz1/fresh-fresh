package com.movix.transak_infield

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.movix.transak_infield.databinding.FragmentClientInfoBinding

class clientinfoFragment : Fragment() {

    private lateinit var name: EditText
    private lateinit var phoneNumber: EditText
    private lateinit var emailAddress: EditText
    private var _binding: FragmentClientInfoBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: DatabaseHandler

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DatabaseHandler(requireContext())

        name = binding.editClientName
        phoneNumber = binding.editPhone
        emailAddress = binding.editEmail

        val savebtn = view.findViewById<RelativeLayout>(R.id.SaveclientDetails)
        val backButton = view.findViewById<RelativeLayout>(R.id.backLayout)

        // Back button
        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Save button
        savebtn.setOnClickListener {
            clientInput()
        }
    }

    private fun clientInput() {
        val clientName = name.text.toString().trim()
        val clientPhone = phoneNumber.text.toString().trim()
        val clientMail = emailAddress.text.toString().trim()

        if (clientName.isEmpty()) {
            name.error = "Name is required"
            return
        }

        // Create client object
        val addDetail = ClientsCreation(name = clientName, phone = clientPhone)

        // Insert into DB
        val newCustomerId = db.addClientsInformations(addDetail).toInt()

        if (newCustomerId > -1) {
            Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()

            // Clear inputs
            name.text.clear()
            phoneNumber.text.clear()
            emailAddress.text.clear()

            // -  Update estimate with this customer ---
            val estimateId =
                activity?.intent?.getIntExtra(MainActivity.EXTRA_ESTIMATE_ID, -1) ?: -1

            if (estimateId != -1) {
                val updated = db.updateEstimateCustomer(estimateId, newCustomerId)
                if (updated) {
                    Toast.makeText(requireContext(), "Estimate updated with new customer", Toast.LENGTH_SHORT).show()
                }
            }

            // Close fragment
            requireActivity().supportFragmentManager.popBackStack()
        } else {
            Toast.makeText(requireContext(), "Failed to save", Toast.LENGTH_SHORT).show()
        }
    }
}