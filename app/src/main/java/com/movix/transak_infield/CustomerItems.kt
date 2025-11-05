package com.movix.transak_infield

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.movix.transak_infield.MainActivity.Companion.EXTRA_CUSTOMER_ID
import com.movix.transak_infield.MainActivity.Companion.EXTRA_ESTIMATE_ID
import com.movix.transak_infield.MainActivity.Companion.estimatePdf
import com.movix.transak_infield.MainActivity.Companion.handleBusinessImage
import com.movix.transak_infield.MainActivity.Companion.handleClientInfoClick
import com.movix.transak_infield.MainActivity.Companion.handleInvoiceButtonClick
import com.movix.transak_infield.MainActivity.Companion.handleItemsCardView
import com.movix.transak_infield.MainActivity.Companion.handleTemplateButtonClick
import com.movix.transak_infield.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CustomerItems : AppCompatActivity() {

	private lateinit var _binding: ActivityMainBinding
	private val binding get() = _binding

	private lateinit var itemsCount: TextView
	private lateinit var itemAdapter: ItemAdapter
	private var estimateId = -1
	private var customerId = -1
	private lateinit var items:ArrayList<ModelClass>
	private lateinit var refreshLauncher: ActivityResultLauncher<Intent>

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		_binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		refreshLauncher = registerForActivityResult(
			ActivityResultContracts.StartActivityForResult()
		) { result ->
			if (result.resultCode == Activity.RESULT_OK) {
				// ✅ Refresh RecyclerView
				setupRecyclerView()
			}
		}


		val rvl_bussinessInfo = binding.relativelayoutBussinessInfo
		val rvl_clientInfo = binding.relativeLayoutClentInfo

		val db = DatabaseHandler(applicationContext)

		estimateId = intent.getIntExtra("estimate_id", -1)
		customerId = intent.getIntExtra("customer_id", -1)
		itemsCount = binding.tvItems

		if (estimateId == -1 || customerId == -1) {
			Toast.makeText(this, "Invalid estimate or customer ID", Toast.LENGTH_SHORT).show()
			return
		}

		items = db.getItemsForEstimate(estimateId, customerId)

		setupRecyclerView()
		loadItems()
		enableSwipeToDelete()

		binding.btnInv001.setOnClickListener {
			handleInvoiceButtonClick(this, estimateId, customerId)
		}

		binding.btnTemplate.setOnClickListener {
			handleTemplateButtonClick(this)
		}

		binding.btnclientInfo.setOnClickListener {
			handleClientInfoClick(this)
		}

		binding.businessimage?.setOnClickListener {
			handleBusinessImage(this)
		}
		binding.additemscardView.setOnClickListener {
			handleItemsCardView(this,intent)
		}

		binding.addbuttonImage.setOnClickListener {
			handleItemsCardView(this,intent)
		}

		rvl_bussinessInfo?.setOnClickListener {
			binding.businessimage?.performClick()
		}

		rvl_clientInfo?.setOnClickListener {
			binding.btnclientInfo.performClick()
		}


		val bottomNav = binding.bottomnav
		bottomNav?.setContent {
			MaterialTheme {
				BottomBar()
			}
		}

	}


