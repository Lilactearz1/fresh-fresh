package com.movix.transak_infield

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.time.LocalDate


//creating a database logic that extends the SQLiteOpenHelper base class

class DatabaseHandler(context: Context) :
	SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {


	companion object {
		private const val DATABASE_VERSION = 39
		private const val DATABASE_NAME = "Transak_infield.db"

		private const val INVOICE_TABLE = "TableInvoice"
		private const val KEY_ID = "_id"
		private const val KEY_NAME = "items_name"
		private const val KEY_QUANTITY = "item_quantity"
		private const val KEY_PRICE = "item_price"
		private const val KEY_ITEM_TOTAL = "item_total"
		private const val KEY_TAX = "item_tax"

		private const val CUSTOMER_TABLE = "TableCustomer"
		private const val CUSTOMER_ID = "customer_id"
		private const val CUSTOMER_PHONE = "customer_phone"
		private const val CUSTOMER_NAME = "customer_name"

		private const val ESTIMATE_TABLE = "Estimates"
		private const val ESTIMATE_TITLE = "Title"
		private const val ESTIMATE_ID = "estimate_id"
		private const val ESTIMATE_DATE = "created_date"

		private const val STATUS ="status"

		private const val DUE_DATE = "due_date"



	}


	override fun onCreate(db: SQLiteDatabase?) {
//        creating table with fields /COLUMNS WITH THE NAME TEXT TYPE: INTEGER ,TEXT ,CHAR
//        CREATE TABLE called TableInvoice(param1,param2,param3,.....)

		val CREATE_CUSTOMERS_TABLE =
			("CREATE TABLE " + CUSTOMER_TABLE + " (" + CUSTOMER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + CUSTOMER_NAME + " VARCHAR(50), " + CUSTOMER_PHONE + " TEXT" +
					")")

		val CREATE_ESTIMATE_TABLE =
			("CREATE TABLE " + ESTIMATE_TABLE + " ("
					+ ESTIMATE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ ESTIMATE_TITLE + " VARCHAR(200), "
					+ ESTIMATE_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP, "
					+ DUE_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP, "
					+ STATUS + " TEXT, "
					+ CUSTOMER_ID + " INTEGER NULL, " +
					"FOREIGN KEY(" + CUSTOMER_ID + ") REFERENCES " + CUSTOMER_TABLE + "(" + CUSTOMER_ID + ") ON DELETE SET NULL" + ")")


		val CREATE_PRODUCTS_TABLE = """
    CREATE TABLE $INVOICE_TABLE (
        $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
        $KEY_NAME TEXT,
        $KEY_QUANTITY INTEGER,
        $KEY_PRICE REAL,
        $KEY_ITEM_TOTAL REAL,
        $KEY_TAX REAL,
        $CUSTOMER_ID INTEGER NOT NULL,
        $ESTIMATE_ID INTEGER NOT NULL,
        FOREIGN KEY($CUSTOMER_ID) REFERENCES $CUSTOMER_TABLE($CUSTOMER_ID) ON DELETE CASCADE,
        FOREIGN KEY($ESTIMATE_ID) REFERENCES $ESTIMATE_TABLE($ESTIMATE_ID) ON DELETE CASCADE
    )
""".trimIndent()


//        tell the database(db?) to go ahead and execute SQL (execSQL)

		db?.execSQL(CREATE_CUSTOMERS_TABLE)


		db?.execSQL("INSERT OR IGNORE INTO $CUSTOMER_TABLE ($CUSTOMER_ID, $CUSTOMER_NAME, $CUSTOMER_PHONE) VALUES (1, 'Guest Customer', 'N/A')")

		db?.execSQL(CREATE_ESTIMATE_TABLE)
		db?.execSQL(CREATE_PRODUCTS_TABLE)

	}

	override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
//		you can omit !! since the db is not null at this pointThe system always calls it with a valid db instance, so it’s safe to use db!! once, or just assume it’s non-null and skip !! entirely (which is better).
		  // Drop child first
		db!!.execSQL("DROP TABLE IF EXISTS $INVOICE_TABLE")
		db.execSQL("DROP TABLE IF EXISTS $ESTIMATE_TABLE")
		db.execSQL("DROP TABLE IF EXISTS $CUSTOMER_TABLE")


		onCreate(db)
	}

