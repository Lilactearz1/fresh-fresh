package com.movix.transak_infield

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.savedstate.SavedState
import com.google.android.material.navigation.NavigationBarItemView
import com.google.android.material.navigation.NavigationBarMenu
import com.google.android.material.navigation.NavigationBarView
import com.movix.transak_infield.databinding.FragmentProductsInfoBinding

/**
 * A simple [Fragment] subclass.

 */
class ProductsInfoFragment : Fragment() {

    private var _binding: FragmentProductsInfoBinding? = null
    private val binding get() = _binding!!
    private lateinit var itemDescription: EditText
    private lateinit var itemQuantity: EditText
    private lateinit var itemPrice: EditText
    private lateinit var itemTaxrate: EditText
    private lateinit var amountScreen: TextView
    private  lateinit var  navigationBar:NavigationBarView



//   private lateinit var databaseHandler:DatabaseHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductsInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize database
//         databaseHandler = DatabaseHandler(requireContext())
        // Now you can use binding to access EditText and Button
        itemDescription = binding.addItemName
        itemQuantity = binding.addItemQuantity
        itemPrice = binding.addItemPrice
        itemTaxrate = binding.addItemTaxRate
        amountScreen=view.findViewById(R.id.tv_amount_display_value)



        val backImagebtn = binding.button1
        val savebtn = binding.btnsave

        savebtn.setOnClickListener {

            addtoRecords(view) }

        backImagebtn.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()


            Toast.makeText(requireContext(),"clicked",Toast.LENGTH_SHORT).show()

        }

    }



//    method for saving records to the database

    private fun addtoRecords(view: View) {

        val productDescription = try {
            itemDescription.text.toString()

        } catch (e: NumberFormatException) {
            itemDescription.error = "input field"
            return
        }

        val productQuantity = try {
            itemQuantity.text.toString().toInt()
        } catch (e: NumberFormatException) {
            itemQuantity.error = "input field"
            return
        }

        val productPrice = try {
            itemPrice.text.toString().toDouble()
        } catch (e: NumberFormatException) {
            itemPrice.error = "input field"
            return
        }

        val productTax = try {
            itemTaxrate.text.toString().toFloat()
        } catch (e: NumberFormatException) {
            itemTaxrate.error = "input field"
            return
        }
        val productAmount:Float = try {

       (itemPrice.text.toString().toFloat()*itemQuantity.text.toString().toFloat())
        }catch (e: NumberFormatException){
            itemQuantity.error = "inputfield"
            itemPrice.error = "input field"
            return
        }


//    create a model instance to use in the database
        val databaseHandler: DatabaseHandler = DatabaseHandler(requireContext())

        val products = ModelClass(0, productQuantity, productDescription, productPrice,productAmount,productTax)

        if (productDescription.isNotEmpty() && productQuantity.toString()
                .isNotEmpty() && productPrice.toString().isNotEmpty() && productTax.toString()
                .isNotEmpty()
        ) {
            val status = databaseHandler.addProductToDatabase(products)

            if (status > -1) {
                Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()

                //
                //optionally  clear input fields
                itemDescription.text.clear()
                itemPrice.text.clear()
                itemQuantity.text.clear()
                itemTaxrate.text.clear()

//                after all the inputs are cleared the screen to read 0.00 fo r amount
                amountScreen.text = "0.00"

// call the method that add adds list into the recyclerview

            }
        }

    }


}