package com.example.hostelworld

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class PropertyDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_property_detail)
        // Setup Back Button
        val btnBack = findViewById<androidx.cardview.widget.CardView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // This gracefully closes the detail screen and slides back to Trips
        }

        // 1. Retrieve Data passed from the Adapter
        val propId = intent.getStringExtra("PROP_ID") ?: ""
        val propName = intent.getStringExtra("PROP_NAME") ?: "Unknown"
        val propPrice = intent.getDoubleExtra("PROP_PRICE", 0.0)
        val propRating = intent.getDoubleExtra("PROP_RATING", 0.0)
        val propImageId = intent.getIntExtra("PROP_IMAGE", R.drawable.circle_placeholder)

        // 2. Populate the Views
        findViewById<TextView>(R.id.tvDetailName).text = propName
        findViewById<TextView>(R.id.tvDetailRating).text = "⭐ $propRating"
        findViewById<TextView>(R.id.tvDetailPrice).text = "$$propPrice / night"
        findViewById<ImageView>(R.id.ivDetailImage).setImageResource(propImageId)

        // 3. Handle Booking Flow (FR-06 -> FR-07)
        val btnBook = findViewById<Button>(R.id.btnDetailBook)
        btnBook.setOnClickListener {
            // Setup dynamic prices based on the property
            val dormPrice = propPrice
            val privatePrice = propPrice * 2.5 // Make private rooms more expensive

            // Inflate Room Selection Dialog (FR-06)
            val roomView = LayoutInflater.from(this).inflate(R.layout.dialog_room_selection, null)
            val tvDormPrice = roomView.findViewById<TextView>(R.id.tvDormPrice)
            val tvPrivatePrice = roomView.findViewById<TextView>(R.id.tvPrivatePrice)
            val tvRoomTotal = roomView.findViewById<TextView>(R.id.tvRoomTotal)
            val btnContinue = roomView.findViewById<Button>(R.id.btnContinueToPayment)

            tvDormPrice.text = "$$dormPrice / bed"
            tvPrivatePrice.text = "$$privatePrice / room"

            var dormCount = 0
            var privateCount = 0

            // Function to update UI when counts change
            fun updateRoomTotal() {
                roomView.findViewById<TextView>(R.id.tvDormCount).text = dormCount.toString()
                roomView.findViewById<TextView>(R.id.tvPrivateCount).text = privateCount.toString()

                val total = (dormCount * dormPrice) + (privateCount * privatePrice)
                tvRoomTotal.text = "Total: $$total"
                btnContinue.isEnabled = total > 0 // Only allow continue if they picked a room!
            }

            // Button Click Listeners for + and -
            roomView.findViewById<Button>(R.id.btnDormPlus).setOnClickListener { dormCount++; updateRoomTotal() }
            roomView.findViewById<Button>(R.id.btnDormMinus).setOnClickListener { if (dormCount > 0) dormCount--; updateRoomTotal() }
            roomView.findViewById<Button>(R.id.btnPrivatePlus).setOnClickListener { privateCount++; updateRoomTotal() }
            roomView.findViewById<Button>(R.id.btnPrivateMinus).setOnClickListener { if (privateCount > 0) privateCount--; updateRoomTotal() }

            val roomDialog = AlertDialog.Builder(this)
                .setView(roomView)
                .create()

            // When they click Continue, open the Payment Dialog (FR-07)
            btnContinue.setOnClickListener {
                roomDialog.dismiss()
                var finalTotal = (dormCount * dormPrice) + (privateCount * privatePrice)
                var discountApplied = false // Track if they already used a code

                val paymentView = LayoutInflater.from(this).inflate(R.layout.dialog_payment, null)
                val tvPaymentPropName = paymentView.findViewById<TextView>(R.id.tvPaymentPropName)
                val btnConfirmPayment = paymentView.findViewById<Button>(R.id.btnConfirmPayment)

                // FR-16: Promo Code logic
                val etPromo = paymentView.findViewById<EditText>(R.id.etPromoCode)
                val btnApply = paymentView.findViewById<Button>(R.id.btnApplyPromo)

                tvPaymentPropName.text = "Booking: $propName\nTotal: $$finalTotal"

                btnApply.setOnClickListener {
                    val code = etPromo.text.toString().trim().uppercase()
                    if (!discountApplied && code == "SUMMER20") {
                        finalTotal *= 0.8 // 20% discount!
                        tvPaymentPropName.text = "Booking: $propName\nTotal: $$finalTotal (20% OFF Applied!)"
                        tvPaymentPropName.setTextColor(android.graphics.Color.parseColor("#4CAF50")) // Turn text green
                        discountApplied = true
                        android.widget.Toast.makeText(this, "Promo Code Applied!", android.widget.Toast.LENGTH_SHORT).show()
                    } else if (discountApplied) {
                        android.widget.Toast.makeText(this, "Discount already applied.", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        android.widget.Toast.makeText(this, "Invalid Code.", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }

                val paymentDialog = AlertDialog.Builder(this)
                    .setView(paymentView)
                    .create()

                btnConfirmPayment.setOnClickListener {
                    paymentDialog.dismiss()

                    // Save to Database
                    val propertyToSave = Property(propId, propName, "Unknown", "Unknown", propRating, finalTotal, listOf(), propImageId)
                    if (!BookingManager.bookedTrips.any { it.id == propId }) {
                        BookingManager.bookedTrips.add(propertyToSave)
                    }

                    // Final Confirmation
                    val mockRef = "HW" + (100000..999999).random()
                    AlertDialog.Builder(this)
                        .setTitle("Booking Confirmed!")
                        .setMessage("You are all set for $propName!\n\nRef: $mockRef\n\nCheck your Traveler Dashboard to view or cancel this trip.")
                        .setPositiveButton("Back to Search") { dialog, _ ->
                            dialog.dismiss()
                            finish()
                        }
                        .setCancelable(false)
                        .show()
                }
                paymentDialog.show()
            }
            roomDialog.show()
        }
    }
}