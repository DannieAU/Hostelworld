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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SearchResultsActivity : AppCompatActivity() {

    private lateinit var adapter: PropertyAdapter
    private lateinit var tvResultCount: TextView
    private lateinit var etSearchDestination: EditText

    // Mock Database
    private val allProperties = listOf(
        Property("1", "Tokyo Backpackers", "Tokyo", "Hostel", 9.5, 25.0, listOf("WiFi", "Kitchen")),
        Property("2", "Shinjuku Budget Hotel", "Tokyo", "Hotel", 8.2, 85.0, listOf("WiFi", "AC")),
        Property("3", "Kyoto Zen Hostel", "Kyoto", "Hostel", 9.8, 30.0, listOf("WiFi")),
        Property("4", "Osaka Party Hostel", "Osaka", "Hostel", 7.5, 15.0, listOf("Bar", "WiFi")),
        Property("5", "Tokyo Luxury Dorms", "Tokyo", "Hostel", 9.9, 45.0, listOf("AC", "Breakfast"))
    )

    private var currentFilteredList = allProperties.toList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_results)

        etSearchDestination = findViewById(R.id.etSearchDestination)
        tvResultCount = findViewById(R.id.tvResultCount)
        val btnFilter = findViewById<Button>(R.id.btnFilter)
        val rvProperties = findViewById<RecyclerView>(R.id.rvProperties)

        // Setup RecyclerView
        rvProperties.layoutManager = LinearLayoutManager(this)
        adapter = PropertyAdapter(currentFilteredList)
        rvProperties.adapter = adapter
        updateResultCount()

        // FR-03: Real-time Destination Search
        etSearchDestination.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters(maxPrice = 200.0, minRating = 0.0) // Re-filter based on new text
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // FR-04: Open Filter Dialog
        btnFilter.setOnClickListener {
            showFilterDialog()
        }

        // --- Navigation Bar Logic ---

        // Catch the user data passed from the Dashboard
        val userName = intent.getStringExtra("USER_NAME")
        val userEmail = intent.getStringExtra("USER_EMAIL")

        val bottomNavigationView = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavSearch)

        // Highlight the "Trips" icon because we are browsing trips
        bottomNavigationView.selectedItemId = R.id.nav_trips

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_explore -> {
                    // Close this screen to go back to the Traveler Dashboard
                    finish()
                }
                R.id.nav_trips -> {
                    // We are already here, do nothing
                }
                R.id.nav_events -> {
                    // Feature removed: Currently does nothing
                }
                R.id.nav_profile -> {
                    // Open Profile and pass the user data forward
                    val intent = android.content.Intent(this@SearchResultsActivity, ProfileActivity::class.java)
                    intent.putExtra("USER_NAME", userName)
                    intent.putExtra("USER_EMAIL", userEmail)
                    intent.putExtra("USER_ROLE", "TRAVELER")
                    startActivity(intent)
                }
            }
            true
        }
        // --- Make Dates Interactive (Check-in & Check-out Range) ---
        val tvSearchDates = findViewById<TextView>(R.id.tvSearchDates)
        tvSearchDates.setOnClickListener {
            // Build the Material Date Range Picker
            val builder = com.google.android.material.datepicker.MaterialDatePicker.Builder.dateRangePicker()
            builder.setTitleText("Select Check-in and Check-out Dates")

            val datePicker = builder.build()

            // Handle what happens when the user clicks "Save"
            datePicker.addOnPositiveButtonClickListener { selection ->
                // The selection contains the start and end dates in milliseconds
                val startDateMillis = selection.first
                val endDateMillis = selection.second

                // Create a formatter to convert milliseconds to "Apr 20" format
                val simpleDateFormat = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())

                val startDateString = simpleDateFormat.format(java.util.Date(startDateMillis))
                val endDateString = simpleDateFormat.format(java.util.Date(endDateMillis))

                // Update the text view with the formatted range
                tvSearchDates.text = "$startDateString - $endDateString"
            }

            // Show the picker
            datePicker.show(supportFragmentManager, "DATE_RANGE_PICKER")
        }

        // --- Make Guests Interactive ---
        val tvSearchGuests = findViewById<TextView>(R.id.tvSearchGuests)
        tvSearchGuests.setOnClickListener {
            val guestOptions = arrayOf("1 Guest", "2 Guests", "3 Guests", "4 Guests", "5+ Guests")

            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Number of Guests")
                .setItems(guestOptions) { dialog, which ->
                    // Update the TextView with the selected option
                    tvSearchGuests.text = guestOptions[which]
                    dialog.dismiss()
                }
                .show()
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

        // Dynamic price label update
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
            // Match Destination OR Name
            val matchesSearch = property.destination.lowercase().contains(searchQuery) ||
                    property.name.lowercase().contains(searchQuery)

            val matchesPrice = property.pricePerNight <= maxPrice
            val matchesRating = property.rating >= minRating

            matchesSearch && matchesPrice && matchesRating
        }.sortedBy { it.pricePerNight } // Default Sort by Price

        adapter.updateData(currentFilteredList)
        updateResultCount()
    }

    private fun updateResultCount() {
        tvResultCount.text = "${currentFilteredList.size} Results found"
    }
}