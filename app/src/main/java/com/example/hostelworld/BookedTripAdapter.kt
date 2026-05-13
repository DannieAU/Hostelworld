package com.example.hostelworld

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

class BookedTripAdapter(private var bookedTrips: MutableList<Property>) :
    RecyclerView.Adapter<BookedTripAdapter.BookedViewHolder>() {

    class BookedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvBookedName)
        val ivImage: ImageView = view.findViewById(R.id.ivBookedImage)
        val btnCancel: Button = view.findViewById(R.id.btnCancelBooking)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booked_trip, parent, false)
        return BookedViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookedViewHolder, position: Int) {
        val property = bookedTrips[position]
        holder.tvName.text = property.name
        holder.ivImage.setImageResource(property.imageResId)

        // Handle Cancellation
        holder.btnCancel.setOnClickListener {
            val context = holder.itemView.context
            AlertDialog.Builder(context)
                .setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel your stay at ${property.name}?")
                .setPositiveButton("Yes, Cancel") { dialog, _ ->
                    // Remove from our mock database
                    BookingManager.bookedTrips.remove(property)
                    // Refresh the specific list the adapter is holding
                    bookedTrips.remove(property)
                    notifyDataSetChanged() // Tell the UI to update

                    Toast.makeText(context, "Booking Cancelled", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .setNegativeButton("Keep Booking", null)
                .show()
        }
    }

    override fun getItemCount() = bookedTrips.size

    fun updateData(newTrips: List<Property>) {
        bookedTrips.clear()
        bookedTrips.addAll(newTrips)
        notifyDataSetChanged()
    }
}