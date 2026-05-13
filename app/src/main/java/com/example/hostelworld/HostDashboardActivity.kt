package com.example.hostelworld

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class HostDashboardActivity : AppCompatActivity() {

    private lateinit var rvProperties: RecyclerView
    private lateinit var adapter: DashPropertyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host_dashboard)

        // 1. Hook up the "Add New" text button
        val tvAddNew = findViewById<TextView>(R.id.tvAddNew)
        tvAddNew.setOnClickListener {
            startActivity(Intent(this, AddPropertyActivity::class.java))
        }

        // 2. Setup the Horizontal RecyclerView
        rvProperties = findViewById(R.id.rvDashboardProperties)
        rvProperties.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapter = DashPropertyAdapter(HostManager.myListings)
        rvProperties.adapter = adapter

        // 3. Bottom Navigation Setup
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavHost)
        bottomNavigationView.selectedItemId = R.id.nav_host_dashboard

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_host_dashboard -> {
                    // You are already on the Dashboard, do nothing!
                }
                R.id.nav_host_listings -> {
                    // Open the Manage Listings screen!
                    startActivity(Intent(this, HostListingsActivity::class.java))
                    finish()
                }
                R.id.nav_host_reports -> {
                    // startActivity(Intent(this, HostReportsActivity::class.java))
                    // finish()
                }
                R.id.nav_host_profile -> {
                    val intent = Intent(this, HostProfileActivity::class.java) // <--- CHANGED THIS!
                    intent.putExtra("USER_NAME", intent.getStringExtra("USER_NAME"))
                    intent.putExtra("USER_EMAIL", intent.getStringExtra("USER_EMAIL"))
                    startActivity(intent)
                    finish()
                }
            }
            true
        }
    }

    // 4. This runs EVERY TIME the screen becomes visible again!
    override fun onResume() {
        super.onResume()

        // Refresh the horizontal list
        adapter.notifyDataSetChanged()

        // Update the Active Listings Number dynamically!
        val tvActiveListings = findViewById<TextView>(R.id.tvActiveListings)
        tvActiveListings.text = HostManager.myListings.size.toString()
    }

    // 5. The Adapter for the Horizontal Dashboard Cards
    inner class DashPropertyAdapter(private val listings: List<HostManager.HostProperty>) : RecyclerView.Adapter<DashPropertyAdapter.DashViewHolder>() {
        inner class DashViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivImage: ImageView = view.findViewById(R.id.ivDashPropImage)
            val tvName: TextView = view.findViewById(R.id.tvDashPropName)
            val tvPrice: TextView = view.findViewById(R.id.tvDashPropPrice)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dashboard_property, parent, false)
            return DashViewHolder(view)
        }

        override fun onBindViewHolder(holder: DashViewHolder, position: Int) {
            val listing = listings[position]
            holder.tvName.text = listing.name
            holder.tvPrice.text = "$${listing.price} / night"
            holder.ivImage.setImageResource(listing.imageResId) // Assigns the random image!
        }

        override fun getItemCount() = listings.size
    }
}