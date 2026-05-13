package com.example.hostelworld

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class EventsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavEvents)
        bottomNav.selectedItemId = R.id.nav_events

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_explore -> {
                    startActivity(android.content.Intent(this, TravelerDashboardActivity::class.java))
                    finish() // Closes the current screen so they don't pile up in the background
                }
                R.id.nav_trips -> {
                    startActivity(android.content.Intent(this, SearchResultsActivity::class.java))
                    finish()
                }
                R.id.nav_chat -> {
                    startActivity(android.content.Intent(this, ChatListActivity::class.java))
                    finish()
                }
                R.id.nav_events -> {
                    startActivity(android.content.Intent(this, EventsActivity::class.java))
                    finish()
                }
                R.id.nav_profile -> {
                    val intent = android.content.Intent(this, ProfileActivity::class.java)
                    // Note: If you have userName and userEmail variables in the file, add the putExtra lines here!
                    startActivity(intent)
                    finish()
                }
            }
            true
        }
    }
}