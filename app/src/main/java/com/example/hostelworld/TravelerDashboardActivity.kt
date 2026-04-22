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

        // 2. Setup Bottom Navigation Listener
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavTraveler)
        bottomNavigationView.selectedItemId = R.id.nav_explore // Ensure Explore is highlighted by default

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_explore -> {
                    // Already on Dashboard, do nothing
                }
                R.id.nav_trips -> {
                    // Navigate to Search Accommodations (FR-03/04) as the "Trips" tab
                    val intent = Intent(this@TravelerDashboardActivity, SearchResultsActivity::class.java)
                    intent.putExtra("USER_NAME", userName)
                    intent.putExtra("USER_EMAIL", userEmail)
                    startActivity(intent)
                }
                R.id.nav_events -> {
                    // Feature removed: Currently does nothing
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
    }
}