// the jetpack bottom appBar

	@Composable
	private fun BottomBar() {
		BottomAppBar(
			containerColor = Color(0, 142, 204), tonalElevation = 4.dp
		) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 12.dp),
				horizontalArrangement = Arrangement.SpaceEvenly,
				verticalAlignment = Alignment.CenterVertically
			) {
				// PREVIEW BUTTON
				TextButton(
					onClick = {
						lifecycleScope.launch {
							try {
								withContext(Dispatchers.IO) {
									val estimateId = intent.getIntExtra(EXTRA_ESTIMATE_ID, -1)
									val customerId = intent.getIntExtra(EXTRA_CUSTOMER_ID, -1)
									val pdfFile = estimatePdf(this@CustomerItems, estimateId, customerId)

									withContext(Dispatchers.Main) {
										PdfUtils.previewPdfFormat(this@CustomerItems, pdfFile)
									}
								}
							} catch (e: Exception) {
								e.printStackTrace()
								withContext(Dispatchers.Main) {
									Toast.makeText(
										this@CustomerItems,
										"Failed to open PDF: ${e.message}",
										Toast.LENGTH_SHORT
									).show()
								}
							}
						}
					},
					shape = RoundedCornerShape(50),
					colors = ButtonDefaults.textButtonColors(
						containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
						contentColor = MaterialTheme.colorScheme.onPrimary
					)
				) {
					Text(text = "Preview", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
				}


				// Save BUTTON
				TextButton(
					onClick = {
						lifecycleScope.launch(Dispatchers.IO) {
							try {
								val estimateId = intent.getIntExtra(EXTRA_ESTIMATE_ID, -1)
								val customerId = intent.getIntExtra(EXTRA_CUSTOMER_ID, -1)

								val newIntent = Intent(this@CustomerItems, Save_previewActivity::class.java).apply {
									putExtra(EXTRA_ESTIMATE_ID, estimateId)
									putExtra(EXTRA_CUSTOMER_ID, customerId)
								}

								withContext(Dispatchers.Main) {
									startActivity(newIntent)
									Toast.makeText(
										this@CustomerItems,
										"Opening save preview...",
										Toast.LENGTH_LONG
									).show()
								}
							} catch (e: Exception) {
								e.printStackTrace()
								withContext(Dispatchers.Main) {
									Toast.makeText(
										this@CustomerItems,
										"Failed to open Save Preview: ${e.message}",
										Toast.LENGTH_SHORT
									).show()
								}
							}
						}
					},
					shape = RoundedCornerShape(50),
					colors = ButtonDefaults.textButtonColors(
						containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
						contentColor = MaterialTheme.colorScheme.onPrimary
					)
				) {
					Text(text = "Save", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
				}

			}
		}
	}
	override fun onResume() {
		super.onResume()

		loadItems()

	}


	private fun setupRecyclerView() {
		binding.recycleItem.layoutManager = LinearLayoutManager(this)
		itemAdapter = ItemAdapter(this, items)
		binding.recycleItem.adapter = itemAdapter
	}

	private fun loadItems() {
		val db = DatabaseHandler(this)
		 items = db.getItemsForEstimate(estimateId, customerId)

		Log.d("CustomerItems", "Fetched items count: ${items.size}")
		db.estimated(applicationContext)

		if (items.isNotEmpty()) {
			binding.recycleItem.visibility = View.VISIBLE
			itemAdapter.itemList.clear()
			itemAdapter.itemList.addAll(items)
			itemAdapter.notifyDataSetChanged()
			itemsCount.text = "Items No: [${items.size}]"

		} else {
			binding.recycleItem.visibility = View.GONE
			itemsCount.text = "No items found"
		}
		refreshTotals()
	}

	private fun enableSwipeToDelete() {
		val itemTouchHelper = ItemTouchHelper(object :
			ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
			override fun onMove(
				recyclerView: RecyclerView,
				viewHolder: RecyclerView.ViewHolder,
				target: RecyclerView.ViewHolder
			): Boolean = false

			override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
				val position = viewHolder.adapterPosition
				val item = itemAdapter.itemList[position]
				val db = DatabaseHandler(this@CustomerItems)
				val deleted = db.deleteItem(item.id)

				if (deleted >0) {
					itemAdapter.itemList.removeAt(position)
					itemAdapter.notifyItemRemoved(position)
					Toast.makeText(applicationContext, "Item deleted", Toast.LENGTH_SHORT).show()
					refreshTotals()
				} else {
					Toast.makeText(applicationContext, "Delete failed", Toast.LENGTH_SHORT).show()
					itemAdapter.notifyItemChanged(position)
				}
			}
		})
		itemTouchHelper.attachToRecyclerView(binding.recycleItem)
	}

	private fun refreshTotals() {
		val subtotal = GlobalFunck().getSubTotal(applicationContext, estimateId)
		val tax = GlobalFunck().summationOfTax(applicationContext,estimateId)
		val grandTotal = subtotal + tax

		binding.esumSubtotal.text = "${String.format("%.2f", subtotal)}"
	//	binding.taxText.text = "Tax: ${String.format("%.2f", tax)}"
		binding.esumTotal.text = "${String.format("%.2f", grandTotal)}"
	}

	fun refreshItems() {
		val db = DatabaseHandler(this)
		val updatedItems = db.getItemsForEstimate(estimateId, customerId)

		items.clear()
		items.addAll(updatedItems)
		itemAdapter.notifyDataSetChanged()
		refreshTotals()

		if (updatedItems.isNotEmpty()) {
			binding.recycleItem.visibility = View.VISIBLE
			binding.tvItems.text = "Items No: [${updatedItems.size}]"
		} else {
			binding.recycleItem.visibility = View.GONE
			binding.tvItems.text = "No items found"
		}

	}

	// method to update the inputs in the dialog

	fun updateDialog(modelClass: ModelClass) {
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
		val saveButton = dialog.findViewById<RelativeLayout>(R.id.SaveclientDetails)
		val cancelButton = dialog.findViewById<RelativeLayout>(R.id.backLayout)
		val amountScreen = dialog.findViewById<TextView>(R.id.tv_amount_display_value)

		// Fill views with existing data
		nameEditText?.setText(modelClass.itemName)
		quantityEditText?.setText(modelClass.quantity.toString())
		priceEditText?.setText(modelClass.price.toString())
		taxRateEditText?.setText(modelClass.tax.toString())

		fun updateItems(price: Double, quantity: Int) {
			val total = price * quantity
//			display the live amount in the text view
			amountScreen?.text = "Ksh: $total"
		}

		// add tecxt watcher functions
		fun textChangerListener() {
			val price = priceEditText?.text.toString().toDoubleOrNull()
			val quantity = quantityEditText?.text.toString().toIntOrNull()

			if (price != null && quantity != null) {
				updateItems(price, quantity)
			}
		}
		textChangerListener()
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
				id = modelClass.id,
				quantity = updatedQuantity,
				itemName = updatedName,
				price = updatedPrice,
				total = (updatedQuantity * updatedPrice).toFloat(),
				tax = updatedTax,
				customerId = modelClass.customerId,
				estimateId = modelClass.estimateId
			)


			val db = DatabaseHandler(this)
			// define this method in your DB handler (updateRecords)
			val status = db.updateRecords(updatedModel)

			if (status > -1) {
				Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()
				dialog.dismiss()
				// Optional: refresh RecyclerView
				setupRecyclerView()
			} else {
				Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
			}
		}

		dialog.show()
	}

	fun deleteItems(modellist: ModelClass) {

		val builder = AlertDialog.Builder(this)
		builder.setTitle(R.string.delete)
//                set message for the alert dialog box
		builder.setMessage("Delete ${modellist.itemName}")
		builder.setIcon(R.drawable.baseline_crisis_alert_24)
//        perform the positive action
		builder.setPositiveButton("Yes") { dialogInterface, which ->
//                creating an instance of the database classs
			val databaseseHandler: DatabaseHandler = DatabaseHandler(this)
//                 call the delete method of the database
			val status = databaseseHandler.deleteRecords(
				ModelClass(
					modellist.id,
					modellist.quantity,
					"",
					modellist.price,
					modellist.total,
					modellist.tax,
					modellist.customerId,
					modellist.estimateId,

					)
			)
			if (status > -1) {
				Toast.makeText(this, "Deleted Successfully", Toast.LENGTH_LONG).show()

//           call the method listing items into the rectcleview so that the list is updated after the delete
				setupRecyclerView()
			}
			dialogInterface.dismiss()// the dialog to be dismissed
		}
//        performing the negative action
		builder.setNegativeButton("No") { dialogInterface, which ->
			dialogInterface.dismiss()
		}
//        create the alert dialog
		val alertDialog: AlertDialog = builder.create()
//        set other dialog properties
		alertDialog.setCancelable(false)// will not allow user to cancel after deletetion
		alertDialog.show() // show the dialog to the UI


	}

}
