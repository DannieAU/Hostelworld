package com.example.hostelworld

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// NEW: A data class to hold real Firebase users!
data class RealTraveler(val uid: String, val name: String)

class TravelerAdapter(private val travelers: List<RealTraveler>) : RecyclerView.Adapter<TravelerAdapter.TravelerViewHolder>() {

    inner class TravelerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: ImageView = view.findViewById(R.id.ivTravelerAvatar)
        val tvName: TextView = view.findViewById(R.id.tvTravelerName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TravelerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_traveler, parent, false)
        return TravelerViewHolder(view)
    }

    override fun onBindViewHolder(holder: TravelerViewHolder, position: Int) {
        val traveler = travelers[position]
        holder.tvName.text = traveler.name

        // When you click a face, pass their exact Firebase UID to the profile screen
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, PublicProfileActivity::class.java)
            intent.putExtra("TARGET_UID", traveler.uid)
            intent.putExtra("TARGET_NAME", traveler.name)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = travelers.size
}