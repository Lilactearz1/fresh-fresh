package com.movix.transak_infield

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.movix.transak_infield.databinding.ActivityMainBinding
import com.movix.transak_infield.MainActivity

class CustomerItems : AppCompatActivity() {
	private lateinit var _binding: ActivityMainBinding
	private val binding get() = _binding
	private lateinit var itemsCount: TextView
	private lateinit var subtotal: GlobalFunck
	private lateinit var total: GlobalFunck
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()

		// todo  Inflate binding
		_binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		val db = DatabaseHandler(applicationContext)

		val customerId = intent.getIntExtra("customer_id", -1)
		if (customerId != -1) {
			val items = db.getItemsForCustomer(customerId = customerId)
			val adapter = ItemAdapter(this, items)
			binding.recycleItem.layoutManager = LinearLayoutManager(this)
			binding.recycleItem.adapter = adapter
		}
		println("data time 0")




		fun updateDialog(modelClass: ModelClass) {
			// TODO(Not Implemented)
			// Create the Dialog
			val dialog = AlertDialog.Builder(this).setTitle("update items info")
				.setView(R.layout.edititem_dialog)
				.setCancelable(false)   //will not allow user to cancel after
				.create()
//        now show the dialog
			dialog.show()

			// Find views from dialog layout
			val nameEditText = dialog.findViewById<EditText>(R.id.edit_item_namedialog)
			val quantityEditText = dialog.findViewById<EditText>(R.id.edit_item_quantity)
			val priceEditText = dialog.findViewById<EditText>(R.id.edit_item_pricedialog)
			val taxRateEditText = dialog.findViewById<EditText>(R.id.edit_item_taxRatedialog)
			val saveButton = dialog.findViewById<Button>(R.id.SaveclientDetails)
			val cancelButton = dialog.findViewById<Button>(R.id.backLayout)

			// Fill views with existing data
			nameEditText?.setText(modelClass.itemName)
			quantityEditText?.setText(modelClass.quantity.toString())
			priceEditText?.setText(modelClass.price.toString())
			taxRateEditText?.setText(modelClass.tax.toString())
			//cancellation button
			cancelButton?.setOnClickListener {
				dialog.dismiss()
			}


			// Save/update action
			saveButton?.setOnClickListener {
				val updatedName = nameEditText?.text.toString()
				val updatedQuantity = quantityEditText?.text.toString().toIntOrNull()
				val updatedPrice = priceEditText?.text.toString().toDoubleOrNull()
				val updatedTax = taxRateEditText?.text.toString().toFloatOrNull()

				if (updatedName.isBlank() || updatedQuantity == null || updatedPrice == null || updatedTax == null) {
					Toast.makeText(this, "All fields must be valid", Toast.LENGTH_SHORT).show()
					return@setOnClickListener
				}

				val updatedModel = ModelClass(
					id = modelClass.id, // keep same ID
					itemName = updatedName,
					quantity = updatedQuantity,
					price = updatedPrice,
					total = (updatedQuantity * updatedPrice).toFloat(),
					tax = updatedTax,
				)

				val db = DatabaseHandler(this)
				// define this method in your DB handler (updateRecords)
				val status = db.updateRecords(updatedModel)

				if (status > -1) {
					Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()
					dialog.dismiss()
					// Optional: refresh RecyclerView
					setupListintoRecycleview()
				} else {
					Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
				}
			}

			dialog.show()
		}


//set the text view to
		itemsCount = binding.tvItems
		//	    function to count items number inserted

		fun countItems() {
			val counteditems = setupListintoRecycleview()
			itemsCount.text = "Items No: [${counteditems}]"
		}
		countItems()


//
//		// function to display totals in the textview
//		fun txtViewTotals() {
//			val stringFomat = "%,.2f"
//			val itemSubtal = binding.esumSubtotal
//			val itemsTotal = binding.esumTotal
//			subtotal = GlobalFunck()
//			val stringSubtotal =
//				stringFomat.format(Math.floor(subtotal.getSubTotal(applicationContext).toDouble()))
//			itemSubtal.text = stringSubtotal
//
//			total = GlobalFunck()
//			val stringTotal = stringFomat.format(
//				Math.floor(
//					total.summationofTotal(applicationContext).toDouble()
//				)
//			)
//			itemsTotal.text = stringTotal
//		}


	}

	//function to show list of inserted data in the recycler view
	private fun setupListintoRecycleview(): Int {
		val itemList = getItemlist()

		if (itemList.isNotEmpty()) {
			binding.recycleItem.visibility = View.VISIBLE
			binding.recycleItem.layoutManager = LinearLayoutManager(this) // ✅ OK in Activity

			val itemAdapter = ItemAdapter(this, itemList) // ✅ 'this' = Activity context
			binding.recycleItem.adapter = itemAdapter
		} else {
			binding.recycleItem.visibility = View.GONE
		}
//	    method to count the children
		val countItems = ItemAdapter(applicationContext, itemList)

		return countItems.itemCount
	}

	private fun getItemlist(): ArrayList<ModelClass> {
		val db = DatabaseHandler(this).viewProduct()
		return db
	}
}