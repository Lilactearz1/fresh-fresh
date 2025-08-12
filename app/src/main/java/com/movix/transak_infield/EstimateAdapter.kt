package com.movix.transak_infield

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
class EstimateAdapter(
	private val context: Context,
	private val estimateList: List<Estimateinfo>,
	private val onEstimateSelected: (Estimateinfo) -> Unit // callback
) : RecyclerView.Adapter<EstimateAdapter.EstimateViewHolder>() {

	class EstimateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val customerName: TextView = itemView.findViewById(R.id.tvCustomerName)
		val estimateTitle: TextView = itemView.findViewById(R.id.tvEstimateTitle)
		val estimateDate: TextView = itemView.findViewById(R.id.tvEstimateDate)
		val archives:CardView=itemView.findViewById(R.id.archivecad1)
	//	val estimateDueDate: TextView = itemView.findViewById(R.id.tvEstimateDueDate)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EstimateViewHolder {
		val view = LayoutInflater.from(context).inflate(R.layout.content_scrolling, parent, false)
		return EstimateViewHolder(view)
	}

	override fun onBindViewHolder(holder: EstimateViewHolder, position: Int) {
		val estimate = estimateList[position]
//		holder.customerName.text = estimate.customerName
		holder.estimateTitle.text = estimate.titleINV
		holder.estimateDate.text = "Created: ${estimate.creationDate}"
//		holder.estimateDueDate.text = "Due: ${estimate.dueDate}"

		holder.archives.setOnClickListener {
			onEstimateSelected(estimate)
		}
	}

	override fun getItemCount(): Int = estimateList.size
}
