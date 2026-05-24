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
    val totalCost: Double
)

class HostDashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Properties List
    private lateinit var rvProperties: RecyclerView
    private lateinit var adapter: DashPropertyAdapter
    private val myProperties = mutableListOf<DashboardProperty>()

    // Reservations List
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

        // Setup My Properties (Horizontal)
        rvProperties = findViewById(R.id.rvDashboardProperties)
        rvProperties.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapter = DashPropertyAdapter(myProperties)
        rvProperties.adapter = adapter

        // Setup Recent Reservations (Vertical)
        rvRecentReservations = findViewById(R.id.rvRecentReservations)
        rvRecentReservations.layoutManager = LinearLayoutManager(this)
        resAdapter = RecentReservationAdapter(recentReservations)
        rvRecentReservations.adapter = resAdapter

        // Bottom Navigation Setup
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
                    startActivity(Intent(this@HostDashboardActivity, HostReportsActivity::class.java))
                    finish()
                }
                R.id.nav_host_profile -> {
                    val intent = Intent(this, HostProfileActivity::class.java)
                    intent.putExtra("USER_NAME", intent.getStringExtra("USER_NAME"))
                    intent.putExtra("USER_EMAIL", intent.getStringExtra("USER_EMAIL"))
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

        db.collection("properties")
            .whereEqualTo("hostUid", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                myProperties.clear()
                for (doc in documents) {
                    val prop = DashboardProperty(
                        propertyId = doc.id,
                        name = doc.getString("name") ?: "Unknown",
                        price = doc.getDouble("pricePerNight") ?: 0.0
                    )
                    myProperties.add(prop)
                }
                adapter.notifyDataSetChanged()

                val tvActiveListings = findViewById<TextView>(R.id.tvActiveListings)
                tvActiveListings.text = myProperties.size.toString()
            }
    }

    private fun fetchRecentReservations() {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("bookings")
            .whereEqualTo("hostUid", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    recentReservations.clear()
                    resAdapter.notifyDataSetChanged()

                    val tvCheckins = findViewById<TextView>(R.id.tvCheckinsToday)
                    tvCheckins.text = "0"
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

                    if (propertyId.isNotEmpty()) {
                        db.collection("properties").document(propertyId).get()
                            .addOnSuccessListener { propDoc ->

                                // --- NEW: SAFETY CHECK FOR ORPHANED BOOKINGS ---
                                if (propDoc.exists()) {
                                    val propName = propDoc.getString("name") ?: "Unknown Property"
                                    tempRes.add(HostReservation(bookingId, propName, "$checkIn to $checkOut", totalCost))
                                }

                                fetchCount++
                                if (fetchCount == totalDocs) {
                                    recentReservations.clear()
                                    recentReservations.addAll(tempRes)
                                    resAdapter.notifyDataSetChanged()

                                    val tvCheckins = findViewById<TextView>(R.id.tvCheckinsToday)
                                    tvCheckins.text = recentReservations.size.toString()
                                }
                            }
                            .addOnFailureListener {
                                fetchCount++
                                if (fetchCount == totalDocs) {
                                    recentReservations.clear()
                                    recentReservations.addAll(tempRes)
                                    resAdapter.notifyDataSetChanged()
                                }
                            }
                    } else {
                        fetchCount++
                    }
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

            holder.itemView.setOnLongClickListener {
                val context = holder.itemView.context
                AlertDialog.Builder(context)
                    .setTitle("Delete Property")
                    .setMessage("Are you sure you want to permanently delete '${listing.name}'?\n\nThis will remove it from the app for all travelers.")
                    .setPositiveButton("Delete") { _, _ ->

                        db.collection("properties").document(listing.propertyId).delete()
                            .addOnSuccessListener {
                                Toast.makeText(context, "Property deleted successfully", Toast.LENGTH_SHORT).show()

                                val currentPos = holder.adapterPosition
                                if (currentPos != RecyclerView.NO_POSITION) {
                                    myProperties.removeAt(currentPos)
                                    notifyItemRemoved(currentPos)

                                    val tvActiveListings = this@HostDashboardActivity.findViewById<TextView>(R.id.tvActiveListings)
                                    tvActiveListings?.text = myProperties.size.toString()
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Failed to delete: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()

                true
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
            holder.tvDates.text = "Dates: ${res.dates}"
            holder.tvTotal.text = "Payout: $${res.totalCost}"
        }

        override fun getItemCount() = reservations.size
    }
}