package com.movix.transak_infield


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.itextpdf.io.font.FontProgramFactory
import com.itextpdf.io.font.PdfEncodings
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.font.PdfFontFactory.EmbeddingStrategy
import com.movix.transak_infield.databinding.ActivityMainBinding

import com.movix.transak_infield.pdfStyles.Modern1
import com.movix.transak_infield.pdfStyles.Modern1.Classic1
import com.movix.transak_infield.pdfStyles.Modern1.Minimal
import com.movix.transak_infield.ui.theme.TemplateInterface
import com.movix.transak_infield.ui.theme.TemplateSelectorActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

//import the R .id r. layout ,drawables


open class MainActivity : AppCompatActivity() {
	private lateinit var binding: ActivityMainBinding
	private lateinit var itemsCount: TextView
	private lateinit var Subtotal: GlobalFunck
	private lateinit var Total: GlobalFunck
	private lateinit var db: DatabaseHandler
	private var itemAdapter: ItemAdapter? = null
	private var estimateId = -1
	private var customerId = -1
	private val stringFomat = "%,.2f"
	private var dueTerms = 0
	private var currentTemplate: PdfTemplateDRW? = null


	override fun onCreate(savedInstanceState: Bundle?) {

		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)

		setContentView(binding.root)  // Connects to activity_main.xml so check layout name to match activity

		val rvl_bussinessInfo = binding.relativelayoutBussinessInfo
		val rvl_clientInfo = binding.relativeLayoutClentInfo

		estimateId = intent.getIntExtra("estimate_id", -1)
		customerId = intent.getIntExtra("customer_id", -1)


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
			handleItemsCardView(this, intent)
		}



		rvl_bussinessInfo?.setOnClickListener {
			binding.businessimage?.performClick()
		}

		rvl_clientInfo?.setOnClickListener {
			binding.btnclientInfo.performClick()
		}


		val bottomNav = binding.bottomnav
		bottomNav?.setContent {
			MaterialTheme {
				BottomBar()
			}
		}


		binding.addbuttonImage.setOnClickListener {
			handleItemsCardView(this, intent)
		}


//set the text view to
		itemsCount = binding.tvItems
		//	    function to count items number inserted
		fun countItems() {
			itemsCount.text = "Items No: [${setupListintoRecycleview()}]"
		}
		countItems()
		// function to display totals in the textview
		fun txtViewTotals() {

			val itemSubtal = binding.esumSubtotal
			val itemsTotal = binding.esumTotal
			Subtotal = GlobalFunck()
			val stringSubtotal = stringFomat.format(
				Math.floor(
					Subtotal.getSubTotal(applicationContext, estimateId).toDouble()
				)
			)
			itemSubtal.text = stringSubtotal

			Total = GlobalFunck()
			val stringTotal = stringFomat.format(
				Math.floor(
					Total.summationofTotal(applicationContext, estimateId).toDouble()
				)
			)
			itemsTotal.text = stringTotal
		}
//call the

		//call the method that show items into our recycler view
		setupListintoRecycleview()
//	    call method for tv add subtotal and totals
		txtViewTotals()


		supportFragmentManager.addOnBackStackChangedListener {
			val fragment = supportFragmentManager.findFragmentById(R.id.newEstimateLayout)

			// Only refresh list when the visible fragment is null (i.e., back to main view)
			if (fragment == null) {
				setupListintoRecycleview()
				//	    function to count items number inserted
				countItems()
				//	    call method for tv add subtotal and totals
				txtViewTotals()
			}
		}

	}

	override fun onResume() {
		super.onResume()
		setupListintoRecycleview()
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

		super.onActivityResult(requestCode, resultCode, data)

		if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
			val templateName = data?.getStringExtra(SELECTED_TEMPLATE)
			if (templateName != null) {
				val selectedTemplate = PdfTemplateDRW.valueOf(templateName)
				Toast.makeText(this, "Template selected: $templateName", Toast.LENGTH_SHORT).show()
				// Save the selected template for later use
				currentTemplate = selectedTemplate
			}
		}
	}

	private fun setupListintoRecycleview(): Int {
		val itemList = getItemlist()

		if (itemList.isNotEmpty()) {
			binding.recycleItem.visibility = View.VISIBLE
			binding.recycleItem.layoutManager = LinearLayoutManager(this)

			if (itemAdapter == null) {
				itemAdapter = ItemAdapter(this, itemList)
				binding.recycleItem.adapter = itemAdapter
			} else {
				itemAdapter!!.apply {
					this.itemList.clear()
					this.itemList.addAll(itemList)
					notifyDataSetChanged()
				}
			}
		} else {
			binding.recycleItem.visibility = View.GONE
		}

		// ✅ Return number of items currently displayed
		return itemList.size
	}


