package com.example.hostelworld

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class DashboardProperty(
    val propertyId: String,
    val name: String,
    val price: Double,
    val imageResId: Int = R.drawable.room_1
)

data class HostReservation(
    val bookingId: String,
    val propertyName: String,
    val dates: String,
    val totalCost: Double,
    val status: String
)

class HostDashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var rvProperties: RecyclerView
    private lateinit var adapter: DashPropertyAdapter
    private val myProperties = mutableListOf<DashboardProperty>()

    private lateinit var rvRecentReservations: RecyclerView
    private lateinit var resAdapter: RecentReservationAdapter
    private val recentReservations = mutableListOf<HostReservation>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host_dashboard)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val tvAddNew = findViewById<TextView>(R.id.tvAddNew)
        tvAddNew.setOnClickListener {
            startActivity(Intent(this, AddPropertyActivity::class.java))
        }

        rvProperties = findViewById(R.id.rvDashboardProperties)
        rvProperties.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapter = DashPropertyAdapter(myProperties)
        rvProperties.adapter = adapter

        rvRecentReservations = findViewById(R.id.rvRecentReservations)
        rvRecentReservations.layoutManager = LinearLayoutManager(this)
        resAdapter = RecentReservationAdapter(recentReservations)
        rvRecentReservations.adapter = resAdapter

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavHost)
        bottomNavigationView.selectedItemId = R.id.nav_host_dashboard

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_host_dashboard -> { }
                R.id.nav_host_listings -> {
                    startActivity(Intent(this, HostListingsActivity::class.java))
                    finish()
                }
                R.id.nav_host_reports -> {
                    startActivity(Intent(this, HostReportsActivity::class.java))
                    finish()
                }
                R.id.nav_host_profile -> {
                    val intent = Intent(this, HostProfileActivity::class.java)
                    intent.putExtra("USER_NAME", this.intent.getStringExtra("USER_NAME"))
                    intent.putExtra("USER_EMAIL", this.intent.getStringExtra("USER_EMAIL"))
                    startActivity(intent)
                    finish()
                }
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
        fetchMyProperties()
        fetchRecentReservations()
    }

    private fun fetchMyProperties() {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("properties").whereEqualTo("hostUid", currentUserId).get()
            .addOnSuccessListener { documents ->
                myProperties.clear()
                for (doc in documents) {
                    val prop = DashboardProperty(
                        doc.id, doc.getString("name") ?: "Unknown", doc.getDouble("pricePerNight") ?: 0.0
                    )
                    myProperties.add(prop)
                }
                adapter.notifyDataSetChanged()
                findViewById<TextView>(R.id.tvActiveListings).text = myProperties.size.toString()
            }
    }

    private fun fetchRecentReservations() {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("bookings").whereEqualTo("hostUid", currentUserId).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    recentReservations.clear()
                    resAdapter.notifyDataSetChanged()
                    findViewById<TextView>(R.id.tvCheckinsToday).text = "0"
                    return@addOnSuccessListener
                }

                val tempRes = mutableListOf<HostReservation>()
                var fetchCount = 0
                val totalDocs = documents.size()

                for (doc in documents) {
                    val propertyId = doc.getString("propertyId") ?: ""
                    val checkIn = doc.getString("checkInDate") ?: "Unknown"
                    val checkOut = doc.getString("checkOutDate") ?: "Unknown"
                    val totalCost = doc.getDouble("totalCost") ?: 0.0
                    val bookingId = doc.id
                    val status = doc.getString("status") ?: "Confirmed"

                    // --- NEW: REMOVE FROM DASHBOARD IF NO LONGER ACTIVE ---
                    if (status != "Confirmed") {
                        fetchCount++
                        if (fetchCount == totalDocs) {
                            recentReservations.clear()
                            recentReservations.addAll(tempRes.sortedBy { it.status })
                            resAdapter.notifyDataSetChanged()
                            findViewById<TextView>(R.id.tvCheckinsToday).text = recentReservations.size.toString()
                        }
                        continue // Skip adding it to the list!
                    }

                    if (propertyId.isNotEmpty()) {
                        db.collection("properties").document(propertyId).get()
                            .addOnSuccessListener { propDoc ->
                                if (propDoc.exists()) {
                                    val propName = propDoc.getString("name") ?: "Unknown Property"
                                    tempRes.add(HostReservation(bookingId, propName, "$checkIn to $checkOut", totalCost, status))
                                }
                                fetchCount++
                                if (fetchCount == totalDocs) {
                                    recentReservations.clear()
                                    recentReservations.addAll(tempRes.sortedBy { it.status })
                                    resAdapter.notifyDataSetChanged()
                                    findViewById<TextView>(R.id.tvCheckinsToday).text = recentReservations.size.toString()
                                }
                            }.addOnFailureListener { fetchCount++ }
                    } else { fetchCount++ }
                }
            }
    }

    inner class DashPropertyAdapter(private val listings: List<DashboardProperty>) : RecyclerView.Adapter<DashPropertyAdapter.DashViewHolder>() {
        inner class DashViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivImage: ImageView = view.findViewById(R.id.ivDashPropImage)
            val tvName: TextView = view.findViewById(R.id.tvDashPropName)
            val tvPrice: TextView = view.findViewById(R.id.tvDashPropPrice)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dashboard_property, parent, false)
            return DashViewHolder(view)
        }
        override fun onBindViewHolder(holder: DashViewHolder, position: Int) {
            val listing = listings[position]
            holder.tvName.text = listing.name
            holder.tvPrice.text = "$${listing.price} / night"
            holder.ivImage.setImageResource(listing.imageResId)

            holder.itemView.setOnClickListener {
                val intent = Intent(holder.itemView.context, PropertyDetailActivity::class.java)
                intent.putExtra("PROP_ID", listing.propertyId)
                intent.putExtra("PROP_NAME", listing.name)
                intent.putExtra("PROP_PRICE", listing.price)
                intent.putExtra("USER_ROLE", "HOST")
                holder.itemView.context.startActivity(intent)
            }
        }
        override fun getItemCount() = listings.size
    }

    inner class RecentReservationAdapter(private val reservations: List<HostReservation>) : RecyclerView.Adapter<RecentReservationAdapter.ResViewHolder>() {
        inner class ResViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvResPropertyName)
            val tvDates: TextView = view.findViewById(R.id.tvResDates)
            val tvTotal: TextView = view.findViewById(R.id.tvResTotal)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recent_reservation, parent, false)
            return ResViewHolder(view)
        }
        override fun onBindViewHolder(holder: ResViewHolder, position: Int) {
            val res = reservations[position]
            holder.tvName.text = "Booking: ${res.propertyName}"
            holder.tvDates.text = "Dates: ${res.dates} \nStatus: ${res.status}"
            holder.tvTotal.text = "Payout: $${res.totalCost}"

            holder.itemView.setOnClickListener {
                if (res.status == "Confirmed") {
                    AlertDialog.Builder(this@HostDashboardActivity)
                        .setTitle("Complete Stay")
                        .setMessage("Has the traveler finished their stay at ${res.propertyName}?")
                        .setPositiveButton("Mark Completed") { _, _ ->
                            db.collection("bookings").document(res.bookingId)
                                .update("status", "Completed")
                                .addOnSuccessListener {
                                    Toast.makeText(this@HostDashboardActivity, "Stay marked as completed!", Toast.LENGTH_SHORT).show()
                                    fetchRecentReservations() // Refresh list, which will now hide it!
                                }
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }
        }
        override fun getItemCount() = reservations.size
    }
}