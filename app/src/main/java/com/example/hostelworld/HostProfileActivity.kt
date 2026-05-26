package com.example.hostelworld

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class HostProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host_profile)

        val tvHostName = findViewById<TextView>(R.id.tvHostName)
        val tvHostEmail = findViewById<TextView>(R.id.tvHostEmail)
        val btnLogout = findViewById<MaterialButton>(R.id.btnHostLogout)

        // 1. Grab user data
        val userName = intent.getStringExtra("USER_NAME") ?: "Host"
        val userEmail = intent.getStringExtra("USER_EMAIL") ?: "host@email.com"

        tvHostName.text = userName
        tvHostEmail.text = "Logged in as: $userEmail"

        // 2. Handle Logout
        btnLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // 3. Currency Setup
        val llCurrency = findViewById<LinearLayout>(R.id.llHostCurrency)
        val tvCurrencyValue = findViewById<TextView>(R.id.tvHostCurrencyValue)
        val currencies = arrayOf("USD ($)", "EUR (€)", "GBP (£)", "PHP (₱)", "JPY (¥)")
        var selectedCurrencyIndex = 0

        llCurrency.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Select Currency")
                .setSingleChoiceItems(currencies, selectedCurrencyIndex) { dialog, which ->
                    selectedCurrencyIndex = which
                    tvCurrencyValue.text = "${currencies[which]} ▼"
                    dialog.dismiss()
                }.show()
        }

        // 4. Distance Setup
        val llDistance = findViewById<LinearLayout>(R.id.llHostDistance)
        val tvDistanceValue = findViewById<TextView>(R.id.tvHostDistanceValue)
        val distances = arrayOf("Kilometers (Km)", "Miles (Mi)")
        var selectedDistanceIndex = 0

        llDistance.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Select Distance Unit")
                .setSingleChoiceItems(distances, selectedDistanceIndex) { dialog, which ->
                    selectedDistanceIndex = which
                    tvDistanceValue.text = "${distances[which]} ▼"
                    dialog.dismiss()
                }.show()
        }

        // 5. Host Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavHostProfile)
        bottomNav.selectedItemId = R.id.nav_host_profile

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_host_dashboard -> {
                    startActivity(Intent(this, HostDashboardActivity::class.java))
                    finish()
                }
                R.id.nav_host_listings -> {
                    startActivity(Intent(this, HostListingsActivity::class.java))
                    finish()
                }
                R.id.nav_host_reports -> {
                    // --- FIXED: Uncommented the routing code! ---
                    startActivity(Intent(this, HostReportsActivity::class.java))
                    finish()
                }
                R.id.nav_host_profile -> {
                    // Already here, do nothing!
                }
            }
            true
        }
    }
}