package com.example.hostelworld

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // 1. Connect UI Elements
        val tvProfileName = findViewById<TextView>(R.id.tvProfileName)
        val tvProfileEmail = findViewById<TextView>(R.id.tvProfileEmail)
        val btnLogout = findViewById<MaterialButton>(R.id.btnLogout)
        val bottomNavProfile = findViewById<BottomNavigationView>(R.id.bottomNavProfile)

        val llCurrency = findViewById<LinearLayout>(R.id.llCurrency)
        val tvCurrencyValue = findViewById<TextView>(R.id.tvCurrencyValue)
        val llDistance = findViewById<LinearLayout>(R.id.llDistance)
        val tvDistanceValue = findViewById<TextView>(R.id.tvDistanceValue)

        // 2. Retrieve Data passed from Dashboards
        val userName = intent.getStringExtra("USER_NAME")
        val userEmail = intent.getStringExtra("USER_EMAIL")
        val userRole = intent.getStringExtra("USER_ROLE")

        // 3. Update UI text with active user details
        if (!userName.isNullOrEmpty()) {
            tvProfileName.text = userName
        }
        if (!userEmail.isNullOrEmpty()) {
            tvProfileEmail.text = "Logged in as: $userEmail"
        }

        // 4. Setup correct bottom navigation menu based on role
        if (userRole == "HOST") {
            bottomNavProfile.menu.clear()
            bottomNavProfile.inflateMenu(R.menu.host_bottom_nav_menu)
            bottomNavProfile.selectedItemId = R.id.nav_host_profile
        } else {
            bottomNavProfile.selectedItemId = R.id.nav_profile
        }

        // 5. Handle Log Out
        btnLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // 6. Settings Interaction Logic - Currency
        val currencies = arrayOf("USD ($)", "EUR (€)", "GBP (£)", "PHP (₱)", "JPY (¥)")
        var selectedCurrencyIndex = 0

        llCurrency.setOnClickListener {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle("Select Currency")
            builder.setSingleChoiceItems(currencies, selectedCurrencyIndex) { dialog, which ->
                selectedCurrencyIndex = which
                tvCurrencyValue.text = currencies[which]
                dialog.dismiss()
            }
            builder.show()
        }

        // 7. Settings Interaction Logic - Distance
        val distances = arrayOf("Kilometers (Km)", "Miles (Mi)")
        var selectedDistanceIndex = 0

        llDistance.setOnClickListener {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle("Select Distance Unit")
            builder.setSingleChoiceItems(distances, selectedDistanceIndex) { dialog, which ->
                selectedDistanceIndex = which
                tvDistanceValue.text = distances[which]
                dialog.dismiss()
            }
            builder.show()
        }

        // 8. Bottom Navigation Click Listener
        bottomNavProfile.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_explore -> finish()
                R.id.nav_host_dashboard -> finish()
            }
            true
        }
    }
}