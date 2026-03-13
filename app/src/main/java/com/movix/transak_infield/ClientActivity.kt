package com.movix.transak_infield

// TODO: THIS IS WHERE CLIENTS ARE LISTED AND THE ADD BUTTON
//  IS LOCATED FOR ADDING CLINTS AND SELECTING A CLIENT
//  FROM THE LIST CREATED

import android.os.Bundle
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.movix.transak_infield.MainActivity.Companion.EXTRA_CUSTOMER_ID
import com.movix.transak_infield.MainActivity.Companion.EXTRA_ESTIMATE_ID
import com.movix.transak_infield.MainActivity.Companion.requireCustomerId
import com.movix.transak_infield.MainActivity.Companion.requireEstimateId
import com.movix.transak_infield.databinding.ActivityClientlistBinding
import com.movix.transak_infield.ui.theme.ClientlistAdapter


class ClientActivity : AppCompatActivity() {
	private lateinit var binding: ActivityClientlistBinding
	private lateinit var recyclerView: RecyclerView
	private lateinit var db:DatabaseHandler
	private lateinit var  adapterClientList : ClientlistAdapter
	private lateinit var btnAddClient:ImageView
	private lateinit var relavBtn:RelativeLayout

    private var estimateId: Int = -1
    private var customerId: Int = -1


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityClientlistBinding.inflate(layoutInflater)
		setContentView(binding.root)


        estimateId = intent.requireEstimateId()
        customerId = intent.requireCustomerId()

		btnAddClient=binding.buttonaddClient
		relavBtn=binding.addClientPerformClick


		btnAddClient.setOnClickListener{
			val clientinfoFragment = clientinfoFragment().apply {
                arguments= Bundle().apply {
                    putInt(EXTRA_ESTIMATE_ID,estimateId)
                    putInt(EXTRA_CUSTOMER_ID,customerId)
                }
            }
			supportFragmentManager.beginTransaction()
//			replace from the id of the parent view(current view) to the next view
				.replace( R.id.viewClientlist,clientinfoFragment ).addToBackStack(null).commit()

		}


		recyclerViewFun()


	}

	override fun recreate() {
		super.recreate()
		recyclerViewFun()
	}
	private fun recyclerViewFun() {

		relavBtn.setOnClickListener{btnAddClient.performClick()}

		recyclerView = binding.relList
		recyclerView.layoutManager = LinearLayoutManager(this)
		db = DatabaseHandler(this)


		val clientlist = db.viewClientsInfo()
		adapterClientList = ClientlistAdapter(this, clientlist,this)
		recyclerView.adapter=adapterClientList
	}

    fun refreshClients() {
       recyclerViewFun()
    }

}