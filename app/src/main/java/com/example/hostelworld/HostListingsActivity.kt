package com.example.hostelworld

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HostListingsActivity : AppCompatActivity() {

    private lateinit var rvListings: RecyclerView
    private lateinit var adapter: HostListingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host_listings)

        // Setup RecyclerView to show the database
        rvListings = findViewById(R.id.rvHostListings)
        rvListings.layoutManager = LinearLayoutManager(this)
        adapter = HostListingAdapter(HostManager.myListings)
        rvListings.adapter = adapter

        // Setup FAB to open Add Property form
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddProperty)
        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddPropertyActivity::class.java))
        }

        // Setup Host Bottom Navigation
        val bottomNavHost = findViewById<BottomNavigationView>(R.id.bottomNavHost)
        bottomNavHost.selectedItemId = R.id.nav_host_listings

        bottomNavHost.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_host_dashboard -> {
                    startActivity(Intent(this, HostDashboardActivity::class.java))
                    finish()
                }
                R.id.nav_host_listings -> {
                    // Already here, do nothing
                }
                R.id.nav_host_reports -> {
                    // startActivity(Intent(this, HostReportsActivity::class.java))
                    // finish()
                }
                R.id.nav_host_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("USER_ROLE", "HOST")
                    startActivity(intent)
                    finish()
                }
            }
            true
        }
    } // End onCreate

    // This forces the list to refresh the moment you return from the Add screen!
    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }

    // Inner Adapter to map the Database to the XML Card
    inner class HostListingAdapter(private val listings: List<HostManager.HostProperty>) : RecyclerView.Adapter<HostListingAdapter.ListingViewHolder>() {
        inner class ListingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvListingName)
            val tvLocation: TextView = view.findViewById(R.id.tvListingLocation)
            val tvPrice: TextView = view.findViewById(R.id.tvListingPrice)
            val tvBeds: TextView = view.findViewById(R.id.tvListingBeds)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListingViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_host_listing, parent, false)
            return ListingViewHolder(view)
        }

        override fun onBindViewHolder(holder: ListingViewHolder, position: Int) {
            val listing = listings[position]
            holder.tvName.text = listing.name
            holder.tvLocation.text = listing.location
            holder.tvPrice.text = "$${listing.price} / night"
            holder.tvBeds.text = "${listing.beds} Beds Available"
        }

        override fun getItemCount() = listings.size
    }
}