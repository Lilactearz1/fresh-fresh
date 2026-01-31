package com.movix.transak_infield.docAnalysis.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.movix.transak_infield.R
import com.movix.transak_infield.docAnalysis.AppConstants
import com.movix.transak_infield.docAnalysis.data.repository.InvoiceRepository
import com.movix.transak_infield.docAnalysis.domain.model.ExtractedItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExtractionAdapter(
    private val items: List<ExtractedItem>,
    private val repository: InvoiceRepository
) : RecyclerView.Adapter<ExtractionAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val desc: EditText = view.findViewById(R.id.txtDescription)
        val qty: EditText = view.findViewById(R.id.txtQty)
        val price: EditText = view.findViewById(R.id.txtPrice)
        val btnSave: Button = view.findViewById(R.id.saveAi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_row, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {

        val item = items[position]

        holder.desc.setText(item.description)
        holder.qty.setText(item.quantity?.toString() ?: "")
        holder.price.setText(item.price?.toString() ?: "")

        holder.btnSave.setOnClickListener {

            item.description = holder.desc.text.toString()
            item.quantity = holder.qty.text.toString().toDoubleOrNull()
            item.price = holder.price.text.toString().toDoubleOrNull()

            CoroutineScope(Dispatchers.IO).launch {
                repository.updateItem(AppConstants.AUTH_TOKEN, item)
            }
        }
    }

    override fun getItemCount() = items.size
}