package com.movix.transak_infield

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.movix.transak_infield.MainActivity.Companion.handleBusinessImage
import com.movix.transak_infield.MainActivity.Companion.handleClientInfoClick
import com.movix.transak_infield.MainActivity.Companion.handleInvoiceButtonClick
import com.movix.transak_infield.MainActivity.Companion.handleItemsCardView
import com.movix.transak_infield.MainActivity.Companion.handleTemplateButtonClick
import com.movix.transak_infield.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
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

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		_binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

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
}
