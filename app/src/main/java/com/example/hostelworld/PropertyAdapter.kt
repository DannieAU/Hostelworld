package com.example.hostelworld

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

class PropertyAdapter(private var properties: List<Property>) :
    RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder>() {

    class PropertyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvPropName)
        val tvRating: TextView = view.findViewById(R.id.tvPropRating)
        val tvType: TextView = view.findViewById(R.id.tvPropType)
        val tvPrice: TextView = view.findViewById(R.id.tvPropPrice)
        val btnBook: Button = view.findViewById(R.id.btnBook) // Added Button
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_property, parent, false)
        return PropertyViewHolder(view)
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val property = properties[position]
        holder.tvName.text = property.name
        holder.tvRating.text = "⭐ ${property.rating}"
        holder.tvType.text = property.type
        holder.tvPrice.text = "$${property.pricePerNight} / night"

        // Handle Booking Confirmation (FR-08)
        holder.btnBook.setOnClickListener {
            val context = holder.itemView.context
            val mockBookingRef = "HW" + (100000..999999).random() // Generate a 6-digit random ID

            AlertDialog.Builder(context)
                .setTitle("Booking Confirmed!")
                .setMessage("You have successfully booked a stay at ${property.name}.\n\nBooking Ref: $mockBookingRef\n\nA confirmation receipt has been sent to your email.")
                .setPositiveButton("Awesome") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    override fun getItemCount() = properties.size

    fun updateData(newProperties: List<Property>) {
        properties = newProperties
        notifyDataSetChanged()
    }
}