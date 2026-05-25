package com.example.hostelworld

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class SearchResultsActivity : AppCompatActivity() {

    private lateinit var adapter: PropertyAdapter
    private lateinit var tvResultCount: TextView
    private lateinit var etSearchDestination: EditText

    private var allProperties = mutableListOf<Property>()
    private var currentFilteredList = listOf<Property>()
    private var selectedGuestCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_results)

        etSearchDestination = findViewById(R.id.etSearchDestination)
        tvResultCount = findViewById(R.id.tvResultCount)
        val btnFilter = findViewById<Button>(R.id.btnFilter)
        val rvProperties = findViewById<RecyclerView>(R.id.rvProperties)

        rvProperties.layoutManager = LinearLayoutManager(this)
        adapter = PropertyAdapter(currentFilteredList)
        rvProperties.adapter = adapter

        fetchPropertiesFromFirebase()

        etSearchDestination.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters(maxPrice = 200.0, minRating = 0.0)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnFilter.setOnClickListener { showFilterDialog() }

        val userName = intent.getStringExtra("USER_NAME")
        val userEmail = intent.getStringExtra("USER_EMAIL")

        val bottomNavigationView = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavSearch)
        bottomNavigationView.selectedItemId = R.id.nav_trips

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_explore -> {
                    startActivity(android.content.Intent(this, TravelerDashboardActivity::class.java))
                    finish()
                }
                R.id.nav_trips -> { }
                R.id.nav_chat -> {
                    startActivity(android.content.Intent(this, ChatListActivity::class.java))
                    finish()
                }
                R.id.nav_events -> {
                    startActivity(android.content.Intent(this, NotificationsActivity::class.java))
                    finish()
                }
                R.id.nav_profile -> {
                    val intent = android.content.Intent(this, ProfileActivity::class.java)
                    intent.putExtra("USER_NAME", userName)
                    intent.putExtra("USER_EMAIL", userEmail)
                    startActivity(intent)
                    finish()
                }
            }
            true
        }

        val tvSearchDates = findViewById<TextView>(R.id.tvSearchDates)
        tvSearchDates.setOnClickListener {
            val builder = com.google.android.material.datepicker.MaterialDatePicker.Builder.dateRangePicker()
            builder.setTitleText("Select Check-in and Check-out Dates")
            val datePicker = builder.build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                val simpleDateFormat = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
                val startDateString = simpleDateFormat.format(java.util.Date(selection.first))
                val endDateString = simpleDateFormat.format(java.util.Date(selection.second))
                tvSearchDates.text = "$startDateString - $endDateString"
            }
            datePicker.show(supportFragmentManager, "DATE_RANGE_PICKER")
        }

        val tvSearchGuests = findViewById<TextView>(R.id.tvSearchGuests)
        tvSearchGuests.setOnClickListener {
            val guestOptions = arrayOf("1 Guest", "2 Guests", "3 Guests", "4 Guests", "5+ Guests")
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Number of Guests")
                .setItems(guestOptions) { dialog, which ->
                    tvSearchGuests.text = guestOptions[which]
                    selectedGuestCount = which + 1
                    applyFilters(200.0, 0.0)
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun fetchPropertiesFromFirebase() {
        val db = FirebaseFirestore.getInstance()
        db.collection("properties").get()
            .addOnSuccessListener { documents ->
                allProperties.clear()
                for (doc in documents) {
                    val id = doc.id
                    val name = doc.getString("name") ?: "Unknown"
                    val location = doc.getString("location") ?: "Unknown Location"
                    val price = doc.getDouble("pricePerNight") ?: 0.0
                    val beds = doc.getDouble("availableBeds")?.toInt() ?: 1

                    // SECURE FETCH: Defeats the Firebase Number Formatting crash
                    val dynamicRating = (doc.get("rating") as? Number)?.toDouble() ?: 5.0

                    val prop = Property(
                        id, name, location, "Hostel", dynamicRating, price, listOf("WiFi"), R.drawable.room_1, beds
                    )
                    allProperties.add(prop)
                }
                applyFilters(200.0, 0.0)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load properties", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showFilterDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_filter, null)
        val builder = AlertDialog.Builder(this).setView(dialogView)
        val dialog = builder.create()

        val sbPrice = dialogView.findViewById<SeekBar>(R.id.sbPrice)
        val tvPriceLabel = dialogView.findViewById<TextView>(R.id.tvPriceLabel)
        val rbRating = dialogView.findViewById<RatingBar>(R.id.rbRating)
        val btnApplyFilters = dialogView.findViewById<Button>(R.id.btnApplyFilters)

        sbPrice.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvPriceLabel.text = "Up to $$progress"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnApplyFilters.setOnClickListener {
            val maxPrice = sbPrice.progress.toDouble()
            val minRating = rbRating.rating.toDouble()
            applyFilters(maxPrice, minRating)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun applyFilters(maxPrice: Double, minRating: Double) {
        val searchQuery = etSearchDestination.text.toString().trim().lowercase()

        currentFilteredList = allProperties.filter { property ->
            val matchesSearch = property.destination.lowercase().contains(searchQuery) ||
                    property.name.lowercase().contains(searchQuery)
            val matchesPrice = property.pricePerNight <= maxPrice
            val matchesRating = property.rating >= minRating
            val matchesGuests = property.availableBeds >= selectedGuestCount

            matchesSearch && matchesPrice && matchesRating && matchesGuests
        }.sortedBy { it.pricePerNight }

        adapter.updateData(currentFilteredList)
        updateResultCount()
    }

    private fun updateResultCount() {
        tvResultCount.text = "${currentFilteredList.size} Results found"
    }
}