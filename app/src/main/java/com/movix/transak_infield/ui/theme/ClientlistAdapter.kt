package com.movix.transak_infield.ui.theme

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.movix.transak_infield.ClientActivity
import com.movix.transak_infield.ClientsCreation
import com.movix.transak_infield.DatabaseHandler
import com.movix.transak_infield.GlobalFunck
import com.movix.transak_infield.MainActivity
import com.movix.transak_infield.R
import kotlinx.serialization.descriptors.PrimitiveKind

class ClientlistAdapter(
	private val context: Context,
	private val clientList: ArrayList<ClientsCreation>,
	private val onSelectedEstimate: ClientActivity

) : RecyclerView.Adapter<ClientlistAdapter.ClientsViewHolder>() {


	class ClientsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val clientName = itemView.findViewById<TextView>(R.id.clientname)
		val entryDate = itemView.findViewById<TextView>(R.id.entryDate)
		val editPen = itemView.findViewById<ImageView>(R.id.editpen)
		val selectRelativeLayout = itemView.findViewById<RelativeLayout>(R.id.relClientName)
	}

	interface OnClientClickListener {

		fun onClientClick(client: ClientsCreation)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientsViewHolder {
		val view = LayoutInflater.from(context).inflate(R.layout.list_clients, parent, false)
		return ClientsViewHolder(view)
	}


	override fun onBindViewHolder(holder: ClientsViewHolder, position: Int) {
		val client = clientList[position]

		holder.clientName.text = client.name
		holder.entryDate.text = GlobalFunck().creationDate(context)

		holder.editPen.setOnClickListener {
		 //pop-up dialog for inputting clients infos
			updateClientinfo(client)
//
		}
	}

	private fun updateClientinfo(client: ClientsCreation) {
		val dialog= AlertDialog.Builder(context).setTitle("update clients info")
			.setView(R.layout.fragment_client_info)
			.setCancelable(false)
			.create()
//		now show the dialog
		dialog.show()
//		find views from dialog layout
		val clientName=dialog.findViewById<EditText>(R.id.edit_clientName)
		val clientPhoneNo =dialog.findViewById<EditText>(R.id.edit_phone)
		val clientEmail = dialog.findViewById<EditText>(R.id.edit_email)
		val backBtn = dialog.findViewById<RelativeLayout>(R.id.backLayout)
		val saveBtn= dialog.findViewById<RelativeLayout>(R.id.SaveclientDetails)

//		fill the views from dialog
		val globalFunck=GlobalFunck()
		clientName?.setText(client.name)
		clientPhoneNo?.setText(client.phone)
//		clientEmail?.setText(client.email)
//		set the cancel button and save button

		backBtn?.setOnClickListener{
			dialog.dismiss()
		}

//		save update actions
		saveBtn?.setOnClickListener {
			val updateName=clientName?.text.toString()
			val updatePhoneNo=clientPhoneNo?.text.toString()
			val updateEmail=clientEmail?.text.toString()

			if (updateName.isEmpty() || updatePhoneNo.isEmpty()){
				Toast.makeText(context,"Field must be Valid",Toast.LENGTH_LONG).show()
			return@setOnClickListener
			}
			val updateClient=ClientsCreation(id = client.id,
				name = updateName,
				phone = updatePhoneNo)

//			 update method in the database
			val db=DatabaseHandler(context)

			val status =db.updateClientsInfos(updateClient)
			if (status>- 1){
				Toast.makeText(context, "success", Toast.LENGTH_SHORT).show()
				dialog.dismiss()
				//refresh list into the views or db
			}else{
				Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
			}

		}
		dialog.show()

	}


	override fun getItemCount(): Int = clientList.size


}

