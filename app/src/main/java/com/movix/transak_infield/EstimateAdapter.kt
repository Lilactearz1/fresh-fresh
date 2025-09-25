package com.movix.transak_infield

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.SurfaceControl.Transaction
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.movix.transak_infield.MainLandingPage
import androidx.recyclerview.widget.RecyclerView

class EstimateAdapter(
	private val context: Context,
	private val estimateList: List<Estimateinfo>,
 	private val onSelectedEstimate: OnEstimateClickListener

) : RecyclerView.Adapter<EstimateAdapter.EstimateViewHolder>() {

	interface OnEstimateClickListener {
		fun onEstimateClick(estimate: Estimateinfo)

	}

	class EstimateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		//val btnDelete: ImageView=itemView.findViewById(R.id.itemlist_deleteitemselected)
		//val btnEdit: ImageView=itemView.findViewById(R.id.itemlist_edititemselected)
		val customerName: TextView = itemView.findViewById(R.id.tvCustomerName)
		val estimateTitle: TextView = itemView.findViewById(R.id.tvEstimateTitle)
		val estimateDate: TextView = itemView.findViewById(R.id.tvEstimateDate)
		val archives: CardView = itemView.findViewById(R.id.archivecad1)
		val estimateDueDate: TextView = itemView.findViewById(R.id.tvDuedate)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EstimateViewHolder {
		val view = LayoutInflater.from(context).inflate(R.layout.content_scrolling, parent, false)
		return EstimateViewHolder(view)
	}

	override fun onBindViewHolder(holder: EstimateViewHolder, position: Int) {
		val estimate = estimateList[position]
		holder.customerName.text = GlobalFunck().customerName(context)
		holder.estimateTitle.text = estimate.titleINV
		holder.estimateDate.text = "Created: ${estimate.creationDate}"
		holder.estimateDueDate.text = "Due: ${estimate.dueDate}"

		holder.archives.setOnClickListener {

			val intent = Intent(context, CustomerItems::class.java)
			intent.putExtra("customer_id", estimate.customerId) // pass the customer id
			context.startActivity(intent)
		}

//		holder.btnEdit.setOnClickListener { View ->
//			if (context is CustomerItems) {
//
////				context.updateDialog(estimate)
//			}
//		}

//		holder.btnDelete.setOnClickListener { View ->
////			if (context is CustomerItems) {
//////				context.deleteItems(estimate)
////			}
////		}
	}

	override fun getItemCount(): Int = estimateList.size
}
