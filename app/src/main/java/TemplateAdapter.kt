import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.ui.layout.Layout
import androidx.recyclerview.widget.RecyclerView
import com.movix.transak_infield.PdfTemplateDRW
import com.movix.transak_infield.R
import com.movix.transak_infield.TemplateItem

class TemplateAdapter(	private var items:List<TemplateItem>,
                          private val onSelected:(PdfTemplateDRW)-> Unit):RecyclerView.Adapter<TemplateAdapter.TemplateViewHolder>(){

	inner class TemplateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val preview = itemView.findViewById<ImageView>(R.id.templatePreview)
		val name = itemView.findViewById<TextView>(R.id.templateName)
		val selected = itemView.findViewById<View>(R.id.selectedIndicator)}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateViewHolder {
	val view = LayoutInflater.from(parent.context).inflate(R.layout.pdfs_template, parent,false)
		return TemplateViewHolder(view)

	}

	override fun onBindViewHolder(holder: TemplateViewHolder, position: Int) {
	val item =items[position]
		holder.preview.setImageResource(item.templateItem.previewRes)
		holder.name.text = item.templateItem.name
		holder.selected.visibility = if (item.selected) View.VISIBLE else View.INVISIBLE

		holder.itemView.setOnClickListener {
			onSelected(item.templateItem)

			// update UI
			items = items.map { it.copy(selected = it.templateItem == item.templateItem) }
			notifyDataSetChanged()
		}
	}


	override fun getItemCount() = items.size

	fun updateSelection(template: PdfTemplateDRW) {
		items = items.map { item ->
			item.copy(selected = item.templateItem == template)
		}
		notifyDataSetChanged()
	}

}
