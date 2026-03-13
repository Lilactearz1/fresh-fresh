package com.movix.transak_infield
// TODO: this fragment is responsible for
//  inputting clients information's the name phone number and emails

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.movix.transak_infield.MainActivity.Companion.EXTRA_CUSTOMER_ID
import com.movix.transak_infield.MainActivity.Companion.EXTRA_ESTIMATE_ID
import com.movix.transak_infield.MainActivity.Companion.requireEstimateId
import com.movix.transak_infield.databinding.FragmentClientInfoBinding
import org.bouncycastle.util.Integers

class clientinfoFragment : Fragment() {

    private lateinit var name: EditText
    private lateinit var phoneNumber: EditText
    private lateinit var emailAddress: EditText
    private var _binding: FragmentClientInfoBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: DatabaseHandler
    private  var estimateId:Int=-1
    private var customerId:Int=-1

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


        estimateId = arguments?.getInt(EXTRA_ESTIMATE_ID) ?: -1
        customerId = arguments?.getInt(EXTRA_CUSTOMER_ID) ?: -1


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
        val addDetail = ClientsCreation(customerId,name = clientName, phone = clientPhone)

        // Insert into DB
        val newCustomerId = db.addClientsInformations(addDetail).toInt()
        if (newCustomerId > -1) {
            Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()

            name.text.clear()
            phoneNumber.text.clear()
            emailAddress.text.clear()

            if (estimateId != -1) {
                val updated = db.updateEstimateCustomer(estimateId, newCustomerId)
                if (updated){
                    Toast.makeText(requireContext(),"updated", Toast.LENGTH_SHORT).show()
                }
            }

            // 🔹 refresh RecyclerView in ClientActivity
            (requireActivity() as? ClientActivity)?.refreshClients()

            // close fragment
            requireActivity().supportFragmentManager.popBackStack()
        }else {
            Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
        }
    }
}