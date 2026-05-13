package com.example.hostelworld

object HostManager {
    // We added 'val imageResId: Int' to the end!
    data class HostProperty(val name: String, val location: String, val price: String, val beds: String, val imageResId: Int)

    val myListings = mutableListOf<HostProperty>()
}