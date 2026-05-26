package com.example.hostelworld

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PublicProfileActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_public_profile)

        db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val myUid = auth.currentUser?.uid ?: return

        val targetUid = intent.getStringExtra("TARGET_UID") ?: return
        val targetName = intent.getStringExtra("TARGET_NAME") ?: "Traveler"

        val tvName = findViewById<TextView>(R.id.tvPublicProfileName)
        val tvStatus = findViewById<TextView>(R.id.tvPublicProfileStatus)
        val btnChat = findViewById<Button>(R.id.btnPublicProfileChat)
        val btnBack = findViewById<Button>(R.id.btnPublicProfileBack)

        tvName.text = targetName
        btnBack.setOnClickListener { finish() }

        // 1. Fetch current user's active property IDs
        db.collection("bookings")
            .whereEqualTo("travelerUid", myUid)
            .whereEqualTo("status", "Confirmed")
            .get()
            .addOnSuccessListener { myDocs ->
                val myProperties = myDocs.documents.mapNotNull { it.getString("propertyId") }

                if (myProperties.isEmpty()) {
                    tvStatus.text = "You haven't booked any trips yet."
                    btnChat.text = "Book a trip to connect"
                    return@addOnSuccessListener
                }

                // 2. Fetch target user's active property IDs
                db.collection("bookings")
                    .whereEqualTo("travelerUid", targetUid)
                    .whereEqualTo("status", "Confirmed")
                    .get()
                    .addOnSuccessListener { targetDocs ->
                        val theirProperties = targetDocs.documents.mapNotNull { it.getString("propertyId") }

                        // 3. THE MAGIC: Do any of our Property IDs match?
                        val sharedPropertyIds = myProperties.intersect(theirProperties.toSet())

                        if (sharedPropertyIds.isNotEmpty()) {
                            val sharedPropId = sharedPropertyIds.first()

                            // 4. Get the property name to open the correct chat!
                            db.collection("properties").document(sharedPropId).get()
                                .addOnSuccessListener { propDoc ->
                                    val propName = propDoc.getString("name") ?: "Hostel"

                                    tvStatus.text = "You are both staying at $propName!"
                                    tvStatus.setTextColor(Color.parseColor("#4CAF50")) // Green text

                                    // Unlock the Chat Gateway
                                    btnChat.isEnabled = true
                                    btnChat.setBackgroundColor(Color.parseColor("#7C3AED")) // Purple
                                    btnChat.text = "Open $propName Chat"

                                    btnChat.setOnClickListener {
                                        val intent = Intent(this, ChatRoomActivity::class.java)
                                        intent.putExtra("CHAT_NAME", "$propName Chat")
                                        startActivity(intent)
                                        finish()
                                    }
                                }
                        } else {
                            // Locked!
                            tvStatus.text = "You don't have any shared destinations with $targetName."
                            btnChat.text = "No Shared Trips"
                        }
                    }
            }
    }
}