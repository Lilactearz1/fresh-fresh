package com.movix.transak_infield

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper

import java.time.LocalDate

//firebase importations

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


//creating a database logic that extends the SQLiteOpenHelper base class

class DatabaseHandler(context: Context) :
	SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {


	companion object {
		private const val DATABASE_VERSION = 6
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




        //////////////////////////////
//    cloud storage functions companion object function

        private const val  BACKUP_DIR ="TransakBackups"
        private  const val  BACKUP_PREFIX ="transak_backup_"
        private const val  BACKUP_EXTENSION =".zip"
        private const val JSON_BACKUP_EXTENSION=".json"




	}


	override fun onCreate(db: SQLiteDatabase?) {
//        creating table with fields /COLUMNS WITH THE NAME TEXT TYPE: INTEGER ,TEXT ,CHAR
//        CREATE TABLE called TableInvoice(param1,param2,param3,.....)

		val CREATE_CUSTOMERS_TABLE =
			("CREATE TABLE " + CUSTOMER_TABLE + " (" + CUSTOMER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + CUSTOMER_NAME + " VARCHAR(50), " + CUSTOMER_PHONE + " TEXT" +
					")")
        val CREATE_ESTIMATE_TABLE = """
CREATE TABLE $ESTIMATE_TABLE (
    $ESTIMATE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
    $ESTIMATE_TITLE VARCHAR(200),
    $ESTIMATE_DATE DATETIME DEFAULT CURRENT_TIMESTAMP,
    $DUE_DATE DATETIME DEFAULT CURRENT_TIMESTAMP,
    $STATUS TEXT,
    $CUSTOMER_ID INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY($CUSTOMER_ID) REFERENCES $CUSTOMER_TABLE($CUSTOMER_ID) ON DELETE SET DEFAULT
)
""".trimIndent()


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

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 1️⃣ Turn off foreign key constraints temporarily
        db.execSQL("PRAGMA foreign_keys=OFF")

        // 2️⃣ Rename existing tables to temporary tables
        db.execSQL("ALTER TABLE $CUSTOMER_TABLE RENAME TO temp_$CUSTOMER_TABLE")
        db.execSQL("ALTER TABLE $ESTIMATE_TABLE RENAME TO temp_$ESTIMATE_TABLE")
        db.execSQL("ALTER TABLE $INVOICE_TABLE RENAME TO temp_$INVOICE_TABLE")

        // 3️⃣ Recreate tables with the latest schema (your hardened schema)
        onCreate(db)

        // 4️⃣ Copy data from old tables into new tables
        // Customers
        db.execSQL("""
        INSERT OR IGNORE INTO $CUSTOMER_TABLE($CUSTOMER_ID, $CUSTOMER_NAME, $CUSTOMER_PHONE)
        SELECT $CUSTOMER_ID, $CUSTOMER_NAME, $CUSTOMER_PHONE FROM temp_$CUSTOMER_TABLE
    """.trimIndent())

        // Estimates
        db.execSQL("""
        INSERT OR IGNORE INTO $ESTIMATE_TABLE($ESTIMATE_ID, $ESTIMATE_TITLE, $ESTIMATE_DATE, $DUE_DATE, $STATUS, $CUSTOMER_ID)
        SELECT $ESTIMATE_ID, $ESTIMATE_TITLE, $ESTIMATE_DATE, $DUE_DATE, $STATUS,
               CASE 
                   WHEN $CUSTOMER_ID IS NULL OR $CUSTOMER_ID = 0 THEN 1
                   ELSE $CUSTOMER_ID
               END
        FROM temp_$ESTIMATE_TABLE
    """.trimIndent())

        // Invoice Items
        db.execSQL("""
        INSERT OR IGNORE INTO $INVOICE_TABLE($KEY_ID, $KEY_NAME, $KEY_QUANTITY, $KEY_PRICE, $KEY_ITEM_TOTAL, $KEY_TAX, $CUSTOMER_ID, $ESTIMATE_ID)
        SELECT $KEY_ID, $KEY_NAME, $KEY_QUANTITY, $KEY_PRICE, $KEY_ITEM_TOTAL, $KEY_TAX,
               CASE 
                   WHEN $CUSTOMER_ID IS NULL OR $CUSTOMER_ID = 0 THEN 1
                   ELSE $CUSTOMER_ID
               END,
               $ESTIMATE_ID
        FROM temp_$INVOICE_TABLE
    """.trimIndent())

        // 5️⃣ Drop the temporary tables
        db.execSQL("DROP TABLE IF EXISTS temp_$INVOICE_TABLE")
        db.execSQL("DROP TABLE IF EXISTS temp_$ESTIMATE_TABLE")
        db.execSQL("DROP TABLE IF EXISTS temp_$CUSTOMER_TABLE")

        // 6️⃣ Re-enable foreign key constraints
        db.execSQL("PRAGMA foreign_keys=ON")
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
    //

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

		} catch (e: Exception) {

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

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        fixOrphanEstimates(db)
    }

    fun getAllEstimate(): MutableList<Estimateinfo> {
        val list = mutableListOf<Estimateinfo>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $ESTIMATE_TABLE", null)

        if (cursor.moveToFirst()) {
            do {
                val customerCol = cursor.getColumnIndexOrThrow(CUSTOMER_ID)
                val safeCustomerId =
                    if (cursor.isNull(customerCol) || cursor.getInt(customerCol) <= 0)
                        1
                    else
                        cursor.getInt(customerCol)

                val estimate = Estimateinfo(
                    estimateId = cursor.getInt(cursor.getColumnIndexOrThrow(ESTIMATE_ID)),
                    titleINV = cursor.getString(cursor.getColumnIndexOrThrow(ESTIMATE_TITLE)),
                    creationDate = cursor.getString(cursor.getColumnIndexOrThrow(ESTIMATE_DATE)),
                    dueDate = cursor.getString(cursor.getColumnIndexOrThrow(DUE_DATE)),
                    customerId = safeCustomerId,
                    status = EstimateStatus.valueOf(
                        cursor.getString(cursor.getColumnIndexOrThrow(STATUS))
                    )
                )
                list.add(estimate)

                Log.d("DB_CHECK", "Estimate ${estimate.estimateId} customer=${estimate.customerId}")

            } while (cursor.moveToNext())
        }

        cursor.close()
        return list
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
					customerId = cursor.getInt(cursor.getColumnIndexOrThrow(CUSTOMER_ID)),
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

            put(CUSTOMER_ID, if (customerId > 0) customerId else 1)


			put(ESTIMATE_DATE, LocalDate.now().toString())
			put(STATUS, EstimateStatus.OPEN.name)
		}

		val newId = db.insert(ESTIMATE_TABLE, null, values)
		db.close()

		if (newId != -1L) {
			EstimateSession.saveSession(context, newId.toInt())

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

			}
		} catch (e: Exception) {

		} finally {
			cursor?.close()
			db.close()
		}

		return estimate
	}


	fun updateEstimateStatus(id: Int, status: EstimateStatus) {
		val db = writableDatabase
		val values =  ContentValues().apply {
			put(STATUS, status.name)
		}
		db.update(ESTIMATE_TABLE, values, "$ESTIMATE_ID=?", arrayOf(id.toString()))
		db.close()
	}

	fun deleteItem(itemId: Int): Int {
		val db = this.writableDatabase
        val result = db.delete(INVOICE_TABLE, "$KEY_ID = ?", arrayOf(itemId.toString()))
		db.close()
		return result
	}

	fun getCustomerById(customerId: Int): ClientsCreation? {
		val db = readableDatabase
		var customer: ClientsCreation? = null
		val query = "SELECT * FROM $CUSTOMER_TABLE WHERE $CUSTOMER_ID = ?"
		val cursor = db.rawQuery(query, arrayOf(customerId.toString()))

		if (cursor.moveToFirst()) {
			val id = cursor.getInt(cursor.getColumnIndexOrThrow(CUSTOMER_ID))
			val name = cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_NAME))
			val phone = cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_PHONE))

			customer = ClientsCreation(id, name, phone)
		}

		cursor.close()
		db.close()
		return customer
	}
    fun getClientNameById(customerId: Int): String {
        val db = readableDatabase
        var name = "Unknown Client"

        val cursor = db.rawQuery(
            "SELECT $CUSTOMER_NAME FROM $CUSTOMER_TABLE WHERE $CUSTOMER_ID = ?",
            arrayOf(customerId.toString())
        )

        if (cursor.moveToFirst()) {
            name = cursor.getString(
                cursor.getColumnIndexOrThrow(CUSTOMER_NAME)
            )
        }

        cursor.close()
        return name
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

			} while (c.moveToNext())
		} else {

		}
		c.close()
		db.close()
	}

    fun fixOrphanEstimates(db: SQLiteDatabase) {
        db.execSQL("""
        UPDATE $ESTIMATE_TABLE
        SET $CUSTOMER_ID = 1
        WHERE $CUSTOMER_ID IS NULL
           OR $CUSTOMER_ID = 0
           OR $CUSTOMER_ID NOT IN (
               SELECT $CUSTOMER_ID FROM $CUSTOMER_TABLE
           )
    """.trimIndent())
    }



    fun updateEstimateCustomer(estimateId: Int, customerId: Int): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("$CUSTOMER_ID", customerId)
        }
        val rowsUpdated = db.update(
            "$ESTIMATE_TABLE",  // <-- your estimates table name
            values,
            "$ESTIMATE_ID = ?",
            arrayOf(estimateId.toString())
        )
        db.close()
        return rowsUpdated > 0
    }




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



	fun doesEstimateExist(id: Int): Boolean {
		val db = readableDatabase
		val cursor = db.rawQuery("SELECT 1 FROM $ESTIMATE_TABLE WHERE $ESTIMATE_ID = ?", arrayOf(id.toString()))
		val exists = cursor.moveToFirst()
		cursor.close()
		db.close()
		return exists
	}


