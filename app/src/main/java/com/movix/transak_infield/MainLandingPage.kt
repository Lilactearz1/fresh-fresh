package com.movix.transak_infield

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.movix.transak_infield.databinding.ActivityArchivesBinding

class MainLandingPage : AppCompatActivity() {

	private lateinit var binding: ActivityArchivesBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityArchivesBinding.inflate(layoutInflater)
		setContentView(binding.root)

		setSupportActionBar(findViewById(R.id.toolbar))
		binding.toolbarLayout.title = title


		val db=DatabaseHandler(applicationContext)
		val recyclerView = binding.estimateListRecycler


		val estimateList: List<Estimateinfo> = db.getAllEstimate()

		val estimateAdapter = EstimateAdapter(this, estimateList) { selectedEstimate ->
			showEstimateInfos(selectedEstimate, estimateList)
		}

		recyclerView.adapter = estimateAdapter
		recyclerView.layoutManager = LinearLayoutManager(this)

	}
	fun showEstimateInfos(estimate:Estimateinfo, itemList: List<Estimateinfo>) {
		val intent = Intent(applicationContext, MainActivity::class.java)
		intent.putExtra("estimateId", estimate.id)
		startActivity(intent)
	}


}