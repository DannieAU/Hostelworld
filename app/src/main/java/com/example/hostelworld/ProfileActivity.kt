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

        val tvProfileName = findViewById<TextView>(R.id.tvProfileName)
        val tvProfileEmail = findViewById<TextView>(R.id.tvProfileEmail)
        val btnLogout = findViewById<MaterialButton>(R.id.btnLogout)
        val bottomNavProfile = findViewById<BottomNavigationView>(R.id.bottomNavProfile)

        val llCurrency = findViewById<LinearLayout>(R.id.llCurrency)
        val tvCurrencyValue = findViewById<TextView>(R.id.tvCurrencyValue)
        val llDistance = findViewById<LinearLayout>(R.id.llDistance)
        val tvDistanceValue = findViewById<TextView>(R.id.tvDistanceValue)

        val userName = intent.getStringExtra("USER_NAME")
        val userEmail = intent.getStringExtra("USER_EMAIL")
        val userRole = intent.getStringExtra("USER_ROLE")

        if (!userName.isNullOrEmpty()) {
            tvProfileName.text = userName
        }
        if (!userEmail.isNullOrEmpty()) {
            tvProfileEmail.text = "Logged in as: $userEmail"
        }

        if (userRole == "HOST") {
            bottomNavProfile.menu.clear()
            bottomNavProfile.inflateMenu(R.menu.host_bottom_nav_menu)
            bottomNavProfile.selectedItemId = R.id.nav_host_profile
        } else {
            bottomNavProfile.selectedItemId = R.id.nav_profile
        }

        btnLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

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

        bottomNavProfile.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_explore -> {
                    startActivity(android.content.Intent(this, TravelerDashboardActivity::class.java))
                    finish()
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
                    // --- FIXED: NOW POINTS TO THE NEW NOTIFICATIONS SCREEN! ---
                    startActivity(android.content.Intent(this, NotificationsActivity::class.java))
                    finish()
                }
                R.id.nav_profile -> {
                    val intent = android.content.Intent(this, ProfileActivity::class.java)
                    intent.putExtra("USER_NAME", userName)
                    intent.putExtra("USER_EMAIL", userEmail)
                    intent.putExtra("USER_ROLE", userRole)
                    startActivity(intent)
                    finish()
                }
            }
            true
        }

        val btnEdit = findViewById<android.widget.Button>(R.id.btnGoToEdit)
        btnEdit.setOnClickListener {
            val intent = android.content.Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("UserProfile", MODE_PRIVATE)

        val tvBio = findViewById<TextView>(R.id.tvDisplayBio)
        val tvInterests = findViewById<TextView>(R.id.tvDisplayInterests)

        tvBio.text = prefs.getString("BIO", "No bio added yet.")
        tvInterests.text = prefs.getString("INTERESTS", "No interests added yet.")
    }
}