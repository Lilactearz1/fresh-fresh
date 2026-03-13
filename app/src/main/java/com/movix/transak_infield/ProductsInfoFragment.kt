package com.movix.transak_infield

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationBarView
import com.movix.transak_infield.MainActivity.Companion.EXTRA_CUSTOMER_ID
import com.movix.transak_infield.MainActivity.Companion.EXTRA_ESTIMATE_ID
import com.movix.transak_infield.databinding.FragmentProductsInfoBinding
import kotlin.properties.Delegates

class ProductsInfoFragment : Fragment() {

    private var _binding: FragmentProductsInfoBinding? = null
    private val binding get() = _binding!!

    private lateinit var itemDescription: EditText
    private lateinit var itemQuantity: EditText
    private lateinit var itemPrice: EditText
    private lateinit var itemTaxrate: EditText
    private lateinit var amountScreen: TextView
    private lateinit var backbutton: LinearLayout
    private lateinit var btnSave: LinearLayout

    private var productAmount by Delegates.notNull<Float>()
    private lateinit var db: DatabaseHandler
    private var estimateId: Int = -1
    private var customerId: Int = -1
    internal var stringFormat = "%,.2f"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductsInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        estimateId = arguments?.getInt(EXTRA_ESTIMATE_ID, -1) ?: -1
        customerId = arguments?.getInt(EXTRA_CUSTOMER_ID, 0) ?: -1

        itemDescription = binding.addItemName
        itemQuantity = binding.addItemQuantity
        itemPrice = binding.addItemPrice
        itemTaxrate = binding.addItemTaxRate
        amountScreen = view.findViewById(R.id.tv_amount_display_value)
        btnSave = view.findViewById<LinearLayout>(R.id.saveF1)
        backbutton = view.findViewById<LinearLayout>(R.id.backF1)

        db = DatabaseHandler(requireContext())

        setupAmountWatcher()

        // Save button
        btnSave.setOnClickListener {
            saveProduct()
        }

        // Back button
        backbutton.setOnClickListener {
            // Refresh the parent activity before closing fragment
            (requireActivity() as? CustomerItems)?.refreshItems()
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupAmountWatcher() {
        itemPrice.doOnTextChanged { _, _, _, _ -> updateAmount() }
        itemQuantity.doOnTextChanged { _, _, _, _ -> updateAmount() }
    }

    private fun updateAmount() {
        val price = itemPrice.text.toString().toDoubleOrNull() ?: 0.0
        val quantity = itemQuantity.text.toString().toIntOrNull() ?: 0
        val amount = price * quantity
        amountScreen.text = stringFormat.format(amount)
        productAmount = amount.toFloat()
    }

    private fun saveProduct() {
        val description = itemDescription.text.toString().trim()
        val quantity = itemQuantity.text.toString().toIntOrNull()
        val price = itemPrice.text.toString().toDoubleOrNull()
        val tax = itemTaxrate.text.toString().toFloatOrNull()

        if (description.isEmpty() || quantity == null || price == null || tax == null) {
            Toast.makeText(requireContext(), "Please fill all fields correctly", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val product = ModelClass(
            id = 0,
            quantity = quantity,
            itemName = description,
            price = price,
            total = (quantity * price).toFloat(),
            tax = tax,
            customerId = customerId,
            estimateId = estimateId
        )

        val status = db.addProductToDatabase(product)

        if (status > -1) {
            Toast.makeText(requireContext(), "Product saved", Toast.LENGTH_SHORT).show()

            // Clear input fields
            itemDescription.text.clear()
            itemQuantity.text.clear()
            itemPrice.text.clear()
            itemTaxrate.text.clear()
            amountScreen.text = "0.00"

            // ✅ Immediately refresh parent activity RecyclerView
            (requireActivity() as? CustomerItems)?.refreshItems()
        } else {
            Toast.makeText(requireContext(), "Save failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}