//	method to add customers info into the database

	fun addClientsInformations(clientsCreation: ClientsCreation): Long {
		val db = this.writableDatabase
		val contentValues = ContentValues()

		contentValues.put(CUSTOMER_NAME, clientsCreation.name)
		contentValues.put(CUSTOMER_PHONE, clientsCreation.phone)
		val infosSuccessId = db.insertOrThrow(CUSTOMER_TABLE, null, contentValues)

		db.close()
		return infosSuccessId // ✅ This is the customerId you want to link the estimates and products
	}

	// update customer infos
	fun updateClientsInfos(clientsCreation: ClientsCreation): Int {
		val db = this.writableDatabase
		val contentValues = ContentValues()
//		 Note: It’s not wrong to include CUSTOMER_ID in put() again, but it’s not necessary when updating by it.
//		contentValues.put(CUSTOMER_ID,clientsCreation.id)
		contentValues.put(CUSTOMER_NAME, clientsCreation.name)
		contentValues.put(CUSTOMER_PHONE, clientsCreation.phone)
		val updateSuccess =
			db.update(CUSTOMER_TABLE, contentValues, "$CUSTOMER_ID=" + clientsCreation.id, null)

		db.close()
		return updateSuccess
	}

	//get latest client
	fun getLatestCustomerId(): Int {
		val db = this.readableDatabase
		val query = "SELECT $CUSTOMER_ID FROM $CUSTOMER_TABLE ORDER BY $CUSTOMER_ID DESC LIMIT 1"
		val cursor = db.rawQuery(query, null)

		val id = if (cursor.moveToFirst()) {
			cursor.getInt(cursor.getColumnIndexOrThrow(CUSTOMER_ID))
		} else {
			0
		}

		cursor.close()
		db.close()
		return id
	}

	//view clients information's
	fun viewClientsInfo(): ArrayList<ClientsCreation> {
		val nameList: ArrayList<ClientsCreation> = ArrayList()

//       the select query gives all the data present in our table
		val selectQuery = "SELECT * FROM $CUSTOMER_TABLE"
		val db = this.readableDatabase
//       the cursor starts at null point
		var cursor: Cursor? = null

//       we try to fill the cursor with a raw query which will try to
//       run the selectquery into our database and a null for no specific selection we need
		try {
			cursor = db.rawQuery(selectQuery, null)
		} catch (e: SQLiteException) {
			db.execSQL(selectQuery)
			return ArrayList()
		}
//		create a variable for different columns
		var id: Int
		var customer_name: String
		var customer_phone: String
// move through the cursor
		if (cursor.moveToFirst()) {
			do {
				id = cursor.getInt(cursor.getColumnIndexOrThrow(CUSTOMER_ID))
				customer_name = cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_NAME))
				customer_phone = cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_PHONE))

				val clientcreation = ClientsCreation(
					id = id,
					name = customer_name,
					phone = customer_phone
				)
				nameList.add(clientcreation)

			} while (cursor.moveToNext())

		}
		cursor.close()
		db.close()
		return nameList
	}

	fun addEstimateInfo(estimateinfo: Estimateinfo): Long {
		val db = this.writableDatabase
		val contentValues = ContentValues()

		// Do NOT include estimate_id; SQLite auto-generates it
		contentValues.put(ESTIMATE_TITLE, estimateinfo.titleINV)
		contentValues.put(ESTIMATE_DATE, estimateinfo.creationDate)
		contentValues.put(DUE_DATE, estimateinfo.dueDate)

		if (estimateinfo.customerId != null && estimateinfo.customerId != 0) {
			contentValues.put(CUSTOMER_ID, estimateinfo.customerId)
		}

		contentValues.put(STATUS, estimateinfo.status.name) // ✅ Save enum value

		var result: Long = -1
		try {
			result = db.insert(ESTIMATE_TABLE, null, contentValues)
			Log.d("DatabaseHandler", "Inserted estimate with ID: $result, values=$contentValues")
		} catch (e: Exception) {
			Log.e("DatabaseHandler", "Error inserting estimate: ${e.message}")
		} finally {
			db.close()
		}

		return result
	}


	//update estimate infos

	fun updateEstimateInfo(estimateinfo: Estimateinfo): Int {
		val db = this.writableDatabase
		val contentValues = ContentValues().apply {
			put(ESTIMATE_TITLE, estimateinfo.titleINV)
			put(ESTIMATE_DATE, estimateinfo.creationDate)
			put(DUE_DATE, estimateinfo.dueDate)

		}

		val rowsUpdated = db.update(
			ESTIMATE_TABLE,
			contentValues,
			"$ESTIMATE_ID = ?",
			arrayOf(estimateinfo.estimateId.toString())
		)

		db.close()
		return rowsUpdated
	}



	//    method to add data to the database
	fun addProductToDatabase(modelClass: ModelClass): Long {

		// Guard against invalid foreign keys
		if (modelClass.customerId == 0 || modelClass.estimateId == 0) {
			throw IllegalArgumentException(
				" Cannot insert product without valid customerId & estimateId (received customerId=${modelClass.customerId}, estimateId=${modelClass.estimateId})"
			)
		}

		val db = this.writableDatabase
		val contentValues = ContentValues()
		contentValues.put(KEY_NAME, modelClass.itemName) // put itemName .quantity, price,total
		contentValues.put(KEY_QUANTITY, modelClass.quantity)
		contentValues.put(KEY_PRICE, modelClass.price)
		contentValues.put(KEY_TAX, modelClass.tax)
		contentValues.put(KEY_ITEM_TOTAL, modelClass.total)
		contentValues.put(ESTIMATE_ID, modelClass.estimateId)
		contentValues.put(CUSTOMER_ID, modelClass.customerId)


//        inserting rows
		val insertSuccess = db.insert(INVOICE_TABLE, null, contentValues)
//        second param2 is a string containing nullColumnHack
			Log.d("EstimateDebug", "Inserting estimate: customerId=${modelClass.customerId}")

		db.close()  //close the database connection

		return insertSuccess
	}

	//method to read data from the database
	fun viewProduct(): ArrayList<ModelClass> {
		val productList: ArrayList<ModelClass> = ArrayList()

//       the select query gives all the data present in our table
		val selectQuery = "SELECT * FROM $INVOICE_TABLE"
		val db = this.readableDatabase
//       the cursor starts at null point
		var cursor: Cursor? = null

//       we try to fill the cursor with a raw query which will try to
//       run the selectquery into our database and a null for no specific selection we need
		try {
			cursor = db.rawQuery(selectQuery, null)
		} catch (e: SQLiteException) {
			db.execSQL(selectQuery)
			return ArrayList()
		}
//       create a variable for different columns
		var id: Int
		var quantity: Int
		var itemName: String
		var price: Double
		var total: Float
		var tax: Float
		var estimateId:Int
		var customerId:Int


//      Move through the cursor
		if (cursor.moveToFirst()) {
			do {
				id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID))
				itemName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME))
				quantity = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_QUANTITY))
				price = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_PRICE))
				total = cursor.getFloat(cursor.getColumnIndexOrThrow(KEY_ITEM_TOTAL))
				tax = cursor.getFloat(cursor.getColumnIndexOrThrow(KEY_TAX))
				estimateId= cursor.getInt(cursor.getColumnIndexOrThrow(ESTIMATE_ID))
				customerId = cursor.getInt(cursor.getColumnIndexOrThrow(CUSTOMER_ID))


				val modelClass1 = ModelClass(
					id = id,
					quantity = quantity,
					itemName = itemName,
					price = price,
					total = total,
					tax = tax,
					estimateId = estimateId,
					customerId = customerId
				)

				productList.add(modelClass1)


			} while (cursor.moveToNext())
		}
		Log.d("DatabaseHandler", "Fetched ${productList.size} items from DB")
		cursor.close()
		db.close()
		return productList
	}

	//    function to update the records
	fun updateRecords(modelClass: ModelClass): Int {
		val db = this.writableDatabase
		val contentValues = ContentValues()
		contentValues.put(KEY_NAME, modelClass.itemName) // put itemName .quantity, price,total
		contentValues.put(KEY_QUANTITY, modelClass.quantity)
		contentValues.put(KEY_PRICE, modelClass.price)
		contentValues.put(KEY_TAX, modelClass.tax)
		contentValues.put(KEY_ITEM_TOTAL, modelClass.total)
		contentValues.put(CUSTOMER_ID, modelClass.customerId)
		contentValues.put(ESTIMATE_ID, modelClass.estimateId)


//    updating rows

		val successUpdate =
			db.update(INVOICE_TABLE, contentValues, KEY_ID + "=" + modelClass.id, null)
// the key id is used to update the specific id of the row selected even if there are other similar product

		db.close()
		return successUpdate
	}

	//    delete the records
	fun deleteRecords(modelClass: ModelClass): Int {
		val db = this.writableDatabase
		val contentValues = ContentValues()
		contentValues.put(KEY_ID, modelClass.id) // model class id

		val successDelete = db.delete(INVOICE_TABLE, KEY_ID + "=" + modelClass.id, null)
		db.close()

		return successDelete
	}

	fun viewEstimateInfo(): ArrayList<Estimateinfo> {
		val estimate: ArrayList<Estimateinfo> = ArrayList()
		val db = this.readableDatabase
		val selectQuery = "SELECT * FROM $ESTIMATE_TABLE ORDER BY $ESTIMATE_ID DESC"    // Order by latest first"
		var cursor: Cursor? = null

		try {
			cursor = db.rawQuery(selectQuery, null)

		} catch (e: SQLiteException) {
			db.execSQL(selectQuery)
			return estimate
		}
//		create a variable for differnt columns
		var id: Int
		var title: String
		var creatDate: String
		var dueDate: String
		var customerId: Int

		if (cursor.moveToFirst())
			do {
				id = cursor.getInt(cursor.getColumnIndexOrThrow(ESTIMATE_ID))
				title = cursor.getString(cursor.getColumnIndexOrThrow(ESTIMATE_TITLE))
				creatDate = cursor.getString(cursor.getColumnIndexOrThrow(ESTIMATE_DATE))
				dueDate = cursor.getString(cursor.getColumnIndexOrThrow(DUE_DATE))
				customerId = cursor.getInt(cursor.getColumnIndexOrThrow(CUSTOMER_ID))
				val result = Estimateinfo(
					estimateId = id,
					titleINV = title,
					creationDate = creatDate,
					dueDate = dueDate,
					customerId = customerId
				)

				estimate.add(result)
			} while (cursor.moveToNext())

		cursor.close()
		db.close()
		return estimate
	}

	override fun onConfigure(db: SQLiteDatabase?) {
		super.onConfigure(db)
		db?.setForeignKeyConstraintsEnabled(true)
	}

	fun getAllEstimate(): MutableList<Estimateinfo> {
		val estimates = mutableListOf<Estimateinfo>()
		val db = this.readableDatabase
		val cursor = db.rawQuery("SELECT * FROM $ESTIMATE_TABLE", null)

		if (cursor.moveToFirst()) {
			do {
				val id = cursor.getInt(cursor.getColumnIndexOrThrow(ESTIMATE_ID))
				val title = cursor.getString(cursor.getColumnIndexOrThrow(ESTIMATE_TITLE))
				val createdDate = cursor.getString(cursor.getColumnIndexOrThrow(ESTIMATE_DATE))
				val estimateDue = cursor.getString(cursor.getColumnIndexOrThrow(DUE_DATE))
				val customerId = cursor.getInt(cursor.getColumnIndexOrThrow(CUSTOMER_ID))
				estimates.add(Estimateinfo(id, title, createdDate, estimateDue, customerId))
			} while (cursor.moveToNext())
		}

		cursor.close()
		db.close()
		return estimates
	}

	// this is similar to the vieProducts method
	fun getItemsForCustomer(customerId: Int): ArrayList<ModelClass> {
		val items = ArrayList<ModelClass>()
		val db = this.readableDatabase
		val cursor = db.rawQuery(
			"SELECT * FROM $INVOICE_TABLE WHERE $CUSTOMER_ID= ?",
			arrayOf(customerId.toString())
		)

		if (cursor.moveToFirst()) {
			do {
				val id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID))
				val itemName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME))
				val quantity = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_QUANTITY))
				val price = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_PRICE))
				val total = cursor.getFloat(cursor.getColumnIndexOrThrow(KEY_ITEM_TOTAL))
				val tax = cursor.getFloat(cursor.getColumnIndexOrThrow(KEY_TAX))
				val customerId = cursor.getInt(cursor.getColumnIndexOrThrow(CUSTOMER_ID))
				val estimateId=cursor.getInt(cursor.getColumnIndexOrThrow(ESTIMATE_ID))
				items.add(ModelClass(id, quantity, itemName, price, total, tax, customerId,estimateId))
			} while (cursor.moveToNext())
		}

		cursor.close()
		db.close()
		return items
	}


	fun getItemsForEstimate(estimateId: Int, customerId: Int): ArrayList<ModelClass> {
		val itemsList = ArrayList<ModelClass>()
		val db = this.readableDatabase
		val query = "SELECT * FROM $INVOICE_TABLE WHERE $ESTIMATE_ID = ? AND $CUSTOMER_ID = ?"
		val cursor = db.rawQuery(query, arrayOf(estimateId.toString(), customerId.toString()))

		if (cursor.moveToFirst()) {
			do {
				val item = ModelClass(
					id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
					itemName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)),
					quantity = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_QUANTITY)),
					price = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_PRICE)),
					total = cursor.getFloat(cursor.getColumnIndexOrThrow(KEY_ITEM_TOTAL)),
					tax = cursor.getFloat(cursor.getColumnIndexOrThrow(KEY_TAX)),
					customerId = customerId,
					estimateId = cursor.getInt(cursor.getColumnIndexOrThrow(ESTIMATE_ID)),
				)
				itemsList.add(item)
			} while (cursor.moveToNext())
		}

		cursor.close()
		db.close()
		return itemsList
	}




	//	 Query with a join to see linked data
	fun linkedData(): List<Estimateinfo> {
		val linkedlist = mutableListOf<Estimateinfo>()
		val db = readableDatabase
		val query =
			""" SELECT e.${ESTIMATE_ID}, e.${ESTIMATE_TITLE}, e.${ESTIMATE_DATE}, e.${DUE_DATE},c.${CUSTOMER_NAME} AS $CUSTOMER_NAME, c.${CUSTOMER_PHONE} AS  $CUSTOMER_PHONE
FROM $ESTIMATE_TABLE e
JOIN $CUSTOMER_TABLE c
ON e.$CUSTOMER_ID= c.$CUSTOMER_ID """.trimIndent()
		val cursor = db.rawQuery(query, null)
//	 create variables for table columns
		var estimateId: Int
		var title: String
		var createdDate: String
		var dueDate: String
		var customerId: Int


		if (cursor.moveToFirst()) {
			do {
				estimateId = cursor.getInt(cursor.getColumnIndexOrThrow(ESTIMATE_ID))
				title = cursor.getString(cursor.getColumnIndexOrThrow(ESTIMATE_TITLE))
				createdDate = cursor.getString(cursor.getColumnIndexOrThrow(ESTIMATE_DATE))
				dueDate = cursor.getString(cursor.getColumnIndexOrThrow(DUE_DATE))
				customerId = cursor.getInt(cursor.getColumnIndexOrThrow(CUSTOMER_ID))

				val list = Estimateinfo(
					estimateId = estimateId,
					titleINV = title,
					creationDate = createdDate,
					dueDate = dueDate,
					customerId = customerId,

					)
				linkedlist.add(list)
			} while (cursor.moveToNext())

		}
		cursor.close()
		db.close()
		return linkedlist
	}
