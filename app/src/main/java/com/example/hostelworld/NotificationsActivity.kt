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

data class AppNotification(val title: String, val message: String, val timestamp: Long)

class NotificationsActivity : AppCompatActivity() {

    private val notifList = mutableListOf<AppNotification>()
    private lateinit var adapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        val rvNotifications = findViewById<RecyclerView>(R.id.rvNotifications)
        rvNotifications.layoutManager = LinearLayoutManager(this)
        adapter = NotificationAdapter(notifList)
        rvNotifications.adapter = adapter

        fetchNotifications()

        // --- NEW: BOTTOM NAVIGATION ROUTING LOGIC ---
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavTraveler)

        // Highlight the Events/Notifications bell icon!
        bottomNavigationView.selectedItemId = R.id.nav_events

        bottomNavigationView.setOnItemSelectedListener { item ->
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
                    // Already here! Do nothing.
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    // You can optionally pass the USER_NAME and USER_EMAIL here if needed
                    startActivity(intent)
                    finish()
                }
            }
            true
        }
    }

    private fun fetchNotifications() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        notifList.clear()
        notifList.add(AppNotification(
            "Summer Backpacking Fest \uD83C\uDF1E",
            "Celebrate summer with 20% off all dorm beds. Use Code: SUMMER20",
            Long.MAX_VALUE
        ))

        if (uid == null) {
            adapter.notifyDataSetChanged()
            return
        }

        FirebaseFirestore.getInstance().collection("notifications")
            .whereEqualTo("travelerUid", uid)
            .get()
            .addOnSuccessListener { docs ->
                val realNotifs = mutableListOf<AppNotification>()

                for (doc in docs) {
                    val title = doc.getString("title") ?: "Notification"
                    val message = doc.getString("message") ?: ""
                    val time = doc.getLong("timestamp") ?: 0L
                    realNotifs.add(AppNotification(title, message, time))
                }

                realNotifs.sortByDescending { it.timestamp }
                notifList.addAll(realNotifs)
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading notifications: ${e.message}", Toast.LENGTH_SHORT).show()
                adapter.notifyDataSetChanged()
            }
    }

    inner class NotificationAdapter(private val items: List<AppNotification>) : RecyclerView.Adapter<NotificationAdapter.NotifViewHolder>() {
        inner class NotifViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitle: TextView = view.findViewById(R.id.tvNotifTitle)
            val tvMessage: TextView = view.findViewById(R.id.tvNotifMessage)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotifViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
            return NotifViewHolder(view)
        }
        override fun onBindViewHolder(holder: NotifViewHolder, position: Int) {
            holder.tvTitle.text = items[position].title
            holder.tvMessage.text = items[position].message
        }
        override fun getItemCount() = items.size
    }
}