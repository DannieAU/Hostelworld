package com.example.hostelworld

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PublicProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_public_profile)

        // Setup back button on toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarPublicProfile)
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert) // Built-in back arrow
        toolbar.setNavigationOnClickListener { finish() }

        // Grab the traveler data passed from the click
        val name = intent.getStringExtra("TRAVELER_NAME") ?: "Unknown"
        val bio = intent.getStringExtra("TRAVELER_BIO") ?: ""
        val interests = intent.getStringExtra("TRAVELER_INTERESTS") ?: ""
        val imageId = intent.getIntExtra("TRAVELER_IMAGE", R.drawable.circle_placeholder)

        // Put the data on the screen
        findViewById<TextView>(R.id.tvPublicName).text = name
        findViewById<TextView>(R.id.tvPublicBio).text = bio
        findViewById<TextView>(R.id.tvPublicInterests).text = interests
        findViewById<ImageView>(R.id.ivPublicProfilePic).setImageResource(imageId)
    }
}