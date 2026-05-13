package com.example.hostelworld

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView

class TravelerDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_traveler_dashboard)

        // Retrieve traveler data passed from LoginActivity
        val userEmail = intent.getStringExtra("USER_EMAIL")
        val userName = intent.getStringExtra("USER_NAME")

        // 1. Setup Main Search Bar
        val cvSearch = findViewById<CardView>(R.id.cvSearch)
        cvSearch.setOnClickListener {
            val intent = Intent(this@TravelerDashboardActivity, SearchResultsActivity::class.java)
            intent.putExtra("USER_NAME", userName)   // Pass Name
            intent.putExtra("USER_EMAIL", userEmail) // Pass Email
            startActivity(intent)
        }
        // Setup Booked Trips RecyclerView
        val rvBookedTrips = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvBookedTrips)

        // 1. Tell it to display items in a vertical list
        rvBookedTrips.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

        // 2. Set the adapter to ONLY the new BookedTripAdapter
        rvBookedTrips.adapter = BookedTripAdapter(BookingManager.bookedTrips.toMutableList())
        // 2. Setup Bottom Navigation Listener
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavTraveler)
        bottomNavigationView.selectedItemId = R.id.nav_explore // Ensure Explore is highlighted by default

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_explore -> {
                    // Already on Dashboard, do nothing
                }
                R.id.nav_trips -> {
                    val intent = Intent(this@TravelerDashboardActivity, SearchResultsActivity::class.java)
                    intent.putExtra("USER_NAME", userName)
                    intent.putExtra("USER_EMAIL", userEmail)
                    startActivity(intent)
                }
                R.id.nav_chat -> {
                    val intent = Intent(this@TravelerDashboardActivity, ChatListActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_events -> { // <--- ADD THIS BLOCK HERE!
                    val intent = Intent(this@TravelerDashboardActivity, EventsActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_profile -> {
                    val intent = Intent(this@TravelerDashboardActivity, ProfileActivity::class.java)
                    intent.putExtra("USER_NAME", userName)
                    intent.putExtra("USER_EMAIL", userEmail)
                    intent.putExtra("USER_ROLE", "TRAVELER")
                    startActivity(intent)
                }
            }
            true
        }

        // Setup Community Horizontal List
        val rvTravelBuddies = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvTravelBuddies)
        rvTravelBuddies.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false)
        rvTravelBuddies.adapter = TravelerAdapter(UserManager.communityTravelers)
    }
    override fun onResume() {
        super.onResume()
        // Every time the user comes back to the dashboard, refresh the list!
        val rvBookedTrips = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvBookedTrips)
        // CHANGE THESE TWO LINES:
        val adapter = rvBookedTrips.adapter as? BookedTripAdapter
        adapter?.updateData(BookingManager.bookedTrips)
    }
}