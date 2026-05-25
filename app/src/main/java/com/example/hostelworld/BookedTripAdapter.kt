package com.example.hostelworld

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BookedTripAdapter(private val trips: MutableList<BookedTrip>) : RecyclerView.Adapter<BookedTripAdapter.TripViewHolder>() {

    inner class TripViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivBookedTripImage)
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

        holder.ivImage.setImageResource(trip.imageResId)
        holder.tvPropName.text = trip.propertyName
        holder.tvDates.text = "Dates: ${trip.dates}\nStatus: ${trip.status}"
        holder.tvPolicy.text = "Policy: ${trip.policy}"
        holder.tvTotalCost.text = "Total: $${trip.totalCost}"

        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val context = holder.itemView.context

        when (trip.status) {
            "Completed" -> {
                holder.btnCancel.setBackgroundColor(Color.parseColor("#FFC107"))
                holder.btnCancel.text = "Leave a Review ★"
                holder.btnCancel.isEnabled = true

                holder.btnCancel.setOnClickListener {
                    val reviewView = LayoutInflater.from(context).inflate(R.layout.dialog_review, null)
                    val tvTitle = reviewView.findViewById<TextView>(R.id.tvReviewPropName)
                    val rbRating = reviewView.findViewById<RatingBar>(R.id.rbPropertyRating)
                    val etReview = reviewView.findViewById<EditText>(R.id.etReviewText)
                    val btnSubmit = reviewView.findViewById<Button>(R.id.btnSubmitReview)

                    tvTitle.text = "Rate your stay at ${trip.propertyName}"

                    val reviewDialog = AlertDialog.Builder(context).setView(reviewView).create()

                    btnSubmit.setOnClickListener {
                        val ratingVal = rbRating.rating.toDouble()
                        val textVal = etReview.text.toString().trim()

                        if (textVal.isNotEmpty()) {
                            val currentUserId = auth.currentUser?.uid ?: ""

                            val reviewData = hashMapOf(
                                "propertyId" to trip.propertyId,
                                "travelerUid" to currentUserId,
                                "rating" to ratingVal,
                                "feedback" to textVal,
                                "timestamp" to System.currentTimeMillis()
                            )

                            db.collection("reviews").add(reviewData).addOnSuccessListener { newReviewRef ->

                                db.collection("reviews").whereEqualTo("propertyId", trip.propertyId).get()
                                    .addOnSuccessListener { reviewDocs ->
                                        var totalStars = 0.0
                                        var count = 0
                                        for (rDoc in reviewDocs) {
                                            totalStars += ((rDoc.get("rating") as? Number)?.toDouble() ?: 5.0)
                                            count++
                                        }

                                        val includesNew = reviewDocs.documents.any { it.id == newReviewRef.id }
                                        if (!includesNew) {
                                            totalStars += ratingVal
                                            count++
                                        }

                                        val newAverage = if (count > 0) totalStars / count else ratingVal
                                        val roundedAvg = Math.round(newAverage * 10.0) / 10.0

                                        // --- NEW: ADDED FAILURE LISTENER TO CATCH FIREBASE BLOCKS! ---
                                        db.collection("properties").document(trip.propertyId).update("rating", roundedAvg)
                                            .addOnFailureListener { e ->
                                                Toast.makeText(context, "Math Error: Firebase blocked the rating update! ${e.message}", Toast.LENGTH_LONG).show()
                                            }
                                    }

                                db.collection("bookings").document(trip.bookingId)
                                    .update("status", "Reviewed")
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Review Published! Thank you.", Toast.LENGTH_SHORT).show()
                                        reviewDialog.dismiss()

                                        val notificationData = hashMapOf(
                                            "travelerUid" to currentUserId,
                                            "title" to "Trip Completed! \uD83C\uDF1F",
                                            "message" to "Thank you for reviewing your stay at ${trip.propertyName}. We hope you had a great time!",
                                            "timestamp" to System.currentTimeMillis()
                                        )
                                        db.collection("notifications").add(notificationData)

                                        val currentPosition = holder.adapterPosition
                                        if (currentPosition != RecyclerView.NO_POSITION) {
                                            trips.removeAt(currentPosition)
                                            notifyItemRemoved(currentPosition)
                                        }
                                    }
                            }
                        } else {
                            Toast.makeText(context, "Please write a short review.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    reviewDialog.show()
                }
            }
            "Reviewed" -> {
                holder.btnCancel.setBackgroundColor(Color.parseColor("#9E9E9E"))
                holder.btnCancel.text = "Reviewed"
                holder.btnCancel.isEnabled = false
            }
            else -> {
                if (trip.policy == "Non-Refundable") {
                    holder.btnCancel.setBackgroundColor(Color.parseColor("#9E9E9E"))
                    holder.btnCancel.text = "Non-Refundable"
                    holder.btnCancel.setOnClickListener {
                        Toast.makeText(context, "The host has disabled cancellation for this booking.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    holder.btnCancel.setBackgroundColor(Color.parseColor("#D32F2F"))
                    holder.btnCancel.text = "Cancel Booking"
                    holder.btnCancel.setOnClickListener {
                        AlertDialog.Builder(context)
                            .setTitle("Cancel Reservation")
                            .setMessage("Are you sure you want to cancel this booking? This action cannot be undone.")
                            .setPositiveButton("Yes, Cancel") { dialog, _ ->
                                db.collection("bookings").document(trip.bookingId).delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Booking Cancelled Successfully", Toast.LENGTH_SHORT).show()

                                        val currentUserId = auth.currentUser?.uid
                                        if (currentUserId != null) {
                                            val notificationData = hashMapOf(
                                                "travelerUid" to currentUserId,
                                                "title" to "Booking Cancelled \uD83D\uDEAB",
                                                "message" to "Your reservation at ${trip.propertyName} has been successfully cancelled.",
                                                "timestamp" to System.currentTimeMillis()
                                            )
                                            db.collection("notifications").add(notificationData)
                                        }

                                        val currentPosition = holder.adapterPosition
                                        if (currentPosition != RecyclerView.NO_POSITION) {
                                            trips.removeAt(currentPosition)
                                            notifyItemRemoved(currentPosition)
                                        }
                                    }.addOnFailureListener { e ->
                                        Toast.makeText(context, "Failed to cancel: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }.setNegativeButton("Keep Booking", null).show()
                    }
                }
            }
        }
    }

    override fun getItemCount() = trips.size
}