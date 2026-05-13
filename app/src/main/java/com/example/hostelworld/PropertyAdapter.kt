package com.example.hostelworld

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView

class PropertyAdapter(private var properties: List<Property>) :
    RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder>() {

    class PropertyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvPropName)
        val tvRating: TextView = view.findViewById(R.id.tvPropRating)
        val tvType: TextView = view.findViewById(R.id.tvPropType)
        val tvPrice: TextView = view.findViewById(R.id.tvPropPrice)
        val btnBook: Button = view.findViewById(R.id.btnBook) // Added Button
        val ivImage: ImageView = view.findViewById(R.id.ivPropImage)
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
        holder.ivImage.setImageResource(property.imageResId)

        // --- ADD THIS: Open Detail Screen when the whole card is clicked ---
        holder.itemView.setOnClickListener {
            val intent = android.content.Intent(holder.itemView.context, PropertyDetailActivity::class.java)
            // Pass the specific property details to the new screen
            intent.putExtra("PROP_ID", property.id)
            intent.putExtra("PROP_NAME", property.name)
            intent.putExtra("PROP_PRICE", property.pricePerNight)
            intent.putExtra("PROP_RATING", property.rating)
            intent.putExtra("PROP_IMAGE", property.imageResId)
            holder.itemView.context.startActivity(intent)
        }

        // Handle Booking Confirmation & Payment (FR-07 & FR-08)
        holder.btnBook.setOnClickListener {
            val context = holder.itemView.context

            // 1. Inflate the custom payment layout
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_payment, null)
            val tvPropName = dialogView.findViewById<TextView>(R.id.tvPaymentPropName)
            val btnConfirmPayment = dialogView.findViewById<Button>(R.id.btnConfirmPayment)

            tvPropName.text = "Booking: ${property.name} - $${property.pricePerNight}/night"

            // 2. Create the dialog
            val paymentDialog = AlertDialog.Builder(context)
                .setView(dialogView)
                .create()

            // 3. Handle Confirm Click
            btnConfirmPayment.setOnClickListener {
                paymentDialog.dismiss() // Close payment screen

                // Save to mock database
                if (!BookingManager.bookedTrips.contains(property)) {
                    BookingManager.bookedTrips.add(property)
                }

                // Show Final Confirmation
                val mockBookingRef = "HW" + (100000..999999).random()
                AlertDialog.Builder(context)
                    .setTitle("Booking Confirmed!")
                    .setMessage("You are all set for ${property.name}!\n\nRef: $mockBookingRef\n\nCheck your Traveler Dashboard to view or cancel this trip.")
                    .setPositiveButton("Awesome") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }

            paymentDialog.show()
        }
    }

    override fun getItemCount() = properties.size

    fun updateData(newProperties: List<Property>) {
        properties = newProperties
        notifyDataSetChanged()
    }
}