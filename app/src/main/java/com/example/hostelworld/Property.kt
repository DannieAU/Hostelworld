package com.example.hostelworld

data class Property(
    val id: String,
    val name: String,
    val destination: String,
    val type: String, // e.g., "Hostel", "Hotel"
    val rating: Double,
    val pricePerNight: Double,
    val amenities: List<String>
)