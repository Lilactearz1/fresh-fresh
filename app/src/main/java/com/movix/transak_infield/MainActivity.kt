package com.movix.transak_infield


import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.itextpdf.io.font.FontProgramFactory
import com.itextpdf.io.font.PdfEncodings
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.io.source.ByteArrayOutputStream
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.font.PdfFontFactory.EmbeddingStrategy
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue

import com.movix.transak_infield.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

import androidx.compose.material3.*
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itextpdf.kernel.events.PdfDocumentEvent
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.properties.Delegates

//import the R .id r. layout ,drawables


open class MainActivity : AppCompatActivity() {
	private lateinit var binding: ActivityMainBinding
	private lateinit var itemsCount: TextView
	private lateinit var Subtotal: GlobalFunck
	private lateinit var Total: GlobalFunck
	private lateinit var db:DatabaseHandler
	private  var itemAdapter: ItemAdapter?= null

	private var dueTerms = 0


	override fun onCreate(savedInstanceState: Bundle?) {

		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)

		setContentView(binding.root)  // Connects to activity_main.xml so check layout name to match activity

		val rvl_bussinessInfo = binding.relativelayoutBussinessInfo
		val rvl_clientInfo = binding.relativeLayoutClentInfo

		binding.btnInv001.setOnClickListener {

			val estimateId = intent.getIntExtra("estimate_id", -1)
			val bundle = Bundle().apply {
				putInt("estimate_id", estimateId)
			}
			val fragment = InvoiceInfo().apply { arguments = bundle }
			supportFragmentManager.beginTransaction().replace(R.id.newEstimateLayout, fragment)
				.addToBackStack(null).commit()
		}



