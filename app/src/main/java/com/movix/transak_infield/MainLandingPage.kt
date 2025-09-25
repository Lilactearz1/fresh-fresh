package com.movix.transak_infield

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.movix.transak_infield.databinding.ActivityArchivesBinding
import java.time.LocalDate

class MainLandingPage : AppCompatActivity(), EstimateAdapter.OnEstimateClickListener {

	private lateinit var allEstimates: TextView
	private lateinit var recyclerView: RecyclerView
	private lateinit var spinner: Spinner
	private lateinit var floatingPlus: FloatingActionButton
	private lateinit var binding: ActivityArchivesBinding
	private lateinit var db: DatabaseHandler
	private lateinit var adapter: EstimateAdapter

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

		Log.d("EstimateDebug", "Loaded session ID = ${EstimateSession.currentEstimate}")

		Log.d("EstimateDebug", "Session at app start: ${EstimateSession.currentEstimate}")


		if (checkActiveEstimate()) return

		// Initialize recycler
		estimateRecyclerview()

		// Floating + button → start or resume estimate
		floatingPlus.setOnClickListener {
			val unfinishedId = EstimateSession.currentEstimate
			if (unfinishedId != null) {
				val unfinishedEstimate = db.getEstimateById(unfinishedId)
				if (unfinishedEstimate != null && unfinishedEstimate.status == EstimateStatus.OPEN) {
					val intent = Intent(this, MainActivity::class.java)
					intent.putExtra("estimate_id", unfinishedId)
					startActivity(intent)
					return@setOnClickListener
				} else {
					EstimateSession.clearSession(this)
				}
			}

			// No unfinished estimate → start a new one
			val blankEstimate = Estimateinfo(
				id = 0,
				titleINV = "",
				creationDate = LocalDate.now().toString(),
				dueDate = "",
				customerId = 0,
				status = EstimateStatus.OPEN
			)

			val newId = db.addEstimateInfo(blankEstimate).toInt()
			EstimateSession.saveSession(this, newId)


			val intent = Intent(this, MainActivity::class.java)
			intent.putExtra("estimate_id", newId)
			intent.putExtra("customer_id", 0)
			startActivity(intent)
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
		adapter = EstimateAdapter(this, items, this)
		recyclerView.adapter = adapter
	}

	override fun onEstimateClick(estimate: Estimateinfo) {
		val intent = Intent(this, CustomerItems::class.java)
		intent.putExtra("customer_id", estimate.customerId)
		startActivity(intent)
	}


}
