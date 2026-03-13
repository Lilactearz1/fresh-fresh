package com.movix.transak_infield

import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.util.Log
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.movix.transak_infield.MainActivity.Companion.EXTRA_CUSTOMER_ID
import com.movix.transak_infield.MainActivity.Companion.EXTRA_ESTIMATE_ID
import com.movix.transak_infield.pdfStyles.Modern1
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class Save_previewActivity : AppCompatActivity() {
	private lateinit var _binding: Binder
	private lateinit var db: DatabaseHandler
	private lateinit var downloadbtn: ImageView
	private lateinit var endEstimatebtn: ImageView
	private lateinit var print: ImageView
	private lateinit var more: ImageView
	private lateinit var totalKsh: TextView
	private lateinit var dueDate: TextView
	private lateinit var name: TextView
	private lateinit var share: MaterialButton
	private lateinit var checkCompleted: CheckBox

    private var estimateId: Int = -1
	private var customerId: Int = -1
	private val stringFormat = "%,.2f"
	private var currentTemplate: PdfTemplateDRW? = null



    override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()

		setContentView(R.layout.activity_save_preview)
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
			insets
		}

        // Get IDs
        estimateId = intent.getIntExtra(EXTRA_ESTIMATE_ID, -1)
        customerId = intent.getIntExtra(EXTRA_CUSTOMER_ID, -1)

        if (estimateId <= 0) {
            Toast.makeText(this, "Missing estimate", Toast.LENGTH_LONG).show()
            finish()
            return
        }

// ✅ Get template from MainActivity
        val templateName = intent.getStringExtra(MainActivity.SELECTED_TEMPLATE)
        currentTemplate = templateName?.let { PdfTemplateDRW.valueOf(it) }

// fallback template
        if (currentTemplate == null) {
            currentTemplate = PdfTemplateDRW.CLASSIC
        }

        db = DatabaseHandler(this)


// Safe to use context here
        val template = currentTemplate ?: PdfTemplateDRW.MODERN

        downloadbtn = findViewById<ImageView>(R.id.Download1)
		endEstimatebtn = findViewById<ImageView>(R.id.edit1)
		print = findViewById<ImageView>(R.id.print1)
		more = findViewById<ImageView>(R.id.more1)
		totalKsh = findViewById<TextView>(R.id.totalKsh1)
		dueDate = findViewById<TextView>(R.id.dueDate1)
		name = findViewById(R.id.name1)
		share = findViewById(R.id.sharebtn1)





// ✅ Download
        downloadbtn.setOnClickListener {

            lifecycleScope.launch(Dispatchers.IO) {

                val file = PdfUtils.generateEstimatePdf(this@Save_previewActivity, estimateId, customerId, template)

                withContext(Dispatchers.Main) {
                    if (file != null) {
                        Toast.makeText(this@Save_previewActivity, "Success...", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // ✅ End Estimate

		endEstimatebtn.setOnClickListener {
			db = DatabaseHandler(applicationContext)

			val currentId = EstimateSession.currentEstimate


			if (currentId != null) {
				// 1️⃣ Close current estimate
				db.updateEstimateStatus(currentId, EstimateStatus.COMPLETED)

				// 2️⃣ Clear the session
				EstimateSession.clearSession(this)

				// 3️⃣ Return to MainLandingPage (will auto-create new estimate if none open)
				val intent = Intent(this, MainLandingPage::class.java)
				intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
				startActivity(intent)
				finish()
			} else {
				Toast.makeText(applicationContext, "No active estimate", Toast.LENGTH_SHORT)
					.show()
			}
		}

		print.setOnClickListener { view ->
			Toast.makeText(applicationContext, "coming soon...", Toast.LENGTH_SHORT).show()
		}
		more.setOnClickListener { view ->
			Toast.makeText(applicationContext, "coming soon...", Toast.LENGTH_SHORT).show()
            // but to upload to drive

		}
		val cashFormat =
			stringFormat.format(GlobalFunck().summationofTotal(applicationContext, estimateId))

		totalKsh.text = "Ksh: ${cashFormat}"

		dueDate.text = GlobalFunck().dueDate(applicationContext)
val db= DatabaseHandler(applicationContext)


        val clientName = if (customerId > 0) {
            db.getClientNameById(customerId)
        } else {
            null
        }

        name.text = clientName ?: "Unknown Client"

        share.setOnClickListener {

            lifecycleScope.launch(Dispatchers.IO) {

                val pdfFile = PdfUtils.generateEstimatePdf(applicationContext, estimateId, customerId, template)

                withContext(Dispatchers.Main) {
                    pdfFile?.let { PdfUtils.sharePdf(this@Save_previewActivity, it) }
                }
            }
        }

	}

	override fun onPostResume() {

		super.onPostResume()
	}

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch(Dispatchers.IO) {

            val template = currentTemplate ?: PdfTemplateDRW.MODERN
            val pdfFile = PdfUtils.generateEstimatePdf(applicationContext, estimateId, customerId, template)
            val preview = pdfFile?.let { PdfUtils.generatePdfPreview(this@Save_previewActivity, it) }

            withContext(Dispatchers.Main) {
                findViewById<ImageView>(R.id.previewDownload1).apply {
                    preview?.let { setImageBitmap(it) }
                }
            }
        }
    }

	private fun getTemplate(): PdfTemplateDRW = currentTemplate ?: PdfTemplateDRW.MODERN


    

	override fun onResumeFragments() {
		super.onResumeFragments()
	}

	fun closeCurrentEstimate() {
		db = DatabaseHandler(applicationContext)
		val id = EstimateSession.currentEstimate ?: return
		db.updateEstimateStatus(id, EstimateStatus.COMPLETED)
		EstimateSession.clearSession(this)

	}

}