// function to close the  current estimate database
	fun closeEstimate(estimateId: Long) {
		val db = writableDatabase
		val values = ContentValues().apply {
			put(STATUS, EstimateStatus.COMPLETED.name)
		}
		db.update(ESTIMATE_TABLE, values, "$ESTIMATE_ID=?", arrayOf(estimateId.toString()))
	}

	fun createNewEstimate(context: Context, customerId: Int,title:String ): Long {
		val db = writableDatabase
		val values = ContentValues().apply {
			put(ESTIMATE_TITLE, title)

			if (customerId != 0)
				put(CUSTOMER_ID, customerId)

			put(ESTIMATE_DATE, LocalDate.now().toString())
			put(STATUS, EstimateStatus.OPEN.name)
		}

		val newId = db.insert(ESTIMATE_TABLE, null, values)
		db.close()

		if (newId != -1L) {
			EstimateSession.saveSession(context, newId.toInt())
			Log.d("EstimateDebug", "New estimate created with ID = $newId and default title = INFIELDER")
		}
		return newId
	}



	fun getEstimateById(id: Int): Estimateinfo? {
		val db = readableDatabase
		val cursor = db.rawQuery("SELECT * FROM $ESTIMATE_TABLE WHERE $ESTIMATE_ID = ?", arrayOf(id.toString()))
		var estimate: Estimateinfo? = null

		try {
			if (cursor != null && cursor.moveToFirst()) {
				// Verify all expected columns exist
				val colNames = cursor.columnNames.toList()
				android.util.Log.e("DatabaseHandler", "Available columns: $colNames")

				estimate = Estimateinfo(
					estimateId = cursor.getInt(cursor.getColumnIndexOrThrow(ESTIMATE_ID)),
					titleINV = cursor.getString(cursor.getColumnIndexOrThrow(ESTIMATE_TITLE) ?: -1)
						?: "Untitled", // Fallback in case of null
					creationDate = cursor.getString(cursor.getColumnIndexOrThrow(ESTIMATE_DATE) ?: -1)
						?: "N/A",
					dueDate = cursor.getString(cursor.getColumnIndexOrThrow(DUE_DATE) ?: -1)
						?: "N/A",
					customerId = cursor.getInt(cursor.getColumnIndexOrThrow(CUSTOMER_ID) ?: -1),
					status = try {
						EstimateStatus.valueOf(
							cursor.getString(cursor.getColumnIndexOrThrow(STATUS) ?: -1) ?: "OPEN"
						)
					} catch (e: Exception) {
						EstimateStatus.OPEN
					}
				)
			} else {
				android.util.Log.e("DatabaseHandler", "No estimate found for ID: $id")
			}
		} catch (e: Exception) {
			android.util.Log.e("DatabaseHandler", "Error fetching estimate: ${e.message}", e)
		} finally {
			cursor?.close()
			db.close()
		}

		return estimate
	}


	fun updateEstimateStatus(id: Int, status: EstimateStatus) {
		val db = writableDatabase
		val values = ContentValues().apply {
			put(STATUS, status.name)
		}
		db.update(ESTIMATE_TABLE, values, "$ESTIMATE_ID=?", arrayOf(id.toString()))
		db.close()
	}

	fun deleteItem(itemId: Int): Int {
		val db = this.writableDatabase
		val result = db.delete("$INVOICE_TABLE", "id = ?", arrayOf(itemId.toString()))
		db.close()
		return result
	}


	// temporary code debug testers from this point downwards

