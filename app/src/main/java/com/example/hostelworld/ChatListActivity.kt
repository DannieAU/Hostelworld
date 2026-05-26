package com.example.hostelworld

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatListActivity : AppCompatActivity() {

    data class ChatChannel(val name: String, val type: String)

    private lateinit var rvChats: RecyclerView
    private lateinit var tvNoChats: TextView
    private val unlockedChats = mutableListOf<ChatChannel>()
    private lateinit var adapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        rvChats = findViewById(R.id.rvChats)
        tvNoChats = findViewById(R.id.tvNoChats)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavChat)

        bottomNav.selectedItemId = R.id.nav_chat

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_explore -> {
                    startActivity(Intent(this, TravelerDashboardActivity::class.java))
                    finish()
                }
                R.id.nav_trips -> {
                    startActivity(Intent(this, SearchResultsActivity::class.java))
                    finish()
                }
                R.id.nav_chat -> { } // Already here
                R.id.nav_events -> {
                    startActivity(Intent(this, NotificationsActivity::class.java))
                    finish()
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                }
            }
            true
        }

        rvChats.layoutManager = LinearLayoutManager(this)
        adapter = ChatAdapter(unlockedChats)
        rvChats.adapter = adapter

        fetchUnlockedChats()
    }

    private fun fetchUnlockedChats() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        // 1. Get all confirmed bookings for this traveler
        db.collection("bookings")
            .whereEqualTo("travelerUid", uid)
            .whereEqualTo("status", "Confirmed")
            .get()
            .addOnSuccessListener { bookings ->
                if (bookings.isEmpty) {
                    tvNoChats.visibility = View.VISIBLE
                    rvChats.visibility = View.GONE
                    return@addOnSuccessListener
                }

                tvNoChats.visibility = View.GONE
                rvChats.visibility = View.VISIBLE

                val tempChats = mutableListOf<ChatChannel>()
                var fetchCount = 0
                val totalDocs = bookings.size()

                for (doc in bookings) {
                    val propertyId = doc.getString("propertyId") ?: ""

                    // 2. Look up the property details to get the Name and City
                    if (propertyId.isNotEmpty()) {
                        db.collection("properties").document(propertyId).get()
                            .addOnSuccessListener { propDoc ->
                                if (propDoc.exists()) {
                                    val propName = propDoc.getString("name") ?: "Unknown Hostel"
                                    val location = propDoc.getString("location") ?: "Unknown City"

                                    // Unlock City Chat
                                    tempChats.add(ChatChannel("$location City Chat", "Connect with travelers in $location!"))
                                    // Unlock Hostel Chat
                                    tempChats.add(ChatChannel("$propName Chat", "Meet your dorm mates!"))
                                }

                                fetchCount++
                                if (fetchCount == totalDocs) {
                                    unlockedChats.clear()
                                    // Remove duplicates (e.g., if they booked 2 hostels in the same city)
                                    unlockedChats.addAll(tempChats.distinct())
                                    adapter.notifyDataSetChanged()
                                }
                            }
                            .addOnFailureListener { fetchCount++ }
                    } else {
                        fetchCount++
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load your chats.", Toast.LENGTH_SHORT).show()
            }
    }

    inner class ChatAdapter(private val chats: List<ChatChannel>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
        inner class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(android.R.id.text1)
            val tvType: TextView = view.findViewById(android.R.id.text2)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
            return ChatViewHolder(view)
        }

        override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
            val chat = chats[position]
            holder.tvName.text = chat.name
            holder.tvName.textSize = 18f
            holder.tvName.setTypeface(null, android.graphics.Typeface.BOLD)
            holder.tvType.text = chat.type
            holder.tvType.setTextColor(android.graphics.Color.GRAY)

            holder.itemView.setOnClickListener {
                val intent = Intent(this@ChatListActivity, ChatRoomActivity::class.java)
                intent.putExtra("CHAT_NAME", chat.name)
                startActivity(intent)
            }
        }
        override fun getItemCount() = chats.size
    }
}