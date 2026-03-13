package com.movix.transak_infield

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.movix.transak_infield.cloudB.BackupManagerActivity
import com.movix.transak_infield.databinding.ActivityArchivesBinding
import com.movix.transak_infield.invoicing.MainInvoicing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainLandingPage : AppCompatActivity(), EstimateAdapter.OnEstimateClickListener {

    private lateinit var allEstimates: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var spinner: Spinner
    private lateinit var nav_InvoiceACtivity: View
    private lateinit var floatingPlus: FloatingActionButton
    private lateinit var binding: ActivityArchivesBinding
    private lateinit var db: DatabaseHandler
    private lateinit var adapter: EstimateAdapter
    private lateinit var clientRepo: ClientRepository

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private var title = "INFIELDER"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArchivesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        setupNavigationDrawer()

        allEstimates = binding.allestmimate
        spinner = findViewById(R.id.spinnerMark)
        floatingPlus = binding.floatingplus
        nav_InvoiceACtivity = findViewById(R.id.nav_Invoice)

        db = DatabaseHandler(applicationContext)

        setupInvoiceButton()
        setupFloatingPlusButton()

        // Initialize RecyclerView
        recyclerView = binding.estimateListRecycler
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize clientRepo with empty list temporarily
        clientRepo = ClientRepository(ArrayList<ClientsCreation>())

        // Initialize adapter once
        adapter = EstimateAdapter(
            this,
            mutableListOf(),
            clientRepo,
            this
        ) { deletedItem, pos -> }
        recyclerView.adapter = adapter

        // Attach swipe for delete
        attachSwipeToRecyclerView()

        // Load current session
        EstimateSession.loadSession(this)
        checkActiveEstimate()

        // Load data from DB into RecyclerView
        loadEstimatesFromDb()
    }

    private fun setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navDrawer_transform -> Toast.makeText(applicationContext, "Use Ai tools to produce quote", Toast.LENGTH_SHORT).show()
                R.id.navDrawer_database_add -> startActivity(Intent(this, BackupManagerActivity::class.java))
                R.id.navDrawer_delivery -> Toast.makeText(applicationContext, "Produce delivery note", Toast.LENGTH_SHORT).show()
                R.id.navDrawer_add_items -> Toast.makeText(applicationContext, "Add items to back end", Toast.LENGTH_SHORT).show()
                R.id.navDrawer_about, R.id.navDrawer_settings -> Toast.makeText(applicationContext, "coming soon", Toast.LENGTH_SHORT).show()
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun setupInvoiceButton() {
        // for battom appbar navigations
        nav_InvoiceACtivity.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val existingId = EstimateSession.currentEstimate
                val estimate = existingId?.let { db.getEstimateById(it) }
                val estimateId = if (estimate?.status == EstimateStatus.OPEN) {
                    estimate.estimateId
                } else {
                    db.createNewEstimate(this@MainLandingPage, 1, "INVOICE").toInt()
                }

                if (estimateId > 0) {
                    EstimateSession.currentEstimate = estimateId
                    withContext(Dispatchers.Main) {
                        startActivity(
                            Intent(this@MainLandingPage, MainInvoicing::class.java).apply {
                                putExtra(MainActivity.EXTRA_ESTIMATE_ID, estimateId)
                                putExtra(MainActivity.EXTRA_CUSTOMER_ID, estimate?.customerId ?: 1)
                            })
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainLandingPage, "Failed to create invoice", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupFloatingPlusButton() {
        floatingPlus.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                EstimateSession.clearSession(this@MainLandingPage)
                Log.d("ANR_TEST", "before query create new")
                val newEstimateId = db.createNewEstimate(this@MainLandingPage, 1, title)
                Log.d("ANR_TEST", "after query create new")
                withContext(Dispatchers.Main) {
                    if (newEstimateId != -1L) {
                        startActivity(
                            Intent(this@MainLandingPage, MainActivity::class.java).apply {
                                putExtra(MainActivity.EXTRA_ESTIMATE_ID, newEstimateId.toInt())
                                putExtra(MainActivity.EXTRA_CUSTOMER_ID, 1)
                            })
                    }
                }
            }
        }
    }

    private fun attachSwipeToRecyclerView() {
        val simpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val removedItem = adapter.removeAt(position)
                adapter.onItemDeleted?.invoke(removedItem, position)
                Snackbar.make(recyclerView, "${removedItem.titleINV} removed", Snackbar.LENGTH_LONG)
                    .setAction("UNDO") { adapter.restoreAt(position, removedItem) }.show()
            }
        }
        ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView)
    }

    private fun loadEstimatesFromDb() {
        lifecycleScope.launch(Dispatchers.IO) {
            Log.d("ANR_TEST", "before query getAllEstimate")
            val items = db.getAllEstimate()
            val clients = db.viewClientsInfo()
            Log.d("ANR_TEST", "After query getAllEstimate")

            clientRepo = ClientRepository(clients)

            withContext(Dispatchers.Main) {
                adapter.updateList(items, clientRepo)
            }
        }
    }

    private fun checkActiveEstimate() {
        lifecycleScope.launch(Dispatchers.IO) {
            val currentId = EstimateSession.currentEstimate ?: 0
            if (currentId != 0) {
                val unfinished = db.getEstimateById(currentId)
                withContext(Dispatchers.Main) {
                    if (unfinished?.status == EstimateStatus.OPEN) {
                        val intent = Intent(this@MainLandingPage, MainActivity::class.java).apply {
                            putExtra(MainActivity.EXTRA_ESTIMATE_ID, currentId)
                            putExtra(MainActivity.EXTRA_CUSTOMER_ID, unfinished.customerId)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        EstimateSession.clearSession(this@MainLandingPage)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        db.close()
    }
    override fun onResume() {
        super.onResume()
        loadEstimatesFromDb()
    }

    override fun onEstimateClick(estimate: Estimateinfo) {
        val intent = Intent(this, CustomerItems::class.java)
        intent.putExtra(MainActivity.EXTRA_ESTIMATE_ID, estimate.estimateId)
        intent.putExtra(MainActivity.EXTRA_CUSTOMER_ID, estimate.customerId)
        startActivity(intent)
    }
}