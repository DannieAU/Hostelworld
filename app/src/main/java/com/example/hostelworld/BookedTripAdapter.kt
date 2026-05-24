package com.example.hostelworld

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BookedTripAdapter(private val trips: MutableList<BookedTrip>) : RecyclerView.Adapter<BookedTripAdapter.TripViewHolder>() {

    inner class TripViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPropName: TextView = view.findViewById(R.id.tvBookedName)
        val tvDates: TextView = view.findViewById(R.id.tvBookedDates)
        val tvPolicy: TextView = view.findViewById(R.id.tvBookedPolicy)
        val tvTotalCost: TextView = view.findViewById(R.id.tvBookedTotal)
        val btnCancel: Button = view.findViewById(R.id.btnCancelBooking)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_booked_trip, parent, false)
        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val trip = trips[position]
        holder.tvPropName.text = trip.propertyName
        holder.tvDates.text = "Dates: ${trip.dates}"
        holder.tvPolicy.text = "Policy: ${trip.policy}"
        holder.tvTotalCost.text = "Total: $${trip.totalCost}"

        // --- ENFORCE CANCELLATION RULES ---
        if (trip.policy == "Non-Refundable") {
            // Turn the button gray and disable the delete function
            holder.btnCancel.setBackgroundColor(Color.parseColor("#9E9E9E"))
            holder.btnCancel.text = "Non-Refundable"

            holder.btnCancel.setOnClickListener {
                Toast.makeText(holder.itemView.context, "The host has disabled cancellation for this booking.", Toast.LENGTH_LONG).show()
            }
        } else {
            // Flexible policy - Allow standard cancellation
            holder.btnCancel.setBackgroundColor(Color.parseColor("#D32F2F"))
            holder.btnCancel.text = "Cancel Booking"

            holder.btnCancel.setOnClickListener {
                val context = holder.itemView.context
                val db = FirebaseFirestore.getInstance()
                val auth = FirebaseAuth.getInstance() // NEW: Get the Auth instance

                AlertDialog.Builder(context)
                    .setTitle("Cancel Reservation")
                    .setMessage("Are you sure you want to cancel this booking? This action cannot be undone.")
                    .setPositiveButton("Yes, Cancel") { dialog, _ ->

                        // 1. Delete the booking from the database
                        db.collection("bookings").document(trip.bookingId)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(context, "Booking Cancelled Successfully", Toast.LENGTH_SHORT).show()

                                // --- NEW: FIRE OFF A CANCELLATION NOTIFICATION ---
                                val currentUserId = auth.currentUser?.uid
                                if (currentUserId != null) {
                                    val notificationData = hashMapOf(
                                        "travelerUid" to currentUserId,
                                        "title" to "Booking Cancelled 🚫",
                                        "message" to "Your reservation at ${trip.propertyName} has been successfully cancelled.",
                                        "timestamp" to System.currentTimeMillis()
                                    )
                                    db.collection("notifications").add(notificationData)
                                }

                                // 2. Remove it from the screen
                                val currentPosition = holder.adapterPosition
                                if (currentPosition != RecyclerView.NO_POSITION) {
                                    trips.removeAt(currentPosition)
                                    notifyItemRemoved(currentPosition)
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Failed to cancel: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .setNegativeButton("Keep Booking", null)
                    .show()
            }
        }
    }

    override fun getItemCount() = trips.size
}