package com.movix.transak_infield

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException

class ItemAdapter(
	private val context: Context, val itemList: ArrayList<ModelClass>

) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {


	// ViewHolder class that holds the view referencess
	class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val textId: TextView = itemView.findViewById(R.id.tvitem_id)
		val textItemName: TextView = itemView.findViewById(R.id.tvitem_name)
		val textQuantity: TextView = itemView.findViewById(R.id.tvitem_quantity)
		val textPrice: TextView = itemView.findViewById(R.id.tvitem_price)
		val textTotal: TextView = itemView.findViewById(R.id.tvitem_total)
		val textTax: TextView = itemView.findViewById(R.id.tvitem_tax)
		val btnEdit: ImageButton = itemView.findViewById(R.id.itemlist_edititemselected)
		val btnDelete: ImageButton = itemView.findViewById(R.id.itemlist_deleteitemselected)
		val itemlistLayout: LinearLayout = itemView.findViewById(R.id.itemlistlayoutMain)
		val cardviewListItem: CardView = itemView.findViewById(R.id.item_name_cardvwiew)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false)
		return ViewHolder(view)
	}

	@RequiresApi(Build.VERSION_CODES.O)
	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val item = itemList[position]


		holder.textId.text = " ${item.id}"
		holder.textItemName.text = "${item.itemName}"
		holder.textQuantity.text = "${item.quantity}"
		holder.textPrice.text = "Ksh${item.price}"
		holder.textTotal.text = "Ksh${item.total}"
		holder.textTax.text = "${item.tax}"



		holder.btnEdit.setOnClickListener { View ->
			try {
				if (context is MainActivity) {

					context.updateDialog(item)
				}

				if (context is CustomerItems) {

					context.updateDialog(item)
				}

			} catch (e: IOException) {
				e.printStackTrace()
			}

		}

		holder.btnDelete.setOnClickListener { View ->
			try {
				if (context is MainActivity) {
					context.deleteItems(item)
				}

				if (context is CustomerItems) {

					context.deleteItems(item)
				}
			}catch (e:IOException){
				e.printStackTrace()
			}

		}


		val cardbackgrnd = holder.cardviewListItem
		if (position % 2 == 0) {

			val colourBackgnd = cardbackgrnd.setBackgroundColor(
				context.getColor(R.color.Alice_blue)

			)
			colourBackgnd
		} else {
			context.getColor(R.color.transparent)
		}


	}

	// Add this function inside ItemAdapter
	fun setData(newItems: List<ModelClass>) {
		itemList.clear()
		itemList.addAll(newItems)
		notifyDataSetChanged()
	}

	override fun getItemCount(): Int = itemList.size

}