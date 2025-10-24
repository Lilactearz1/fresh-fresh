package com.movix.transak_infield

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class EstimateAdapter(
	private val context: Context,
	private val estimateList: MutableList<Estimateinfo>,
	private val clientInfo:MutableList<ClientsCreation>,
	private val onSelectedEstimate: OnEstimateClickListener,
	internal val onItemDeleted: (deletedItem: Estimateinfo, pos: Int) -> Unit  // callback to persist deletion
) : RecyclerView.Adapter<EstimateAdapter.EstimateViewHolder>() {

	interface OnEstimateClickListener {
		fun onEstimateClick(estimate: Estimateinfo)
	}

	class EstimateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val customerName: TextView = itemView.findViewById(R.id.tvCustomerName)
		val estimateTitle: TextView = itemView.findViewById(R.id.tvEstimateTitle)
		val estimateDate: TextView = itemView.findViewById(R.id.tvEstimateDate)
		val estimateDueDate: TextView = itemView.findViewById(R.id.tvDuedate)
		val archives: CardView = itemView.findViewById(R.id.archivecad1)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EstimateViewHolder {
		val view = LayoutInflater.from(context).inflate(R.layout.content_scrolling, parent, false)
		return EstimateViewHolder(view)
	}

	override fun onBindViewHolder(holder: EstimateViewHolder, position: Int) {
		val estimate = estimateList[position]
		val client = if (position < clientInfo.size) clientInfo[position] else null
		val safeClient =GlobalFunck().safeClientName(context)

		holder.customerName.text = client?.name ?: safeClient
		holder.estimateTitle.text = estimate.titleINV ?: "INFIELDER ${estimate.creationDate}"
		holder.estimateDate.text = "Created: ${estimate.creationDate}"
		holder.estimateDueDate.text = "Due: ${estimate.dueDate}"

		holder.archives.setOnClickListener {
			onSelectedEstimate.onEstimateClick(estimate)
		}
	}


	override fun getItemCount(): Int = estimateList.size

	// remove from adapter list and notify
	fun removeAt(position: Int): Estimateinfo {
		val removed = estimateList.removeAt(position)
		notifyItemRemoved(position)
		return removed
	}

//	insert for (undo)
fun restoreAt(position: Int,item:Estimateinfo) {
	estimateList.add(position,item)
	notifyItemInserted(position)
	
}
}
