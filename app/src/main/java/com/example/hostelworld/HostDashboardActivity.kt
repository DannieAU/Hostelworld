package com.example.hostelworld

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class HostDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host_dashboard)

        // 1. Retrieve BOTH the email and the name passed from LoginActivity
        val userEmail = intent.getStringExtra("USER_EMAIL")
        val userName = intent.getStringExtra("USER_NAME") // <-- Catching the Name!

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavHost)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // R.id.nav_host_dashboard -> // Home overview
                // R.id.nav_host_listings -> // Manage properties
                // R.id.nav_host_reports -> // View reports

                R.id.nav_host_profile -> {
                    val intent = Intent(this@HostDashboardActivity, ProfileActivity::class.java)
                    intent.putExtra("USER_EMAIL", userEmail)
                    intent.putExtra("USER_NAME", userName) // <-- Passing the Name forward to Profile!
                    intent.putExtra("USER_ROLE", "HOST")
                    startActivity(intent)
                }
            }
            true
        }
    }
}