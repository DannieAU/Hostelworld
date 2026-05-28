package com.example.hostelworld

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val tvProfileName = findViewById<TextView>(R.id.tvProfileName)
        val tvProfileEmail = findViewById<TextView>(R.id.tvProfileEmail)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val bottomNavProfile = findViewById<BottomNavigationView>(R.id.bottomNavProfile)

        val llCurrency = findViewById<LinearLayout>(R.id.llCurrency)
        val tvCurrencyValue = findViewById<TextView>(R.id.tvCurrencyValue)
        val llDistance = findViewById<LinearLayout>(R.id.llDistance)
        val tvDistanceValue = findViewById<TextView>(R.id.tvDistanceValue)

        val llAdsConsent = findViewById<LinearLayout>(R.id.llAdsConsent)
        val llReport = findViewById<LinearLayout>(R.id.llReport)
        val llHelp = findViewById<LinearLayout>(R.id.llHelp)

        val userName = intent.getStringExtra("USER_NAME")
        val userEmail = intent.getStringExtra("USER_EMAIL")
        val userRole = intent.getStringExtra("USER_ROLE")

        // Initial setup from Login intent (Will be quickly overwritten by onResume if updated)
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
            FirebaseAuth.getInstance().signOut() // Best practice: Sign out of Firebase!
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
                tvCurrencyValue.text = "${currencies[which]} ▼"
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
                tvDistanceValue.text = "${distances[which]} ▼"
                dialog.dismiss()
            }
            builder.show()
        }

        llAdsConsent.setOnClickListener {
            Toast.makeText(this, "Ad Consent Settings Opened", Toast.LENGTH_SHORT).show()
        }
        llReport.setOnClickListener {
            Toast.makeText(this, "Report Center Opened", Toast.LENGTH_SHORT).show()
        }
        llHelp.setOnClickListener {
            Toast.makeText(this, "Help & Support Opened", Toast.LENGTH_SHORT).show()
        }

        bottomNavProfile.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_explore -> {
                    startActivity(Intent(this, TravelerDashboardActivity::class.java))
                    finish()
                }
                R.id.nav_trips -> {
                    startActivity(Intent(this, SearchResultsActivity::class.java))
                    finish()
                }
                R.id.nav_chat -> {
                    startActivity(Intent(this, ChatListActivity::class.java))
                    finish()
                }
                R.id.nav_events -> {
                    startActivity(Intent(this, NotificationsActivity::class.java))
                    finish()
                }
                R.id.nav_profile -> {
                    // Already here
                }
            }
            true
        }

        val btnEdit = findViewById<Button>(R.id.btnEditProfile)
        btnEdit.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
    }

    // --- BULLETPROOF DATA REFRESH ---
    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("UserProfile", MODE_PRIVATE)
        val tvName = findViewById<TextView>(R.id.tvProfileName)
        val tvBio = findViewById<TextView>(R.id.tvProfileBio)
        val tvInterests = findViewById<TextView>(R.id.tvProfileInterests)

        // 1. Load Local Data (Bio & Interests)
        tvBio.text = prefs.getString("BIO", "No bio added yet.")
        tvInterests.text = prefs.getString("INTERESTS", "No interests added yet.")

        // 2. Fetch Absolute Latest Name Directly from Firestore
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    val latestName = doc.getString("name")
                    if (!latestName.isNullOrEmpty()) {
                        tvName.text = latestName
                    }
                }
        }
    }
}