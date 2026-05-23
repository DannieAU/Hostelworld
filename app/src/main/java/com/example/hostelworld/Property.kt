package com.example.hostelworld

data class Property(
    val id: String,
    val name: String,
    val destination: String,
    val type: String,
    val rating: Double,
    val pricePerNight: Double,
    val amenities: List<String>,
    val imageResId: Int,
    val availableBeds: Int = 1 // <-- Added this to hold the capacity!
)