		binding.btnTemplate.setOnClickListener {
			// prevent double-tap

			GlobalScope.launch {

				val lifecyclejob: Job = lifecycleScope.launch(Dispatchers.IO) {


					withContext(Dispatchers.Main) {
						Toast.makeText(this@MainActivity, "Coming soon ...", Toast.LENGTH_LONG)
							.show()
					}


				}
				lifecyclejob.join()
			}
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




		binding.btnclientInfo.setOnClickListener {

			lifecycleScope.launch {
				val intent = Intent(applicationContext, ClientActivity::class.java)
				startActivity(intent)

			}
		}
		binding.businessimage?.setOnClickListener {
			Toast.makeText(applicationContext, "coming soon...", Toast.LENGTH_LONG).show()
		}

		val cardviewButton = binding.additemscardView

		cardviewButton.setOnClickListener {
			// Get IDs from intent (or any other source)
			val estimateId = intent.getIntExtra("estimate_id", -1)
			val customerId = intent.getIntExtra("customerId", -1)

			// Log for debugging
			Log.d("InvoiceDebug", "Passing estimateId = $estimateId, customerId = $customerId to ProductsInfoFragment")

			// Create fragment with arguments
			val productfrag = ProductsInfoFragment().apply {
				arguments = Bundle().apply {
					putInt("estimate_id", estimateId)
					putInt("customerId", customerId)
				}
			}

			// Navigate to the fragment
			supportFragmentManager.beginTransaction()
				.replace(R.id.newEstimateLayout, productfrag)
				.addToBackStack(null)
				.commit()
		}


		val addBtn_image = binding.addbuttonImage
		addBtn_image.setOnClickListener {
			cardviewButton.performClick()
		}
		val db = DatabaseHandler(applicationContext)


//set the text view to
		itemsCount = binding.tvItems
		//	    function to count items number inserted
		fun countItems() {
			itemsCount.text = "Items No: [${setupListintoRecycleview()}]"
		}
		countItems()
		// function to display totals in the textview
		fun txtViewTotals() {
			val stringFomat = "%,.2f"
			val itemSubtal = binding.esumSubtotal
			val itemsTotal = binding.esumTotal
			Subtotal = GlobalFunck()
			val stringSubtotal =
				stringFomat.format(Math.floor(Subtotal.getSubTotal(applicationContext).toDouble()))
			itemSubtal.text = stringSubtotal

			Total = GlobalFunck()
			val stringTotal = stringFomat.format(
				Math.floor(
					Total.summationofTotal(applicationContext).toDouble()
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

	private fun setupListintoRecycleview():Int {
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


	override fun onResume() {
		super.onResume()
		setupListintoRecycleview()
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
							try {
								// Switch to IO thread for file operations
								withContext(Dispatchers.IO) {
									PdfUtils.previewPdfFormat(
										this@MainActivity,
										MainActivity.estimatePdf(this@MainActivity)
									)
								}
							} catch (e: Exception) {
								e.printStackTrace()
								// Show Toast safely on main thread
								Toast.makeText(
									this@MainActivity,
									"Failed to open PDF",
									Toast.LENGTH_SHORT
								).show()
							}
						}
					}, shape = RoundedCornerShape(50), colors = ButtonDefaults.textButtonColors(
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
					onClick = { /* Handle save process */
						lifecycleScope.launch(Dispatchers.IO) {
							try {

								val intent =
									Intent(applicationContext, Save_previewActivity::class.java)
								startActivity(intent)
								withContext(Dispatchers.Main) {
									Toast.makeText(
										this@MainActivity, " Success...", Toast.LENGTH_LONG
									)

								}
							} catch (e: Exception) {
								e.printStackTrace()
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
		private lateinit var file: File

		//set the fonts for the pdf sections
		fun getPdfFontFromAssets(context: Context): PdfFont {
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


		fun estimatePdf(context: Context): File {

			val clientId = GlobalFunck().safeClientId(context)
			val clientNumber = GlobalFunck()

			val estimateTitle = GlobalFunck().titleINV(context)
			// for the invoice to be counted from number index 0+1 to avoid the zero client
			val invoiceNumber = GlobalFunck().id(context)
			var creationDate = GlobalFunck().creationDate(context)

			if (creationDate.isEmpty()) {
				creationDate = LocalDate.now().toString()
			}

			val toLocalDate = try {
				if (creationDate.matches(Regex("\\d+"))) {
					// If it's a numeric timestamp, convert from milliseconds
					Instant.ofEpochMilli(creationDate.toLong()).atZone(ZoneId.systemDefault())
						.toLocalDate()
				} else {
					// Otherwise, parse as a normal ISO date string
					LocalDate.parse(creationDate)
				}
			} catch (e: Exception) {
				// Fallback to current date if parsing fails
				LocalDate.now()
			}


			val duedate = GlobalFunck().calculateDueDate(toLocalDate, dueTerms)
			val clientName = GlobalFunck().safeClientName(context)

			if (clientName.isNotEmpty()) {
				clientName.plus("$invoiceNumber")
			}
			try {

				// File path in Downloads folder
				val path =
					Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
						.toString()
				file = File(path, "$invoiceNumber-$clientId $clientName.pdf")

				// iText 7 writing
				val writer = PdfWriter(file)
				val pdfDocument = PdfDocument(writer)
				pdfDocument.defaultPageSize = PageSize.A4
				val document = Document(pdfDocument)
				val pdfCanvas = PdfCanvas(pdfDocument.addNewPage())


//            create the image path

				val width = PageSize.A4.width
				val height = PageSize.A4.height
				// 1. Load image from drawable
				val drawablepic = ContextCompat.getDrawable(context, R.drawable.backpdf)
				val bitmap1 = (drawablepic as BitmapDrawable).bitmap

				// 2. Convert Bitmap to ByteArray
				val stream = ByteArrayOutputStream()
				bitmap1.compress(Bitmap.CompressFormat.PNG, 100, stream)
				val imageBytes = stream.toByteArray()

				// 3. Create ImageData from ByteArray   // Load image
				val imageDatafromArr = ImageDataFactory.create(imageBytes)

				// 4. Draw background image on canvas
				pdfCanvas.addImageFittedIntoRectangle(
					imageDatafromArr, Rectangle(0f, 0f, width, height), false
				)
				// draw horizontal line on the top section of header

				pdfCanvas.setLineWidth(0.06f)

				pdfCanvas.moveTo(50.00, 689.00)
				pdfCanvas.lineTo(544.00, 689.00)
				pdfCanvas.stroke().setStrokeColor(DeviceRgb(47, 59, 81))

//       1. Define Table Headers: and its properties the font color background colour and border

				val latoRegular = latoRegularFont(context)
				val queensFont = queensFont(context)
				val gerhanaFont = getPdfFontFromAssets(context)
				val latobold = latoBold(context)
				val textColor = DeviceRgb(44, 45, 47)

//            the solid border color resource custom defined
				val border = SolidBorder(DeviceRgb(224, 224, 244), 0.9f)
//            where to start the table at
				val tablemargintop = 202f
				//            create columnwidth dimension
				val columnWidth = floatArrayOf(221.5f, 62.75f, 73.5f, 47f, 100f)

				val table = Table(columnWidth)  //table having column widths
					.setWidth(UnitValue.createPointValue(columnWidth.sum()))  // full width

					.setBorderLeft(border).setBorderRight(border).setBorderBottom(border)
					.setBorderTop(Border.NO_BORDER).setFontSize(11f).setFont(gerhanaFont)
					.setFontColor(textColor).setMarginTop(tablemargintop)

				// Add header cells
				val headers = listOf("DESCRIPTION", "QUANTITY", "PRICE", "TAX", "AMOUNT")


				headers.forEach { head ->

					table.addCell(
						Cell().setBorder(Border.NO_BORDER).add(
							Paragraph(head).setFont(latobold).setBold()
								.setFontColor(ColorConstants.WHITE)
						).setBackgroundColor(DeviceRgb(62, 140, 202)) // blue header background

					)
				}

				headers.first()


// Images to the page

				val drawable =
					ContextCompat.getDrawable(context, R.drawable.infieldlogo) as BitmapDrawable
				val bitmap = drawable.bitmap

//     // Save bitmap to a temporary file
				val imagefile = File(context.cacheDir, "infieldlogo.png")
				val outputStream = FileOutputStream(imagefile)
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
				outputStream.flush()
				outputStream.close()

				// Use the file path in iText
				val imageData = ImageDataFactory.create(imagefile.absolutePath)
				val image =
					Image(imageData).scaleToFit(100f, 100f).setFixedPosition(15f, 767.8f, 350f)
				document.add(image)

//            3. Populate the Table with Rows
				val items = DatabaseHandler(context).viewProduct()
				items.forEach { item ->
					//Loop through and calculate amount = price × quantity:
					val amount = item.price * item.quantity
					// create a border with a custom colour lighter grey-blue and width weight 0.9

					val cell1 = Cell().add(
						Paragraph(item.itemName.uppercase()).setBorder(Border.NO_BORDER)
							.setTextAlignment(TextAlignment.LEFT)
					).setBorder(border)


					val cell2 = Cell().add(
						Paragraph(item.quantity.toString()).setBorder(Border.NO_BORDER)
							.setBorder(Border.NO_BORDER).setFontColor(textColor)
							.setTextAlignment(TextAlignment.CENTER)
					).setBorder(border)


					val cell3 = Cell().add(
						Paragraph("${"%,.2f".format(item.price)}").setBorder(Border.NO_BORDER)
							.setBorder(Border.NO_BORDER).setFontColor(textColor)
							.setTextAlignment(TextAlignment.RIGHT)
					).setBorder(border)


					val cell4 = Cell().add(
						Paragraph(item.tax.toString()).setBorder(Border.NO_BORDER)
							.setFontColor(textColor).setTextAlignment(TextAlignment.CENTER)
					).setBorder(border)

					val cell5 = Cell().add(
						Paragraph("${"%,.2f".format(amount)}").setBorder(Border.NO_BORDER)
							.setFontColor(textColor).setTextAlignment(TextAlignment.RIGHT)
					).setBorder(border)

					table.addCell(cell1)
					table.addCell(cell2)
					table.addCell(cell3)
					table.addCell(cell4)
					table.addCell(cell5)
				}
// the header sections


//			call the image of qr function to be used in the itext pdf
				val docName = "CLIENT_iNFIELDER $estimateTitle"

				val qrImage = Templatepdf1().qrcodeGenerator(docName)
				document.add(qrImage.setFixedPosition(1, 431.21f, 685f))

				document.add(
					Paragraph("INFIELD").setBold()
						.setFontColor(DeviceRgb(62, 140, 202)) // infiellt tee blue
						.setFontSize(18f).setFixedPosition(146f, 784.8f, 84f)
				)
				document.add(
					Paragraph("ENGINEERING").setBold()
						.setFontColor(DeviceRgb(67, 105, 45)) // engineering jungle green
						.setFontSize(18f).setFixedPosition(223.1f, 784.8f, 134f)
				)
				document.add(
					Paragraph("SERVICES LTD").setBold()
						.setFontColor(DeviceRgb(62, 140, 202)) // infield tee blue
						.setFontSize(19f).setFixedPosition(146f, 756.8f, 144f)
				)
				document.add(
					Paragraph("P.O BOX 62700-19052").setFontColor(
						DeviceRgb(
							47, 59, 81
						)
					) // address dark blue
						.setFontSize(16f).setFixedPosition(146f, 737.3f, 184f).setFont(latoRegular)

				)
				document.add(
					Paragraph("Meru Township").setFontColor(
						DeviceRgb(
							47, 59, 81
						)
					) // address dark blue
						.setFontSize(16f).setFixedPosition(146f, 718.3f, 184f).setFont(latoRegular)
				)

				document.add(
					Paragraph("0701243548").setFontColor(DeviceRgb(47, 59, 81)) // address dark blue
						.setFontSize(16f).setFixedPosition(146f, 696.1f, 164f).setFont(latoRegular)

				)

				document.add(
					Paragraph(
						"BILL TO: \n" + "$clientName"
					).setFontColor(DeviceRgb(47, 59, 81)) // address dark blue
						.setFontSize(12.5f)

						.setFont(latobold).setFixedPosition(35.7f, 650f, 164f)

				)

				document.add(
					Paragraph("QUOTE NUMBER: ").setFontColor(
						DeviceRgb(
							47, 59, 81
						)
					) // address dark blue
						.setFontSize(13f)

						.setFont(latobold).setFixedPosition(370.2F, 669f, 164f)


				)
				document.add(
					//Set] the Quote number
					Paragraph("QTE-INF-#$invoiceNumber").setFontColor(
						DeviceRgb(
							47, 59, 81
						)
					) // address dark blue
						.setFontSize(13f).setFont(queensFont).setFixedPosition(500.2F, 669f, 164f)


				)

				document.add(
					Paragraph("CREATION DATE: ").setFontColor(
						DeviceRgb(
							47, 59, 81
						)
					) // address dark blue
						.setFontSize(13f)

						.setFont(latobold).setFixedPosition(370.2F, 653.1f, 164f)


				)
				document.add(
					Paragraph("$creationDate ").setFontColor(
						DeviceRgb(
							47, 59, 81
						)
					) // address dark blue
						.setFontSize(13f).setFont(queensFont).setFixedPosition(500.2F, 653.1f, 164f)


				)

				document.add(
					Paragraph("DUE DATE: ").setFontColor(DeviceRgb(47, 59, 81)) // address dark blue
						.setFontSize(13f).setFont(latobold).setFixedPosition(370.2F, 635.1f, 164f)


				)

				document.add(
					Paragraph("$duedate").setFontColor(DeviceRgb(47, 59, 81)) // address dark blue
						.setFontSize(13f).setFont(queensFont).setFixedPosition(500.2F, 635.1f, 164f)
				)

				document.add(
					Paragraph("ESTIMATE").setFontColor(DeviceRgb(156, 39, 176)) // address dark blue
						.setFontSize(30.5f).setFont(latobold).setFixedPosition(422.2f, 776.8f, 166f)
				)

				document.add(table)
				val amountCell = Table(floatArrayOf(93f, 130f))
				val amountTotal = mutableListOf("Subtotal", "0.0", "Vat", "0.0", "Total", "0.0")

				amountTotal.forEach {
//function to sum total of items from the database
					val subtotal = GlobalFunck().getSubTotal(context)
					val textformat = "%,.2f"
					val objTax = GlobalFunck().summationOfTax(context)
					var objTotal = GlobalFunck().summationofTotal(context)


					//we are debugging here FOR THE CALCULATION OF PRICES AND TOTALS
					amountTotal[1] = "${textformat.format(Math.ceil(subtotal.toDouble()))}"
					amountTotal[3] = "${textformat.format(Math.ceil(objTax))}"
					amountTotal[5] = "${textformat.format(Math.ceil(objTotal.toDouble()))}"




					amountCell.addCell(
						Cell().add(Paragraph(it)).setHeight(18f).setFontSize(12f)
							.setBorder(Border.NO_BORDER).setFont(latobold)
					).setFontColor(ColorConstants.BLACK).setBackgroundColor(DeviceRgb(62, 140, 202))
						.setBorder(Border.NO_BORDER).setMarginLeft(284f).setMarginTop(30f)
				}
				document.add(amountCell)
				val mpesaInfo = Table(floatArrayOf(178.75f, 164.75f, 168f))
				val mpesa = listOf("529901", "ACCOUNT NO", "5021964795002")
				mpesa.forEach {
					mpesaInfo.addCell(
						Cell().setBorder(Border.NO_BORDER)
							.setBackgroundColor(DeviceRgb(62, 140, 202)).add(Paragraph(it))
					).setFontColor(ColorConstants.WHITE).setHeight(35f).setFont(gerhanaFont)
						.setFontColor(ColorConstants.WHITE)

				}


				val bankInfoTable = Table(floatArrayOf(145.75f, 120.75f, 120.75f, 125.75f))

				val aboutDetailing = Table(floatArrayOf(135.15f))
				val detailHeading = listOf("BANK DETAIL")

				val mpesadetail = Table(floatArrayOf(135.15f))
				val detailHeading2 = listOf("M-PESA PAYBILL")


				val dH2 = detailHeading2.first()


				dH2.forEach {
					mpesadetail.addCell(
						Cell().setBorder(Border.NO_BORDER).add(Paragraph(dH2))
							.setBackgroundColor(DeviceRgb(62, 140, 202))
					).setFontColor(ColorConstants.WHITE).setHeight(35f).setFont(gerhanaFont)
						.setMarginTop(10f)

				}

				detailHeading.forEach {
					aboutDetailing.addCell(
						Cell().add(Paragraph(it).setBorder(Border.NO_BORDER)).setPaddingTop(15f)
					).setBackgroundColor(DeviceRgb(62, 140, 202)).setFontColor(ColorConstants.WHITE)
						.setFont(gerhanaFont).setBorder(Border.NO_BORDER).setPaddingTop(20f)

				}


				val infosBank = listOf("ACCOUNT NAME", "BANK NAME", "BRANCH", "ACCOUNT NO")
				val bankdetail =
					listOf("INFIELD ENGINEERING LTD", "KINGDOM BANK", "NAIROBI", "5021964795002")

				infosBank.forEach {
					bankInfoTable.addCell(
						Cell().setFontColor(ColorConstants.WHITE).add(Paragraph(it))
							.setBackgroundColor(DeviceRgb(62, 140, 202)).setBorder(Border.NO_BORDER)
							.setFont(gerhanaFont)

					).setMarginTop(20f)

				}

				bankdetail.forEach {
					bankInfoTable.setBorder(Border.NO_BORDER)
					bankInfoTable.addCell(
						Cell().setBorderLeft(SolidBorder(DeviceRgb(224, 224, 224), 0.04f))
							.setBorderBottom(SolidBorder(DeviceRgb(224, 224, 224), 0.08f))
							.setFontColor(ColorConstants.BLACK).add(Paragraph(it))
							.setBorderRight(SolidBorder(DeviceRgb(224, 224, 224), 0.04f))
							.setFontSize(11f)

					).setMarginTop(0.01f).setFont(latobold)
				}
				infosBank.last()

				// Add footer event handler by calling the handler created in the clientsCreation file
				pdfDocument.addEventHandler(PdfDocumentEvent.END_PAGE, FooterEvent())

				document.add(aboutDetailing)
				document.add(bankInfoTable)
				document.add(mpesadetail)
				document.add(mpesaInfo)


				//document metadata
				pdfDocument.documentInfo.setAuthor("nelvinKelly@gmail.com").setTitle("Estimates")
					.addCreationDate().setCreator("" + "Megistanas Developers")

				document.close()

				println("PDF generated at: ${file.absolutePath}")

				Toast.makeText(context, "Download Success...", Toast.LENGTH_LONG)
			} catch (e: Exception) {
				e.printStackTrace()
			}

			return file
		}
	}

	//function to show list of inserted data in the recycler view
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
		} else {
			binding.recycleItem.visibility = View.GONE
		}

		// ✅ Return number of items currently displayed
		return itemList.size
	}

	//    function to get the items list
	private fun getItemlist(): ArrayList<ModelClass> {
//        create instance of the databaseHandler class
		val databaseHandler: DatabaseHandler = DatabaseHandler(this)
//          calling the viewProduct  of DatabaseHandler class to read the list
		return databaseHandler.viewProduct()

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
//			display the live amount in the text view
			amountScreen?.text = "Ksh: $total"
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
				id = modelClass.id,  // keep same ID
				itemName = updatedName,
				quantity = updatedQuantity,
				price = updatedPrice,
				total = (updatedQuantity * updatedPrice).toFloat(),
				tax = updatedTax
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
					modellist.tax
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
