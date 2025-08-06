package com.movix.transak_infield
//the data model class
class ModelClass (val id:Int,val quantity:Int,val itemName:String,val price:Double,val total: Float,val tax:Float){

}
data class EstimateModel(
	val estimateId: Int,
	val customerName: String,
	val estimateTitle: String,
	val estimateDate: String,
	val itemList: ArrayList<Estimateinfo> // itemized details
)
