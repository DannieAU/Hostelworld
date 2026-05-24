package com.example.hostelworld

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class ChatListActivity : AppCompatActivity() {

    data class ChatChannel(val name: String, val type: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        val rvChats = findViewById<RecyclerView>(R.id.rvChats)
        val tvNoChats = findViewById<TextView>(R.id.tvNoChats)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavChat)

        // Ensure "Chat" icon is highlighted
        bottomNav.selectedItemId = R.id.nav_chat

        // Bottom Nav Logic
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_explore -> {
                    startActivity(android.content.Intent(this, TravelerDashboardActivity::class.java))
                    finish() // Closes the current screen so they don't pile up in the background
                }
                R.id.nav_trips -> {
                    startActivity(android.content.Intent(this, SearchResultsActivity::class.java))
                    finish()
                }
                R.id.nav_chat -> {
                    // Already on the Chat screen, do nothing!
                }
                R.id.nav_events -> {
                    // --- FIXED: NOW POINTS TO THE NEW NOTIFICATIONS SCREEN! ---
                    startActivity(android.content.Intent(this, NotificationsActivity::class.java))
                    finish()
                }
                R.id.nav_profile -> {
                    val intent = android.content.Intent(this, ProfileActivity::class.java)
                    // Note: If you have userName and userEmail variables in the file, add the putExtra lines here!
                    startActivity(intent)
                    finish()
                }
            }
            true
        }

        // FR-15 Logic: Generate chats based on booked trips
        val unlockedChats = mutableListOf<ChatChannel>()
        for (trip in BookingManager.bookedTrips) {
            // Unlock a City Chat
            unlockedChats.add(ChatChannel("${trip.destination} City Chat", "Connect with travelers in ${trip.destination}"))
            // Unlock a Hostel Chat
            unlockedChats.add(ChatChannel("${trip.name} Chat", "Meet your dorm mates!"))
        }

        // Remove duplicates (in case they book 2 hostels in the same city)
        val uniqueChats = unlockedChats.distinct().toList()

        if (uniqueChats.isEmpty()) {
            tvNoChats.visibility = View.VISIBLE
            rvChats.visibility = View.GONE
        } else {
            rvChats.layoutManager = LinearLayoutManager(this)
            rvChats.adapter = ChatAdapter(uniqueChats)
        }
    }

    // Simple Adapter for the Chat List
    inner class ChatAdapter(private val chats: List<ChatChannel>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
        inner class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(android.R.id.text1)
            val tvType: TextView = view.findViewById(android.R.id.text2)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
            // Using a built-in Android layout for a quick list item!
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

            // Open the actual chat room when clicked
            holder.itemView.setOnClickListener {
                val intent = Intent(this@ChatListActivity, ChatRoomActivity::class.java)
                intent.putExtra("CHAT_NAME", chat.name)
                startActivity(intent)
            }
        }
        override fun getItemCount() = chats.size
    }
}