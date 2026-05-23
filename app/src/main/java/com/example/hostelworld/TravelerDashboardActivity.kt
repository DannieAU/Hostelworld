package com.example.hostelworld

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class BookedTrip(
    val bookingId: String,
    val propertyId: String,
    val propertyName: String,
    val totalCost: Double,
    val status: String,
    val dates: String,
    val policy: String
)

class TravelerDashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var rvBookedTrips: RecyclerView
    private lateinit var adapter: BookedTripAdapter
    private val bookedTripsList = mutableListOf<BookedTrip>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_traveler_dashboard)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val userEmail = intent.getStringExtra("USER_EMAIL")
        val userName = intent.getStringExtra("USER_NAME")

        val cvSearch = findViewById<CardView>(R.id.cvSearch)
        cvSearch.setOnClickListener {
            val intent = Intent(this@TravelerDashboardActivity, SearchResultsActivity::class.java)
            intent.putExtra("USER_NAME", userName)
            intent.putExtra("USER_EMAIL", userEmail)
            startActivity(intent)
        }

        rvBookedTrips = findViewById(R.id.rvBookedTrips)
        rvBookedTrips.layoutManager = LinearLayoutManager(this)
        adapter = BookedTripAdapter(bookedTripsList)
        rvBookedTrips.adapter = adapter

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavTraveler)
        bottomNavigationView.selectedItemId = R.id.nav_explore

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_explore -> { }
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
                R.id.nav_events -> {
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

        val rvTravelBuddies = findViewById<RecyclerView>(R.id.rvTravelBuddies)
        rvTravelBuddies.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvTravelBuddies.adapter = TravelerAdapter(UserManager.communityTravelers)
    }

    override fun onResume() {
        super.onResume()
        fetchBookingsFromFirebase()
    }

    private fun fetchBookingsFromFirebase() {
        val currentUser = auth.currentUser ?: return

        db.collection("bookings")
            .whereEqualTo("travelerUid", currentUser.uid)
            .get()
            .addOnSuccessListener { documents ->

                if (documents.isEmpty) {
                    bookedTripsList.clear()
                    adapter.notifyDataSetChanged()
                    return@addOnSuccessListener
                }

                val tempTrips = mutableListOf<BookedTrip>()
                var fetchCount = 0
                val totalDocs = documents.size()

                for (doc in documents) {
                    val propertyId = doc.getString("propertyId") ?: ""
                    val totalCost = doc.getDouble("totalCost") ?: 0.0
                    val status = doc.getString("status") ?: "Confirmed"
                    val bookingId = doc.id

                    val checkIn = doc.getString("checkInDate") ?: ""
                    val checkOut = doc.getString("checkOutDate") ?: ""
                    val policy = doc.getString("cancellationPolicy") ?: "Non-Refundable"
                    val dates = "$checkIn - $checkOut"

                    if (propertyId.isNotEmpty()) {
                        db.collection("properties").document(propertyId).get()
                            .addOnSuccessListener { propDoc ->

                                // --- NEW: ONLY ADD TO UI IF THE PROPERTY STILL EXISTS ---
                                if (propDoc.exists()) {
                                    val propName = propDoc.getString("name") ?: "Unknown Property"
                                    tempTrips.add(BookedTrip(bookingId, propertyId, propName, totalCost, status, dates, policy))
                                }

                                fetchCount++
                                if (fetchCount == totalDocs) {
                                    bookedTripsList.clear()
                                    bookedTripsList.addAll(tempTrips)
                                    adapter.notifyDataSetChanged()
                                }
                            }
                            .addOnFailureListener {
                                fetchCount++
                                if (fetchCount == totalDocs) {
                                    bookedTripsList.clear()
                                    bookedTripsList.addAll(tempTrips)
                                    adapter.notifyDataSetChanged()
                                }
                            }
                    } else {
                        fetchCount++
                        if (fetchCount == totalDocs) {
                            bookedTripsList.clear()
                            bookedTripsList.addAll(tempTrips)
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load bookings", Toast.LENGTH_SHORT).show()
            }
    }
}