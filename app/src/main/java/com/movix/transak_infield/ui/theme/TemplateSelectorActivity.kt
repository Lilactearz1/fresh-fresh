package com.movix.transak_infield.ui.theme

import TemplateAdapter
import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.movix.transak_infield.PdfTemplateDRW
import com.movix.transak_infield.R
import com.movix.transak_infield.TemplateItem
import android.content.Intent
import androidx.activity.OnBackPressedCallback
import com.movix.transak_infield.MainActivity.Companion.SELECTED_TEMPLATE

class TemplateSelectorActivity : AppCompatActivity() {

	private lateinit var adapter: TemplateAdapter
	private lateinit var recyclerView: RecyclerView
	private var selectedTemplate: PdfTemplateDRW = PdfTemplateDRW.CLASSIC

	override fun onCreate(savedInstanceState: Bundle?) {
//load saved template

		val savedName = getSharedPreferences("templates", MODE_PRIVATE)
			.getString("selected", PdfTemplateDRW.CLASSIC.name)

		selectedTemplate = PdfTemplateDRW.valueOf(savedName!!)

		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_template_selector)

		// Modern back press handler
		onBackPressedDispatcher.addCallback(
			this,
			object : OnBackPressedCallback(true) {
				override fun handleOnBackPressed() {
					returnResultAndFinish()
				}
			}
		)

		recyclerView = findViewById(R.id.templatesRecyclerView)
		recyclerView.layoutManager = LinearLayoutManager(
			this,
			LinearLayoutManager.VERTICAL,
			false
		)

		val items = PdfTemplateDRW.entries.map {
			TemplateItem(
				templateItem = it,
				selected = it == selectedTemplate
			)
		}.toMutableList()

		adapter = TemplateAdapter(items) { clickedTemplate ->
			onTemplateSelected(clickedTemplate)
		}

		recyclerView.adapter = adapter
	}

	private fun onTemplateSelected(template: PdfTemplateDRW) {
		selectedTemplate = template

		// Update UI
		adapter.updateSelection(template)

		// Save selection for later
		getSharedPreferences("templates", MODE_PRIVATE)
			.edit()
			.putString("selected", template.name)
			.apply()

		// Return to calling activity
		returnResultAndFinish()
	}

	private fun returnResultAndFinish() {
		val resultIntent = Intent()
		resultIntent.putExtra(SELECTED_TEMPLATE, selectedTemplate.name)
		setResult(Activity.RESULT_OK, resultIntent)
		finish()
	}


}
