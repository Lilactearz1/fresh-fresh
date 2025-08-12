package com.movix.transak_infield

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
import com.movix.transak_infield.R.id.spinnerMark
import com.movix.transak_infield.databinding.ActivityArchivesBinding

class MainLandingPage : AppCompatActivity() {
	private lateinit var allEstimates: TextView

	private lateinit var spinner: Spinner
	private lateinit var floatingPlus: FloatingActionButton
	private lateinit var all:BottomNavigationView
	private  lateinit var unpaid:BottomNavigationView
	private lateinit var partiallypaid:BottomNavigationView
	private lateinit var overdue:BottomNavigationView
	private lateinit var paid :BottomNavigationView
	private lateinit var filter:BottomNavigationView
	private lateinit var binding: ActivityArchivesBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityArchivesBinding.inflate(layoutInflater)
		setContentView(binding.root)

		setSupportActionBar(findViewById(R.id.toolbar))
		binding.toolbarLayout.title = title
		allEstimates = binding.allestmimate
		spinner = findViewById(spinnerMark)
		floatingPlus = binding.floatingplus

		allEstimates.setOnClickListener { view ->
			val typeface = Templatepdf1().getTypeface(applicationContext)
			allEstimates.setTypeface(Typeface.create(typeface, Typeface.NORMAL))
		}

//		arrange your views systematically
		/*  1.horizontalscroll -> all unpaid  partiallypaid overdue paid filter
			2.recyclerview
			3.plus_floating button
			4.bottom_navigation-> estimate invoice receipt delivery  */
		floatingPlus.setOnClickListener(View.OnClickListener {
			val prefs =getSharedPreferences("mainLanding",MODE_PRIVATE)
			prefs.edit()
			startActivity(Intent(this,MainActivity::class.java))

		})

		val db = DatabaseHandler(applicationContext)
		val recyclerView = binding.estimateListRecycler


		val estimateList: List<Estimateinfo> = db.getAllEstimate()

		val estimateAdapter = EstimateAdapter(this, estimateList) { selectedEstimate ->
			showEstimateInfos(selectedEstimate, estimateList)
		}

		recyclerView.adapter = estimateAdapter
		recyclerView.layoutManager = LinearLayoutManager(this)

//		val itemmarking =resources.getStringArray(R.array.markItems)
//		val adapter2:ArrayAdapter<CharSequence> =ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, itemmarking)
//			 adapter2.setDropDownViewResource(com.google.android.material.R.layout.support_simple_spinner_dropdown_item)
//			spinner.adapter=adapter2


	}

	fun showEstimateInfos(estimate: Estimateinfo, itemList: List<Estimateinfo>) {
		val intent = Intent(applicationContext, MainActivity::class.java)
		intent.putExtra("estimateId", estimate.id)
		startActivity(intent)
	}


}