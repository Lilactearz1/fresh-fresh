package com.movix.transak_infield

import android.content.Context

class GlobalFunck{
//    method to sum list in items
    fun getSubTotal(context: Context):Float{
        var totalSum=0F
        val db = DatabaseHandler(context).readableDatabase
        val cursor = db.rawQuery("SELECT item_total FROM TableInvoice",null)
    if (cursor.moveToFirst()){
        do {
            val total =cursor.getFloat(cursor.getColumnIndexOrThrow("item_total"))
            totalSum+=total
        }while(cursor.moveToNext())

    }
  cursor.close()
    db.close()
    return totalSum
    }

	//        an extension function to the global functions
	fun summationOfTax(context: Context): Double {
		var cumulativeTax=0.0
		val dbView = DatabaseHandler(context)
		var viewitem = dbView.viewProduct()

		for (items in viewitem){
			val price = items.price
			val quantitty= items.quantity
			val taxedIndex =items.tax
			val nontax  = price*quantitty
			// Multiply amount by taxed index
			val taxeditemprice = (nontax * taxedIndex * 0.01)
			//add vat data dynamically as they are entered in the table
			cumulativeTax +=  taxeditemprice
			println( "test for cummulative total tax is $cumulativeTax")

		}
		return cumulativeTax
	}


	fun summationofTotal(context: Context):Float{
//get the summation of total plus the taxed =(taxed or non-taxed)
		val sumTax = summationOfTax(context).toFloat()
		var summedTotal =0.00f
		var subtotalAmount = getSubTotal(context)
		println("this is subtotal $subtotalAmount")
		subtotalAmount  += sumTax
//            add the collected tax total to the initial total
		summedTotal+=subtotalAmount

		// total plus the taxed amount is returned for use
		return summedTotal
	}


}