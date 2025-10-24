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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.itextpdf.kernel.colors.ColorConstants
import com.movix.transak_infield.databinding.ActivityArchivesBinding

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

 		Log.d("EstimateDebug", "Session at app start: ${EstimateSession.currentEstimate}")


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
				Log.d("EstimateDebug", "Resuming unfinished estimate ID = $currentId")
				val intent = Intent(this, MainActivity::class.java).apply {
					putExtra("estimate_id", currentId)
					putExtra("customer_id", unfinished.customerId)
				}
				startActivity(intent)
				finish()
				return true // ✅ Found active estimate, skip landing
			} else {
				Log.d("EstimateDebug", "Estimate is completed or invalid, clearing session")
				EstimateSession.clearSession(this)
			}
		}
		return false // ✅ No active estimate
	}


	override fun onStart() {
		super.onStart()
		if (checkActiveEstimate()) return

		Log.d("EstimateDebug", "Session onStart: ${EstimateSession.currentEstimate}")
	}

	private fun estimateRecyclerview() {
		recyclerView = binding.estimateListRecycler
		recyclerView.layoutManager = LinearLayoutManager(this)
		val items = db.getAllEstimate()
		val clients =db.viewClientsInfo()
		adapter = EstimateAdapter(this, items, clients,this,{ deletedItem, pos ->
			// Persist deletion (delete from DB / remote API)
		})

		recyclerView.adapter = adapter

		// Create callback
		val simpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
			override fun onMove(
				recyclerView: RecyclerView,
				viewHolder: RecyclerView.ViewHolder,
				target: RecyclerView.ViewHolder
			): Boolean = false // not supporting move/drag

			override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
				val position = viewHolder.adapterPosition
				val removedItem = adapter.removeAt(position)

				// call adapter callback to persist deletion

				adapter.onItemDeleted?.invoke(removedItem, position) // if you used lambda param

				// Show Snackbar with undo
				Snackbar.make(recyclerView, "${removedItem.titleINV} removed", Snackbar.LENGTH_LONG)
					.setAction("UNDO") {
						// undo: re-insert into adapter and (optionally) cancel DB deletion
						adapter.restoreAt(position, removedItem)
						// also revert the persistent deletion if already performed
					}
					.show()
			}

			// Optional: draw background icon/color while swiping (visual polish)
			override fun onChildDraw(
				c: Canvas,
				recyclerView: RecyclerView,
				viewHolder: RecyclerView.ViewHolder,
				dX: Float,
				dY: Float,
				actionState: Int,
				isCurrentlyActive: Boolean
			) {
				// You can draw colored background and icon here,

				// or use ItemDecorator libraries. For brevity, call super:
				super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
			}
		}

		val itemTouchHelper = ItemTouchHelper(simpleCallback)
		itemTouchHelper.attachToRecyclerView(recyclerView)
	}

	override fun onEstimateClick(estimate: Estimateinfo) {
		val intent = Intent(this, CustomerItems::class.java)
		intent.putExtra("estimate_id", estimate.estimateId)
		intent.putExtra("customer_id", estimate.customerId)
		startActivity(intent)
	}


}
