package com.movix.transak_infield

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper

//creating a database logic that extends the SQLiteOpenHelper base class

class DatabaseHandler(context: Context) :
	SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
	companion object {
		private const val DATABASE_VERSION = 6
		private const val DATABASE_NAME = "Transak_infield.db"
		private const val TABLE_NAME = "TableInvoice"
		private const val CUSTOMER_TABLE = "TableCustomer"
		private const val ESTIMATE_TABLE = "Estimates"

		private const val CUSTOMER_ID = "customer_id"
		private const val CUSTOMER_PHONE = "customer_phone"
		private const val CUSTOMER_NAME = "customer_name"
		private const val ESTIMATE_ID = "estimate_id"
		private const val ESTIMATE_DATE = "created_date"
		private const val DUE_DATE = "due_date"


		private const val KEY_ID = "_id"
		private const val KEY_NAME = "items_name"
		private const val KEY_QUANTITY = "item_quantity"
		private const val KEY_PRICE = "item_price"
		private const val KEY_ITEM_TOTAL = "item_total"
		private const val KEY_TAX = "item_tax"
	}


	override fun onCreate(db: SQLiteDatabase?) {
//        creating table with fields /COLUMNS WITH THE NAME TEXT TYPE: INTEGER ,TEXT ,CHAR
//        CREATE TABLE called TableInvoice(param1,param2,param3,.....)

		val CREATE_CUSTOMERS_TABLE =
			("CREATE TABLE " + CUSTOMER_TABLE + " (" + CUSTOMER_ID + " INTEGER PRIMARY KEY, " + CUSTOMER_NAME + " VARCHAR(50), " + CUSTOMER_PHONE + " TEXT" +
					")")


		val CREATE_ESTIMATE_TABLE =
			("CREATE TABLE " + ESTIMATE_TABLE + " (" + ESTIMATE_ID + " INTEGER PRIMARY KEY, " + ESTIMATE_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP, " + DUE_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP, " + CUSTOMER_ID + " INTEGER, " +
					// Must match the primary key column in Customers table
					"FOREIGN KEY(" + CUSTOMER_ID + ") REFERENCES " + CUSTOMER_TABLE + "(" + CUSTOMER_ID + ") ON DELETE CASCADE" + ")")


		val CREATE_PRODUCTS_TABLE =
			("CREATE TABLE " + TABLE_NAME + "(" + KEY_ID + " INTEGER PRIMARY KEY," // not "INTEGERPRIMARY"
					+ KEY_NAME + " TEXT," + KEY_QUANTITY + " INTEGER," + KEY_PRICE + " REAL," + KEY_ITEM_TOTAL + " REAL," + KEY_TAX + " REAL" + ")")


//        tell the database(db?) to go ahead and execute SQL (execSQL)
		db?.execSQL(CREATE_PRODUCTS_TABLE)
		db?.execSQL(CREATE_ESTIMATE_TABLE)
		db?.execSQL(CREATE_CUSTOMERS_TABLE)
	}

	override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
//		you can omit !! since the db is not null at this pointThe system always calls it with a valid db instance, so it’s safe to use db!! once, or just assume it’s non-null and skip !! entirely (which is better).
		db!!.execSQL("DROP TABLE IF EXISTS $ESTIMATE_TABLE") // Drop child first
		db.execSQL("DROP TABLE IF EXISTS $CUSTOMER_TABLE")
		db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")


		onCreate(db)
	}

