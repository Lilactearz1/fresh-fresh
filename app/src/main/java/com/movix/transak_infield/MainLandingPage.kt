package com.movix.transak_infield

import android.app.ComponentCaller
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Adapter
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.SpinnerAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.DividerDefaults.color
import androidx.compose.ui.graphics.Color
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.itextpdf.kernel.colors.DeviceRgb
 import com.movix.transak_infield.EstimateSession
import com.movix.transak_infield.R.id.spinnerMark
import com.movix.transak_infield.databinding.ActivityArchivesBinding
import java.time.LocalDate


class MainLandingPage : AppCompatActivity(), EstimateAdapter.OnEstimateClickListener {
	private lateinit var allEstimates: TextView
	private lateinit var recyclerView: RecyclerView
	private lateinit var spinner: Spinner
	private lateinit var floatingPlus: FloatingActionButton
	private lateinit var all: BottomNavigationView
	private lateinit var unpaid: BottomNavigationView
	private lateinit var partiallypaid: BottomNavigationView
	private lateinit var overdue: BottomNavigationView
	private lateinit var paid: BottomNavigationView
	private lateinit var filter: BottomNavigationView
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

		spinner = findViewById(spinnerMark)
		floatingPlus = binding.floatingplus

		allEstimates.setOnClickListener {
			val typeface = Templatepdf1().getTypeface(applicationContext)
			allEstimates.setTypeface(Typeface.create(typeface, Typeface.NORMAL))
		}
		//initialize the databasehandler
		db =DatabaseHandler(applicationContext)



	 EstimateSession.loadSession(this)

		val currentId = EstimateSession.currentEstimate
		if (currentId != null) {
			val unfinished = db.getEstimateById(currentId)
			if (unfinished != null && unfinished.status == EstimateStatus.OPEN) {
				// Resume previous unfinished estimate
				val intent = Intent(this, MainActivity::class.java)
				intent.putExtra("estimate_id", currentId)
				startActivity(intent)
				finish()
			} else {
				// Clear the session if it was already closed
				EstimateSession.clearSession(this)
			}
		}


//		arrange your views systematically
		/*  1.horizontalscroll -> all unpaid  partiallypaid overdue paid filter
			2.recyclerview
			3.plus_floating button
			4.bottom_navigation-> estimate invoice receipt delivery  */

		floatingPlus.setOnClickListener {
			val unfinishedId = EstimateSession.currentEstimate
			if (unfinishedId != null) {
				val unfinishedEstimate = db.getEstimateById(unfinishedId)
				if (unfinishedEstimate != null && unfinishedEstimate.status == EstimateStatus.OPEN) {
					// Resume unfinished estimate
					val intent = Intent(this, MainActivity::class.java)
					intent.putExtra("estimate_id", unfinishedId)
					startActivity(intent)
					return@setOnClickListener
				} else {
					EstimateSession.clearSession(this)
				}
			}

			// Start a new estimate
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
			startActivity(intent)
		}



		recyclerView = binding.estimateListRecycler
		recyclerView.layoutManager = LinearLayoutManager(this)
		db = DatabaseHandler(this)

		val estimateId = intent.getIntExtra("customer_id", -1)

		val items = db.getAllEstimate()
		adapter = EstimateAdapter(this, items, this)
		recyclerView.adapter = adapter


//		val itemmarking =resources.getStringArray(R.array.markItems)
//		val adapter2:ArrayAdapter<CharSequence> =ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, itemmarking)
//			 adapter2.setDropDownViewResource(com.google.android.material.R.layout.support_simple_spinner_dropdown_item)
//			spinner.adapter=adapter2


	}



	override fun onEstimateClick(estimate: Estimateinfo) {
		val intent = Intent(this, CustomerItems::class.java)
		intent.putExtra("customer_id", estimate.customerId)
		startActivity(intent)
	}


	fun startnewEstimate(dbHandler: DatabaseHandler, customerId: Int, estimate: Estimateinfo): Int {
		val estimateinfo = estimate
		val newId = dbHandler.addEstimateInfo(estimateinfo = estimateinfo).toInt()
		EstimateSession.currentEstimate = newId
		return newId
	}


}