// the jetpack bottom appBar

	@Composable
	private fun BottomBar() {
		BottomAppBar(
			containerColor = Color(0, 142, 204), tonalElevation = 4.dp
		) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 12.dp),
				horizontalArrangement = Arrangement.SpaceEvenly,
				verticalAlignment = Alignment.CenterVertically
			) {
				// PREVIEW BUTTON
				TextButton(
					onClick = {
						lifecycleScope.launch {

							val estimateId = intent.getIntExtra(EXTRA_ESTIMATE_ID, -1)
							val customerId = intent.getIntExtra(EXTRA_CUSTOMER_ID, -1)
							val template = currentTemplate ?: PdfTemplateDRW.CLASSIC

							Log.d("TEMPLATE", "currentTemplate = $currentTemplate")

							try {
								// Generate PDF in background
								val pdfFile = withContext(Dispatchers.IO) {
									estimatePdf(this@MainActivity, estimateId, customerId, template)
								}

								// UI updates on main thread
								Toast.makeText(this@MainActivity, "Success...", Toast.LENGTH_SHORT).show()
								PdfUtils.previewPdfFormat(this@MainActivity, pdfFile)

							} catch (e: Exception) {
								e.printStackTrace()
								Toast.makeText(
									this@MainActivity,
									"Failed to open PDF: ${e.message}",
									Toast.LENGTH_SHORT
								).show()
							}
						}
					},
					shape = RoundedCornerShape(50),
					colors = ButtonDefaults.textButtonColors(
						containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
						contentColor = MaterialTheme.colorScheme.onPrimary
					)
				) {
					Text(
						text = "Preview",
						modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
					)
				}



				// Save BUTTON
				TextButton(
					onClick = {
						lifecycleScope.launch(Dispatchers.IO) {
							try {
								val estimateId = intent.getIntExtra(EXTRA_ESTIMATE_ID, -1)
								val customerId = intent.getIntExtra(EXTRA_CUSTOMER_ID, -1)

								val newIntent = Intent(
									this@MainActivity, Save_previewActivity::class.java
								).apply {
									putExtra(EXTRA_ESTIMATE_ID, estimateId)
									putExtra(EXTRA_CUSTOMER_ID, customerId)
								}

								withContext(Dispatchers.Main) {
									startActivity(newIntent)
									Toast.makeText(
										this@MainActivity,
										"Opening save preview...",
										Toast.LENGTH_LONG
									).show()
								}
							} catch (e: Exception) {
								e.printStackTrace()
								withContext(Dispatchers.Main) {
									Toast.makeText(
										this@MainActivity,
										"Failed to open Save Preview: ${e.message}",
										Toast.LENGTH_SHORT
									).show()
								}
							}
						}
					}, shape = RoundedCornerShape(50), colors = ButtonDefaults.textButtonColors(
						containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
						contentColor = MaterialTheme.colorScheme.onPrimary
					)
				) {
					Text(
						text = "Save",
						modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
					)
				}

			}
		}
	}


	companion object {

		const val EXTRA_ESTIMATE_ID = "estimate_id"
		const val EXTRA_CUSTOMER_ID = "customer_id"
		const val SELECTED_TEMPLATE= "selected_template"

		private lateinit var file: File

		fun handleInvoiceButtonClick(
			activity: AppCompatActivity, estimateId: Int, customerId: Int
		) {
			val bundle = Bundle().apply {
				putInt(EXTRA_ESTIMATE_ID, estimateId)
				putInt(EXTRA_CUSTOMER_ID, customerId)
			}

			val fragment = InvoiceInfo().apply { arguments = bundle }

			activity.supportFragmentManager.beginTransaction()
				.replace(R.id.newEstimateLayout, fragment).addToBackStack(null).commit()
		}

		fun handleTemplateButtonClick(activity: Activity) {

			val intent = Intent(activity, TemplateSelectorActivity::class.java)
			activity.startActivityForResult(intent, 1001)


		}


		fun handleClientInfoClick(context: Context) {
			val intent = Intent(context, ClientActivity::class.java)

			// Important: add FLAG_ACTIVITY_NEW_TASK if context is not an Activity
			if (context !is AppCompatActivity) {
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			}
			context.startActivity(intent)
		}

		fun handleBusinessImage(context: Context) {
			Toast.makeText(context, "Coming soon...", Toast.LENGTH_LONG).show()
		}

		fun handleItemsCardView(activity: AppCompatActivity, intent: Intent) {
			val estimateId = intent.getIntExtra(EXTRA_ESTIMATE_ID, -1)
			val customerId = intent.getIntExtra(EXTRA_CUSTOMER_ID, -1)

			Log.d(
				"InvoiceDebug",
				"Passing estimateId=$estimateId, customerId=$customerId to ProductsInfoFragment"
			)

			val productFrag = ProductsInfoFragment().apply {
				arguments = Bundle().apply {
					putInt(EXTRA_ESTIMATE_ID, estimateId)
					putInt(EXTRA_CUSTOMER_ID, customerId)
				}
			}

			activity.supportFragmentManager.beginTransaction()
				.replace(R.id.newEstimateLayout, productFrag).addToBackStack(null).commit()
		}

		//set the fonts for the pdf sections
		fun getFontGerhana(context: Context): PdfFont {
			val inputStream = context.assets.open("fonts/gerhana.ttf")
			val fontBytes = inputStream.readBytes()
			inputStream.close()

			val fontProgram = FontProgramFactory.createFont(fontBytes, true)
			return PdfFontFactory.createFont(
				fontProgram, PdfEncodings.IDENTITY_H, EmbeddingStrategy.PREFER_EMBEDDED
			)
		}

		fun latoRegularFont(context: Context): PdfFont {
			val inputStream = context.assets.open("fonts/lato_regular.ttf")
			val fontByte = inputStream.readBytes()
//        create  the font program
			val fontProgram = FontProgramFactory.createFont(fontByte, true)
//        return gffont factory
			val fontFact = PdfFontFactory.createFont(
				fontProgram, PdfEncodings.IDENTITY_H, EmbeddingStrategy.PREFER_EMBEDDED
			)
			return fontFact
		}


		fun latoBold(context: Context): PdfFont {
			val fontstream = context.assets.open("fonts/latobold.ttf")
			val inputStream = fontstream.readBytes()
//        create a font program and parse input stream to it
			val fontProgram = FontProgramFactory.createFont(inputStream, true)
//        create a pdfontfactory
			val latoboldFont = PdfFontFactory.createFont(
				fontProgram, PdfEncodings.IDENTITY_H, EmbeddingStrategy.PREFER_EMBEDDED
			)
			return latoboldFont
		}

		//queens font for the pdf headings
		fun queensFont(context: Context): PdfFont {
			val fontStream = context.assets.open("fonts/queen.otf")
			val inputstream = fontStream.readBytes()
			val fontProgram = FontProgramFactory.createFont(inputstream, true)
			val queenFont = PdfFontFactory.createFont(
				fontProgram, PdfEncodings.IDENTITY_H, EmbeddingStrategy.PREFER_EMBEDDED
			)
			return queenFont
		}


		fun estimatePdf(
			context: Context,
			estimateId: Int,
			customerId: Int,
			templateDRW: PdfTemplateDRW,
		): File {
			val data = PdfUtils.load(context, estimateId, customerId)
			val layout = PdfUtils.loadTemplateFromJson(context, templateDRW.jsonResId)

			Log.d("PDFDebug", "Selected template : $templateDRW, background: ${layout.background}")

			val pdfTemplate: TemplateInterface = when (templateDRW) {
				PdfTemplateDRW.CLASSIC -> Classic1(context)
				PdfTemplateDRW.MODERN -> Modern1(context)
				PdfTemplateDRW.MINIMAL -> Minimal(context)

			}

			return PdfUtils.generate(context, pdfTemplate, layout, data)

		}

	}

	private fun setupListIntoRecyclerView(): Int {
		val itemList = getItemlist()

		if (itemList.isNotEmpty()) {
			binding.recycleItem.visibility = View.VISIBLE
			binding.recycleItem.layoutManager = LinearLayoutManager(this)

			if (itemAdapter == null) {
				itemAdapter = ItemAdapter(this, itemList)
				binding.recycleItem.adapter = itemAdapter
			} else {
				itemAdapter!!.apply {
					this.itemList.clear()
					this.itemList.addAll(itemList)
					notifyDataSetChanged()
				}
			}

			val itemTouchHelper = ItemTouchHelper(object :
				ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

				override fun onMove(
					recyclerView: RecyclerView,
					viewHolder: RecyclerView.ViewHolder,
					target: RecyclerView.ViewHolder
				) = false

				override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
					val position = viewHolder.adapterPosition
					val item = itemAdapter!!.itemList[position]

					val db = DatabaseHandler(this@MainActivity)
					val deleted = db.deleteItem(item.id)

					if (deleted > 0) {
						itemAdapter!!.itemList.removeAt(position)
						itemAdapter!!.notifyItemRemoved(position)
						Toast.makeText(
							this@MainActivity, "${item.itemName} deleted", Toast.LENGTH_SHORT
						).show()
					} else {
						Toast.makeText(this@MainActivity, "Delete failed", Toast.LENGTH_SHORT)
							.show()
					}

					setupListIntoRecyclerView()
				}

				// 👇 This is where you visually draw the red background or delete icon while swiping
				override fun onChildDraw(
					c: Canvas,
					recyclerView: RecyclerView,
					viewHolder: RecyclerView.ViewHolder,
					dX: Float,
					dY: Float,
					actionState: Int,
					isCurrentlyActive: Boolean
				) {
					val itemView = viewHolder.itemView
					val background = ColorDrawable(R.color.red)
					val icon = ContextCompat.getDrawable(
						this@MainActivity, R.drawable.baseline_delete_24// your delete icon
					)

					// Draw red background as you swipe
					if (dX > 0) { // Swiping right
						background.setBounds(
							itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom
						)
					} else if (dX < 0) { // Swiping left
						background.setBounds(
							itemView.right + dX.toInt(),
							itemView.top,
							itemView.right,
							itemView.bottom
						)
					} else {
						background.setBounds(0, 0, 0, 0)
					}

					background.draw(c)

					// Draw delete icon
					icon?.let {
						val iconMargin = (itemView.height - it.intrinsicHeight) / 2
						val iconTop = itemView.top + iconMargin
						val iconBottom = iconTop + it.intrinsicHeight

						if (dX > 0) { // right swipe
							val iconLeft = itemView.left + iconMargin
							val iconRight = iconLeft + it.intrinsicWidth
							it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
						} else if (dX < 0) { // left swipe
							val iconRight = itemView.right - iconMargin
							val iconLeft = iconRight - it.intrinsicWidth
							it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
						}

						it.draw(c)
					}

					super.onChildDraw(
						c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive
					)
				}
			})

			itemTouchHelper.attachToRecyclerView(binding.recycleItem)
		} else {
			binding.recycleItem.visibility = View.GONE
		}

		return itemList.size
	}


	//    function to get the items list
	private fun getItemlist(): ArrayList<ModelClass> {
//        create instance of the databaseHandler class
		val databaseHandler: DatabaseHandler = DatabaseHandler(this)
//          calling the viewProduct  of DatabaseHandler class to read the list
		return databaseHandler.getItemsForEstimate(estimateId, customerId)

		/**
		 * a shorter replacement use the inline function "single expression function
		 *
		private fun getItemlist() = DatabaseHandler(this).viewProduct()
		 *
		or
		private fun getItemlist(): ArrayList<ModelClass> {
		return DatabaseHandler(this).viewProduct()
		}

		 */
	}


	// method to update the inputs in the dialog

	fun updateDialog(modelClass: ModelClass) {
		// Create the Dialog
		val dialog = AlertDialog.Builder(this).setTitle("update items info")
			.setView(R.layout.edititem_dialog)
			.setCancelable(false)   //will not allow user to cancel after
			.create()
//        now show the dialog
		dialog.show()


		// Find views from dialog layout
		val nameEditText = dialog.findViewById<EditText>(R.id.edit_item_namedialog)
		val quantityEditText = dialog.findViewById<EditText>(R.id.edit_item_quantity)
		val priceEditText = dialog.findViewById<EditText>(R.id.edit_item_pricedialog)
		val taxRateEditText = dialog.findViewById<EditText>(R.id.edit_item_taxRatedialog)
		val saveButton = dialog.findViewById<RelativeLayout>(R.id.SaveclientDetails)
		val cancelButton = dialog.findViewById<RelativeLayout>(R.id.backLayout)
		val amountScreen = dialog.findViewById<TextView>(R.id.tv_amount_display_value)

		// Fill views with existing data
		nameEditText?.setText(modelClass.itemName)
		quantityEditText?.setText(modelClass.quantity.toString())
		priceEditText?.setText(modelClass.price.toString())
		taxRateEditText?.setText(modelClass.tax.toString())

		fun updateItems(price: Double, quantity: Int) {
			val total = price * quantity
			val totalFormatText = stringFomat.format(total)

//			display the live amount in the text view
			amountScreen?.text = "Ksh: $totalFormatText"
		}

		// add tecxt watcher functions
		fun textChangerListener() {
			val price = priceEditText?.text.toString().toDoubleOrNull()
			val quantity = quantityEditText?.text.toString().toIntOrNull()

			if (price != null && quantity != null) {
				updateItems(price, quantity)
			}
		}
		textChangerListener()
		//cancellation button
		cancelButton?.setOnClickListener {
			dialog.dismiss()
		}


		// Save/update action
		saveButton?.setOnClickListener {

			val updatedName = nameEditText?.text.toString()
			val updatedQuantity = quantityEditText?.text.toString().toIntOrNull()
			val updatedPrice = priceEditText?.text.toString().toDoubleOrNull()
			val updatedTax = taxRateEditText?.text.toString().toFloatOrNull()

			if (updatedName.isBlank() || updatedQuantity == null || updatedPrice == null || updatedTax == null) {
				Toast.makeText(this, "All fields must be valid", Toast.LENGTH_SHORT).show()
				return@setOnClickListener
			}

			val updatedModel = ModelClass(
				id = modelClass.id,
				quantity = updatedQuantity,
				itemName = updatedName,
				price = updatedPrice,
				total = (updatedQuantity * updatedPrice).toFloat(),
				tax = updatedTax,
				customerId = modelClass.customerId,
				estimateId = modelClass.estimateId
			)


			val db = DatabaseHandler(this)
			// define this method in your DB handler (updateRecords)
			val status = db.updateRecords(updatedModel)

			if (status > -1) {
				Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()
				dialog.dismiss()
				// Optional: refresh RecyclerView
				setupListintoRecycleview()
			} else {
				Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
			}
		}

		dialog.show()
	}

	fun deleteItems(modellist: ModelClass) {

		val builder = AlertDialog.Builder(this)
		builder.setTitle(R.string.delete)
//                set message for the alert dialog box
		builder.setMessage("Delete ${modellist.itemName}")
		builder.setIcon(R.drawable.baseline_crisis_alert_24)
//        perform the positive action
		builder.setPositiveButton("Yes") { dialogInterface, which ->
//                creating an instance of the database classs
			val databaseseHandler: DatabaseHandler = DatabaseHandler(this)
//                 call the delete method of the database
			val status = databaseseHandler.deleteRecords(
				ModelClass(
					modellist.id,
					modellist.quantity,
					"",
					modellist.price,
					modellist.total,
					modellist.tax,
					modellist.customerId,
					modellist.estimateId,

					)
			)
			if (status > -1) {
				Toast.makeText(this, "Deleted Successfully", Toast.LENGTH_LONG).show()

//           call the method listing items into the rectcleview so that the list is updated after the delete
				setupListintoRecycleview()
			}
			dialogInterface.dismiss()// the dialog to be dismissed
		}
//        performing the negative action
		builder.setNegativeButton("No") { dialogInterface, which ->
			dialogInterface.dismiss()
		}
//        create the alert dialog
		val alertDialog: AlertDialog = builder.create()
//        set other dialog properties
		alertDialog.setCancelable(false)// will not allow user to cancel after deletetion
		alertDialog.show() // show the dialog to the UI


	}

}
