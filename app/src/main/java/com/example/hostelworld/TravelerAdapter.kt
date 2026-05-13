package com.example.hostelworld

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TravelerAdapter(private val travelers: List<Traveler>) : RecyclerView.Adapter<TravelerAdapter.TravelerViewHolder>() {

    class TravelerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivBuddyImage)
        val tvName: TextView = view.findViewById(R.id.tvBuddyName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TravelerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_traveler, parent, false)
        return TravelerViewHolder(view)
    }

    override fun onBindViewHolder(holder: TravelerViewHolder, position: Int) {
        val traveler = travelers[position]
        holder.tvName.text = traveler.name
        holder.ivImage.setImageResource(traveler.imageResId)

        // When clicked, open their public profile!
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, PublicProfileActivity::class.java)
            intent.putExtra("TRAVELER_NAME", traveler.name)
            intent.putExtra("TRAVELER_BIO", traveler.bio)
            intent.putExtra("TRAVELER_INTERESTS", traveler.interests)
            intent.putExtra("TRAVELER_IMAGE", traveler.imageResId)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = travelers.size
}