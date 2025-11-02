package com.movix.transak_infield

import android.content.Intent
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.itextpdf.kernel.colors.ColorConstants
import com.movix.transak_infield.databinding.ActivityArchivesBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainLandingPage : AppCompatActivity(), EstimateAdapter.OnEstimateClickListener {

	private lateinit var allEstimates: TextView
	private lateinit var recyclerView: RecyclerView
	private lateinit var spinner: Spinner
	private lateinit var floatingPlus: FloatingActionButton
	private lateinit var binding: ActivityArchivesBinding
	private lateinit var db: DatabaseHandler
	private lateinit var adapter: EstimateAdapter
	private  var title ="INFIELDER"

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityArchivesBinding.inflate(layoutInflater)
		setContentView(binding.root)

		setSupportActionBar(findViewById(R.id.toolbar))
		binding.toolbarLayout.title = title

		allEstimates = binding.allestmimate
		spinner = findViewById(R.id.spinnerMark)
		floatingPlus = binding.floatingplus
		db = DatabaseHandler(applicationContext)

		// Load current session
		EstimateSession.loadSession(this)




		if (checkActiveEstimate()) return

		// Initialize recycler
		estimateRecyclerview()

		// Floating + button → start or resume estimate
		floatingPlus.setOnClickListener {
			EstimateSession.clearSession(this) // force new start
			val unfinishedId = EstimateSession.currentEstimate
			if (unfinishedId != null) {
				val unfinishedEstimate = db.getEstimateById(unfinishedId)
				if (unfinishedEstimate != null && unfinishedEstimate.status == EstimateStatus.OPEN) {
					val intent = Intent(this, MainActivity::class.java)
					intent.putExtra("estimate_id", unfinishedId)
					intent.putExtra("customer_id", unfinishedEstimate.customerId)
					startActivity(intent)
					return@setOnClickListener
				} else {
					EstimateSession.clearSession(this)
				}
			}

			// No unfinished estimate → start a new one
			// Use the helper function that auto-handles ID & session
			val newEstimateId = db.createNewEstimate(this, customerId = 1,title) // use Guest ID


			if (newEstimateId != -1L) {
				val intent = Intent(this, MainActivity::class.java)
				intent.putExtra("estimate_id", newEstimateId.toInt())
				intent.putExtra("customer_id", 1)
				startActivity(intent)
			} else {
				Toast.makeText(this, "Failed to create new estimate.", Toast.LENGTH_SHORT).show()
			}
		}

	}

	private fun checkActiveEstimate(): Boolean {
		val currentId = EstimateSession.currentEstimate ?: 0
		if (currentId != 0) {
			val unfinished = db.getEstimateById(currentId)
			if (unfinished?.status == EstimateStatus.OPEN) {

				val intent = Intent(this, MainActivity::class.java).apply {
					putExtra("estimate_id", currentId)
					putExtra("customer_id", unfinished.customerId)
				}
				startActivity(intent)
				finish()
				return true // ✅ Found active estimate, skip landing
			} else {

				EstimateSession.clearSession(this)
			}
		}
		return false // ✅ No active estimate
	}


	override fun onStart() {
		super.onStart()
		if (checkActiveEstimate()) return


	}

	private fun estimateRecyclerview() {
		recyclerView = binding.estimateListRecycler
		recyclerView.layoutManager = LinearLayoutManager(this)

		// Launch background coroutine for DB queries
		lifecycleScope.launch(Dispatchers.IO) {
			val items = db.getAllEstimate()
			val clients = db.viewClientsInfo()

			withContext(Dispatchers.Main) {
				adapter = EstimateAdapter(
					this@MainLandingPage,
					items,
					clients,
					this@MainLandingPage
				) { deletedItem, pos ->
					// Persist deletion (delete from DB / remote API)
				}

				recyclerView.adapter = adapter

				val simpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
					override fun onMove(
						recyclerView: RecyclerView,
						viewHolder: RecyclerView.ViewHolder,
						target: RecyclerView.ViewHolder
					) = false

					override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
						val position = viewHolder.adapterPosition
						val removedItem = adapter.removeAt(position)
						adapter.onItemDeleted?.invoke(removedItem, position)

						Snackbar.make(recyclerView, "${removedItem.titleINV} removed", Snackbar.LENGTH_LONG)
							.setAction("UNDO") {
								adapter.restoreAt(position, removedItem)
							}.show()
					}
				}
				ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView)
			}
		}
	}


	override fun onEstimateClick(estimate: Estimateinfo) {
		val intent = Intent(this, CustomerItems::class.java)
		intent.putExtra("estimate_id", estimate.estimateId)
		intent.putExtra("customer_id", estimate.customerId)
		startActivity(intent)
	}


}
