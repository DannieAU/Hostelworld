package com.example.hostelworld

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class HostListing(
    val propertyId: String,
    val name: String,
    val location: String,
    val price: Double,
    val beds: Int
)

class HostListingsActivity : AppCompatActivity() {

    private lateinit var rvListings: RecyclerView
    private lateinit var adapter: HostListingAdapter
    private val myProperties = mutableListOf<HostListing>()

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host_listings)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        rvListings = findViewById(R.id.rvHostListings)
        rvListings.layoutManager = LinearLayoutManager(this)
        adapter = HostListingAdapter(myProperties)
        rvListings.adapter = adapter

        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddProperty)
        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddPropertyActivity::class.java))
        }

        val bottomNavHost = findViewById<BottomNavigationView>(R.id.bottomNavHost)
        bottomNavHost.selectedItemId = R.id.nav_host_listings

        bottomNavHost.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_host_dashboard -> {
                    startActivity(Intent(this, HostDashboardActivity::class.java))
                    finish()
                }
                R.id.nav_host_listings -> { }
                R.id.nav_host_reports -> { }
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
        fetchListings()
    }

    private fun fetchListings() {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("properties")
            .whereEqualTo("hostUid", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                myProperties.clear()
                for (doc in documents) {
                    val prop = HostListing(
                        propertyId = doc.id,
                        name = doc.getString("name") ?: "Unknown",
                        location = doc.getString("location") ?: "Unknown Location",
                        price = doc.getDouble("pricePerNight") ?: 0.0,
                        beds = doc.getDouble("availableBeds")?.toInt() ?: 0
                    )
                    myProperties.add(prop)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load listings", Toast.LENGTH_SHORT).show()
            }
    }

    inner class HostListingAdapter(private val listings: List<HostListing>) : RecyclerView.Adapter<HostListingAdapter.ListingViewHolder>() {
        inner class ListingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvListingName)
            val tvLocation: TextView = view.findViewById(R.id.tvListingLocation)
            val tvPrice: TextView = view.findViewById(R.id.tvListingPrice)
            val tvBeds: TextView = view.findViewById(R.id.tvListingBeds)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListingViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_host_listing, parent, false)
            return ListingViewHolder(view)
        }

        override fun onBindViewHolder(holder: ListingViewHolder, position: Int) {
            val listing = listings[position]
            holder.tvName.text = listing.name
            holder.tvLocation.text = listing.location
            holder.tvPrice.text = "$${listing.price} / night"
            holder.tvBeds.text = "${listing.beds} Beds Available"

            // Tap to Edit
            holder.itemView.setOnClickListener {
                val intent = Intent(holder.itemView.context, PropertyDetailActivity::class.java)
                intent.putExtra("PROP_ID", listing.propertyId)
                intent.putExtra("PROP_NAME", listing.name)
                intent.putExtra("PROP_PRICE", listing.price)
                intent.putExtra("USER_ROLE", "HOST")
                holder.itemView.context.startActivity(intent)
            }

            // --- NEW: LONG PRESS TO DELETE PROPERTY ---
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
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Failed to delete: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()

                true // Tells Android the long-click was handled
            }
        }
        override fun getItemCount() = listings.size
    }
}