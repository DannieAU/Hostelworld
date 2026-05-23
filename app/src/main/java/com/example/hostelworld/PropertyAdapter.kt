package com.example.hostelworld

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PropertyAdapter(private var properties: List<Property>) :
    RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder>() {

    class PropertyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvPropName)
        val tvRating: TextView = view.findViewById(R.id.tvPropRating)
        val tvType: TextView = view.findViewById(R.id.tvPropType)
        val tvPrice: TextView = view.findViewById(R.id.tvPropPrice)
        val btnBook: Button = view.findViewById(R.id.btnBook)
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

        // Create a single function to route users to the Real Firebase Detail Screen
        fun openDetailScreen() {
            val intent = Intent(holder.itemView.context, PropertyDetailActivity::class.java)

            // THE CRUCIAL ID LINK!
            intent.putExtra("PROP_ID", property.id)

            intent.putExtra("PROP_NAME", property.name)
            intent.putExtra("PROP_PRICE", property.pricePerNight)
            intent.putExtra("PROP_RATING", property.rating)
            intent.putExtra("PROP_IMAGE", property.imageResId)

            holder.itemView.context.startActivity(intent)
        }

        // --- Route BOTH clicks to the Detail Screen to prevent fake mock bookings! ---
        holder.itemView.setOnClickListener {
            openDetailScreen()
        }

        holder.btnBook.setOnClickListener {
            openDetailScreen()
        }
    }

    override fun getItemCount() = properties.size

    fun updateData(newProperties: List<Property>) {
        properties = newProperties
        notifyDataSetChanged()
    }
}