//	/////////////////////////////////////////////////////////////////////////
	//////////////////////////////////
	fun doesCustomerExist(id: Int): Boolean {
		val db = readableDatabase
		val cursor = db.rawQuery("SELECT 1 FROM $CUSTOMER_TABLE WHERE $CUSTOMER_ID = ?", arrayOf(id.toString()))
		val exists = cursor.moveToFirst()
		cursor.close()
		db.close()
		return exists
	}

fun estimated (context: Context){
		val db = DatabaseHandler(context).readableDatabase
		val c = db.rawQuery(
			"SELECT $KEY_ID, $KEY_NAME, $KEY_QUANTITY, $KEY_PRICE, $KEY_ITEM_TOTAL, $KEY_TAX, $CUSTOMER_ID, $ESTIMATE_ID FROM $INVOICE_TABLE",
			null
		)
		if (c.moveToFirst()) {
			do {
				val id = c.getInt(0)
				val name = c.getString(1)
				val qty = c.getInt(2)
				val price = c.getDouble(3)
				val total = c.getFloat(4)
				val tax = c.getFloat(5)
				val cust = c.getInt(6)
				val est = c.getInt(7)
				android.util.Log.d(
					"DB_DUMP",
					"row id=$id name=$name qty=$qty price=$price total=$total tax=$tax cust=$cust est=$est"
				)
			} while (c.moveToNext())
		} else {
			android.util.Log.d("DB_DUMP", "TableInvoice is empty")
		}
		c.close()
		db.close()
	}

	fun doesEstimateExist(id: Int): Boolean {
		val db = readableDatabase
		val cursor = db.rawQuery("SELECT 1 FROM $ESTIMATE_TABLE WHERE $ESTIMATE_ID = ?", arrayOf(id.toString()))
		val exists = cursor.moveToFirst()
		cursor.close()
		db.close()
		return exists
	}




}