//	method to add customers info into the databse

	fun addClientsInformations(clientsCreation: ClientsCreation):Long{
		val db=this.writableDatabase
		val contentValues=ContentValues()
		contentValues.put(CUSTOMER_ID,clientsCreation.id)
		contentValues.put(CUSTOMER_NAME,clientsCreation.name)
		contentValues.put(CUSTOMER_PHONE,clientsCreation.phone)
	    val infosSuccess = db.insertOrThrow(CUSTOMER_TABLE,null,contentValues)

	db.close()
	return infosSuccess
	}

	fun updateClientsInfos(clientsCreation: ClientsCreation): Int {
		val db=this.writableDatabase
		val contentValues =ContentValues()
//		 Note: It’s not wrong to include CUSTOMER_ID in put() again, but it’s not necessary when updating by it.
//		contentValues.put(CUSTOMER_ID,clientsCreation.id)
		contentValues.put(CUSTOMER_NAME,clientsCreation.name)
		contentValues.put(CUSTOMER_PHONE,clientsCreation.phone)
		val updateSuccess = db.update(CUSTOMER_TABLE,contentValues, "$CUSTOMER_ID="+clientsCreation.id,null)

		db.close()
		return updateSuccess
	}

	fun addEstimateInfo(estimateinfo: Estimateinfo):Long{
		val db =  this.writableDatabase
		val contentValues= ContentValues()
		contentValues.put(ESTIMATE_ID,estimateinfo.id)
		contentValues.put(ESTIMATE_DATE,estimateinfo.creationDate)
//		another way to pass the varag parameter created *(asterisk) called the spread operator
		contentValues.put(DUE_DATE,estimateinfo.dueDate)
		contentValues.put(CUSTOMER_ID, estimateinfo.customerId) // FIXED: Add this line
		val successEstimate=db.insert(ESTIMATE_TABLE,null,contentValues)
		db.close()
		return successEstimate
	}
//update estimate infos
	fun updateEstimateInfo(estimateinfo: Estimateinfo):Int{
		val db =  this.writableDatabase
		val contentValues= ContentValues()
		contentValues.put(ESTIMATE_ID,estimateinfo.id)
		contentValues.put(ESTIMATE_DATE,estimateinfo.creationDate)
//		another way to pass the varag parameter created *(asterisk) called the spread operator
		contentValues.put(DUE_DATE,estimateinfo.dueDate)
		contentValues.put(CUSTOMER_ID, estimateinfo.customerId)  // Important: Link to customer! -> Insert foreign key
		val successUpdate=db.update(ESTIMATE_TABLE,contentValues,"$ESTIMATE_ID="+estimateinfo.id,null)
		db.close()
		return successUpdate
	}


	//    method to add data to the database
	fun addProductToDatabase(modelClass: ModelClass): Long {
		val db = this.writableDatabase
		val contentValues = ContentValues()
		contentValues.put(KEY_NAME, modelClass.itemName) // put itemName .quantity, price,total
		contentValues.put(KEY_QUANTITY, modelClass.quantity)
		contentValues.put(KEY_PRICE, modelClass.price)
		contentValues.put(KEY_TAX, modelClass.tax)
		contentValues.put(KEY_ITEM_TOTAL, modelClass.total)

//        inserting rows
		val insertSuccess = db.insert(TABLE_NAME, null, contentValues)
//        second param2 is a string containing nullColumnHack

		db.close()  //close the database connection

		return insertSuccess
	}

	//method to read data from the database
	fun viewProduct(): ArrayList<ModelClass> {
		val productList: ArrayList<ModelClass> = ArrayList()

//       the select query gives all the data present in our table
		val selectQuery = "SELECT * FROM $TABLE_NAME"
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


//      Move through the cursor
		if (cursor.moveToFirst()) {
			do {
				id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID))
				itemName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME))
				quantity = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_QUANTITY))
				price = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_PRICE))
				total = cursor.getFloat(cursor.getColumnIndexOrThrow(KEY_ITEM_TOTAL))
				tax = cursor.getFloat(cursor.getColumnIndexOrThrow(KEY_TAX))


				val modelClass1 = ModelClass(
					id = id,
					quantity = quantity,
					itemName = itemName,
					price = price,
					total = total,
					tax = tax
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

//    updating rows

		val successUpdate = db.update(TABLE_NAME, contentValues, KEY_ID + "=" + modelClass.id, null)
// the key id is used to update the specific id of the row selected even if there are other similar product

		db.close()
		return successUpdate
	}

	//    delete the records
	fun deleteRecords(modelClass: ModelClass): Int {
		val db = this.writableDatabase
		val contentValues = ContentValues()
		contentValues.put(KEY_ID, modelClass.id) // model class id

		val successDelete = db.delete(TABLE_NAME, KEY_ID + "=" + modelClass.id, null)
		db.close()

		return successDelete
	}
}