///////////////////////////////
//    backup functions
fun getDatabaseFile(context: Context): File{
    return context.getDatabasePath(DATABASE_NAME)

}
/*
* get the entire database and export it to a zip file*/
fun createBackupFile(context: Context):File{
//    create a backup directory
    val backupDir = File(context.getExternalFilesDir(null),BACKUP_DIR)
    if(!backupDir.exists()){
        backupDir.mkdirs()
    }
    /// create timestamp forname
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val zipFileName  = "${BACKUP_PREFIX}${timeStamp}${BACKUP_EXTENSION}"
    val zipFile = File(backupDir,zipFileName)
    /////create a zip file
    FileOutputStream(zipFile).use { fos->
        ZipOutputStream(fos).use { zos->
            // add sqlire database file
            val dbFile = getDatabaseFile(context)
            if (dbFile.exists()){
                val entry = ZipEntry("database/$DATABASE_NAME")
                zos.putNextEntry(entry)
                FileInputStream(dbFile).use { fis->
                    fis.copyTo(zos)

                }
                zos.closeEntry()

            }
            /// add json export data
            val jsonData= exportAllDataToJson()
            val jsonEntry= ZipEntry("data/backup_data.json")
            zos.putNextEntry(jsonEntry)
            zos.write(jsonData.toByteArray())
            zos.closeEntry()

            ////add metadata
            val metaData= JSONObject().apply {
                put("app_name","Transak Infield")
                put("backup_timestamp", "timestamp")
                put("database_version", DATABASE_VERSION)
                put("total_customers", getCustomerCount())
                put("total_estimates", getEstimateCount())
                put("total_invoice_items", getInvoiceItemCount())
            }
            val metaEntry = ZipEntry("metadata.json")
            zos.putNextEntry(metaEntry)
            zos.write(metaData.toString().toByteArray())
            zos.closeEntry()
        }
    }
    return zipFile
}

    /**
     * Export all data to JSON format
     */
    fun exportAllDataToJson(): String {
        val jsonObject = JSONObject()

        // 1. Export customers
        val customers = viewClientsInfo()
        val customersArray = JSONArray()
        for (customer in customers) {
            val customerJson = JSONObject().apply {
                put("customer_id", customer.id)
                put("name", customer.name)
                put("phone", customer.phone)
            }
            customersArray.put(customerJson)
        }
        jsonObject.put("customers", customersArray)

        // 2. Export estimates
        val estimates = getAllEstimate()
        val estimatesArray = JSONArray()
        for (estimate in estimates) {
            val estimateJson = JSONObject().apply {
                put("estimate_id", estimate.estimateId)
                put("title", estimate.titleINV)
                put("created_date", estimate.creationDate)
                put("due_date", estimate.dueDate)
                put("customer_id", estimate.customerId)
                put("status", estimate.status?.name ?: "OPEN")
            }
            estimatesArray.put(estimateJson)
        }
        jsonObject.put("estimates", estimatesArray)

        // 3. Export invoice items
        val invoiceItems = viewProduct()
        val itemsArray = JSONArray()
        for (item in invoiceItems) {
            val itemJson = JSONObject().apply {
                put("id", item.id)
                put("item_name", item.itemName)
                put("quantity", item.quantity)
                put("price", item.price)
                put("total", item.total)
                put("tax", item.tax)
                put("customer_id", item.customerId)
                put("estimate_id", item.estimateId)
            }
            itemsArray.put(itemJson)
        }
        jsonObject.put("invoice_items", itemsArray)

        // 4. Add summary
        val summary = JSONObject().apply {
            put("export_date", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
            put("total_records", customers.size + estimates.size + invoiceItems.size)
        }
        jsonObject.put("summary", summary)

        return jsonObject.toString(2) // Pretty print with indentation
    }

    /**
     * Get counts for metadata
     */
    private fun getCustomerCount(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $CUSTOMER_TABLE", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        db.close()
        return count
    }

    private fun getEstimateCount(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $ESTIMATE_TABLE", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        db.close()
        return count
    }

    private fun getInvoiceItemCount(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $INVOICE_TABLE", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        db.close()
        return count
    }

    /**
     * List all available backup files
     */
    fun listBackups(context: Context): List<File> {
        val backupDir = File(context.getExternalFilesDir(null), BACKUP_DIR)
        if (!backupDir.exists()) return emptyList()

        return backupDir.listFiles { file ->
            file.name.startsWith(BACKUP_PREFIX) && file.name.endsWith(BACKUP_EXTENSION)
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    /**
     * Delete old backups (keep only last N)
     */
    fun cleanupOldBackups(keepLast: Int = 5,context: Context) {
        val backups = listBackups(context)
        if (backups.size > keepLast) {
            backups.drop(keepLast).forEach { it.delete() }
        }
    }
}