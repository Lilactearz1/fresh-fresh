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
import com.google.android.material.button.MaterialButton
import com.movix.transak_infield.MainActivity.Companion.EXTRA_CUSTOMER_ID
import com.movix.transak_infield.MainActivity.Companion.EXTRA_ESTIMATE_ID


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

		downloadbtn = findViewById<ImageView>(R.id.Download1)
		endEstimatebtn = findViewById<ImageView>(R.id.edit1)
		print = findViewById<ImageView>(R.id.print1)
		more = findViewById<ImageView>(R.id.more1)
		totalKsh = findViewById<TextView>(R.id.totalKsh1)
		dueDate = findViewById<TextView>(R.id.dueDate1)
		name = findViewById(R.id.name1)
		share = findViewById(R.id.sharebtn1)

		estimateId = intent.getIntExtra(EXTRA_ESTIMATE_ID, -1)
		customerId = intent.getIntExtra(EXTRA_CUSTOMER_ID, -1)

		val template = currentTemplate
			?: PdfTemplateDRW.CLASSIC  // fallback to default

		downloadbtn.setOnClickListener { view ->
			view.isHovered

			PdfUtils.generateEstimatePdf(applicationContext, estimateId,customerId, template)
			Toast.makeText(applicationContext, "Success...", Toast.LENGTH_LONG).show()
			closeCurrentEstimate()

		}

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
				Toast.makeText(applicationContext, "No active estimate to end.", Toast.LENGTH_SHORT)
					.show()
			}
		}


		print.setOnClickListener { view ->
			Toast.makeText(applicationContext, "coming soon...", Toast.LENGTH_SHORT).show()
		}

		more.setOnClickListener { view ->
			Toast.makeText(applicationContext, "coming soon...", Toast.LENGTH_SHORT).show()
		}
		val cashFormat =
			stringFormat.format(GlobalFunck().summationofTotal(applicationContext, estimateId))

		totalKsh.text = "Ksh: ${cashFormat}"

		dueDate.text = GlobalFunck().dueDate(applicationContext)

		name.text = GlobalFunck().safeClientName(applicationContext)

		share.setOnClickListener {
			val pdFile = PdfUtils.generateEstimatePdf(applicationContext, estimateId, customerId,template)
			pdFile.let {
				it
				if (it != null) {
					PdfUtils.sharePdf(this, it)
				}
			}
		}


	}


	override fun onPostResume() {

		super.onPostResume()
	}

	override fun onResume() {
		super.onResume()


		val template = currentTemplate
			?: PdfTemplateDRW.CLASSIC  // fallback to default
		val pdfFile = PdfUtils.generateEstimatePdf(applicationContext, estimateId, customerId,template)
		val pdfPreview = pdfFile?.let { PdfUtils.generatePdfPreview(this, it) }
		findViewById<ImageView>(R.id.previewDownload1).apply {
			pdfPreview?.let { setImageBitmap(it) }
		}
	}


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