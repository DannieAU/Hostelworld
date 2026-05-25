package com.example.hostelworld

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PropertyReview(val rating: Double, val feedback: String, val dateStr: String, val timestamp: Long)

class PropertyDetailActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private val reviewList = mutableListOf<PropertyReview>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_property_detail)

        db = FirebaseFirestore.getInstance()

        val btnBack = findViewById<androidx.cardview.widget.CardView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        val propId = intent.getStringExtra("PROP_ID") ?: ""
        val propName = intent.getStringExtra("PROP_NAME") ?: "Unknown"
        val propPrice = intent.getDoubleExtra("PROP_PRICE", 0.0)
        var propRating = intent.getDoubleExtra("PROP_RATING", 5.0)
        val propImageId = intent.getIntExtra("PROP_IMAGE", R.drawable.circle_placeholder)
        val userRole = intent.getStringExtra("USER_ROLE") ?: "TRAVELER"

        var maxAvailableBeds = 1
        var cancellationPolicy = "Non-Refundable"

        if (propId.isNotEmpty()) {
            db.collection("properties").document(propId).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        maxAvailableBeds = doc.getDouble("availableBeds")?.toInt() ?: 1
                        cancellationPolicy = doc.getString("cancellationPolicy") ?: "Non-Refundable"
                        propRating = (doc.get("rating") as? Number)?.toDouble() ?: 5.0

                        // --- NEW: FETCH THE LOCATION HOST TYPED IN ---
                        val locationStr = doc.getString("location") ?: "Cebu City, Philippines"

                        findViewById<TextView>(R.id.tvDetailBeds)?.text = "🛏️ $maxAvailableBeds Beds Available"
                        findViewById<TextView>(R.id.tvDetailCancelPolicy)?.text = "Cancellation Rule: $cancellationPolicy"
                        findViewById<TextView>(R.id.tvDetailRating)?.text = "⭐ $propRating"

                        // --- NEW: WIRE UP THE MAP CLICK LISTENER ---
                        val ivDetailMap = findViewById<ImageView>(R.id.ivDetailMap)
                        ivDetailMap.setOnClickListener {
                            // Create a URI that searches Google Maps for the address
                            val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(locationStr)}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")

                            // Check if they have the Google Maps app installed
                            if (mapIntent.resolveActivity(packageManager) != null) {
                                startActivity(mapIntent)
                            } else {
                                // Fallback: Open it in their web browser if they don't have the app!
                                val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(locationStr)}")
                                startActivity(Intent(Intent.ACTION_VIEW, browserUri))
                            }
                        }
                    }
                }

            val rvReviews = findViewById<RecyclerView>(R.id.rvDetailReviews)
            if (rvReviews != null) {
                rvReviews.layoutManager = LinearLayoutManager(this)
                val reviewAdapter = ReviewAdapter(reviewList)
                rvReviews.adapter = reviewAdapter

                db.collection("reviews").whereEqualTo("propertyId", propId).get()
                    .addOnSuccessListener { docs ->
                        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        for (doc in docs) {
                            val rating = (doc.get("rating") as? Number)?.toDouble() ?: 5.0
                            val feedback = doc.getString("feedback") ?: ""
                            val time = doc.getLong("timestamp") ?: 0L
                            reviewList.add(PropertyReview(rating, feedback, sdf.format(Date(time)), time))
                        }
                        reviewList.sortByDescending { it.timestamp }
                        reviewAdapter.notifyDataSetChanged()
                    }
            }
        }

        findViewById<TextView>(R.id.tvDetailName).text = propName
        findViewById<TextView>(R.id.tvDetailRating).text = "⭐ $propRating"
        findViewById<TextView>(R.id.tvDetailPrice).text = "$$propPrice / night"
        findViewById<ImageView>(R.id.ivDetailImage).setImageResource(propImageId)

        val btnAction = findViewById<Button>(R.id.btnDetailBook)

        if (userRole == "HOST") {
            btnAction.text = "Edit Property"
            btnAction.setBackgroundColor(android.graphics.Color.parseColor("#7C3AED"))
            btnAction.setOnClickListener {
                val editIntent = Intent(this, AddPropertyActivity::class.java)
                editIntent.putExtra("PROPERTY_ID", propId)
                startActivity(editIntent)
                finish()
            }
        } else {
            btnAction.text = "Book Now"
            btnAction.setOnClickListener {
                val builder = MaterialDatePicker.Builder.dateRangePicker()
                builder.setTitleText("Select Check-in and Check-out Dates")
                val datePicker = builder.build()

                datePicker.addOnPositiveButtonClickListener { selection ->
                    val newCheckIn = selection.first ?: return@addOnPositiveButtonClickListener
                    val newCheckOut = selection.second ?: return@addOnPositiveButtonClickListener
                    checkAvailabilityAndProceed(newCheckIn, newCheckOut, propId, propName, propPrice, maxAvailableBeds, cancellationPolicy)
                }
                datePicker.show(supportFragmentManager, "DATE_PICKER")
            }
        }
    }

    private fun checkAvailabilityAndProceed(newCheckIn: Long, newCheckOut: Long, propId: String, propName: String, propPrice: Double, maxBeds: Int, cancellationPolicy: String) {
        db.collection("bookings").whereEqualTo("propertyId", propId).get()
            .addOnSuccessListener { documents ->
                var isAvailable = true
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

                for (doc in documents) {
                    val existingInStr = doc.getString("checkInDate") ?: ""
                    val existingOutStr = doc.getString("checkOutDate") ?: ""

                    try {
                        val existingIn = sdf.parse(existingInStr)?.time ?: 0L
                        val existingOut = sdf.parse(existingOutStr)?.time ?: 0L
                        if (newCheckIn < existingOut && newCheckOut > existingIn) {
                            isAvailable = false
                            break
                        }
                    } catch (e: Exception) {}
                }

                if (isAvailable) {
                    showRoomSelectionDialog(newCheckIn, newCheckOut, propId, propName, propPrice, maxBeds, cancellationPolicy)
                } else {
                    Toast.makeText(this, "Sorry! This property is already booked for these dates.", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun showRoomSelectionDialog(checkInTime: Long, checkOutTime: Long, propId: String, propName: String, propPrice: Double, maxAvailableBeds: Int, cancellationPolicy: String) {
        val dormPrice = propPrice
        val privatePrice = propPrice * 2.5

        val roomView = LayoutInflater.from(this).inflate(R.layout.dialog_room_selection, null)
        val tvDormPrice = roomView.findViewById<TextView>(R.id.tvDormPrice)
        val tvPrivatePrice = roomView.findViewById<TextView>(R.id.tvPrivatePrice)
        val tvRoomTotal = roomView.findViewById<TextView>(R.id.tvRoomTotal)
        val btnContinue = roomView.findViewById<Button>(R.id.btnContinueToPayment)

        tvDormPrice.text = "$$dormPrice / bed"
        tvPrivatePrice.text = "$$privatePrice / room"

        var dormCount = 0
        var privateCount = 0

        fun updateRoomTotal() {
            roomView.findViewById<TextView>(R.id.tvDormCount).text = dormCount.toString()
            roomView.findViewById<TextView>(R.id.tvPrivateCount).text = privateCount.toString()
            val total = (dormCount * dormPrice) + (privateCount * privatePrice)
            tvRoomTotal.text = "Total: $$total"
            btnContinue.isEnabled = total > 0
        }

        roomView.findViewById<Button>(R.id.btnDormPlus).setOnClickListener {
            if ((dormCount + privateCount) < maxAvailableBeds) { dormCount++; updateRoomTotal() }
        }
        roomView.findViewById<Button>(R.id.btnDormMinus).setOnClickListener { if (dormCount > 0) dormCount--; updateRoomTotal() }
        roomView.findViewById<Button>(R.id.btnPrivatePlus).setOnClickListener {
            if ((dormCount + privateCount) < maxAvailableBeds) { privateCount++; updateRoomTotal() }
        }
        roomView.findViewById<Button>(R.id.btnPrivateMinus).setOnClickListener { if (privateCount > 0) privateCount--; updateRoomTotal() }

        val roomDialog = AlertDialog.Builder(this).setView(roomView).create()

        btnContinue.setOnClickListener {
            roomDialog.dismiss()
            var finalTotal = (dormCount * dormPrice) + (privateCount * privatePrice)
            var discountApplied = false

            val paymentView = LayoutInflater.from(this).inflate(R.layout.dialog_payment, null)
            val tvPaymentPropName = paymentView.findViewById<TextView>(R.id.tvPaymentPropName)
            val btnConfirmPayment = paymentView.findViewById<Button>(R.id.btnConfirmPayment)
            val etPromo = paymentView.findViewById<EditText>(R.id.etPromoCode)
            val btnApply = paymentView.findViewById<Button>(R.id.btnApplyPromo)
            val rgPayment = paymentView.findViewById<RadioGroup>(R.id.rgPaymentMethod)

            tvPaymentPropName.text = "Booking: $propName\nTotal: $$finalTotal"

            btnApply.setOnClickListener {
                val code = etPromo.text.toString().trim().uppercase()
                if (!discountApplied && code == "SUMMER20") {
                    finalTotal *= 0.8
                    tvPaymentPropName.text = "Booking: $propName\nTotal: $$finalTotal (20% OFF Applied!)"
                    tvPaymentPropName.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                    discountApplied = true
                }
            }

            val paymentDialog = AlertDialog.Builder(this).setView(paymentView).create()

            btnConfirmPayment.setOnClickListener {
                paymentDialog.dismiss()
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

                val selectedPaymentId = rgPayment.checkedRadioButtonId
                val rbSelected = paymentView.findViewById<RadioButton>(selectedPaymentId)
                val paymentModeStr = rbSelected?.text?.toString() ?: "Credit Card"

                if (currentUserId != null) {
                    db.collection("properties").document(propId).get()
                        .addOnSuccessListener { propDoc ->
                            val hostUid = propDoc.getString("hostUid") ?: ""

                            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            val checkInStr = sdf.format(Date(checkInTime))
                            val checkOutStr = sdf.format(Date(checkOutTime))

                            val mockRef = "HW" + (100000..999999).random()
                            val currentTime = System.currentTimeMillis()

                            val bookingData = hashMapOf(
                                "propertyId" to propId,
                                "travelerUid" to currentUserId,
                                "hostUid" to hostUid,
                                "totalCost" to finalTotal,
                                "checkInDate" to checkInStr,
                                "checkOutDate" to checkOutStr,
                                "cancellationPolicy" to cancellationPolicy,
                                "status" to "Confirmed",
                                "bookingRef" to mockRef,
                                "paymentMode" to paymentModeStr,
                                "timestamp" to currentTime
                            )

                            db.collection("bookings").add(bookingData).addOnSuccessListener {

                                val notificationData = hashMapOf(
                                    "travelerUid" to currentUserId,
                                    "title" to "Booking Confirmed! \uD83C\uDF89",
                                    "message" to "Your trip to $propName is confirmed. Ref: $mockRef. An email receipt has been sent.",
                                    "timestamp" to currentTime
                                )
                                db.collection("notifications").add(notificationData)

                                AlertDialog.Builder(this@PropertyDetailActivity)
                                    .setTitle("Booking Confirmed!")
                                    .setMessage("You are set for $propName!\n\nRef: $mockRef\nDates: $checkInStr to $checkOutStr\nPolicy: $cancellationPolicy")
                                    .setPositiveButton("Awesome") { dialog, _ ->
                                        dialog.dismiss()
                                        finish()
                                    }
                                    .setCancelable(false).show()
                            }
                        }
                }
            }
            paymentDialog.show()
        }
        roomDialog.show()
    }

    inner class ReviewAdapter(private val reviews: List<PropertyReview>) : RecyclerView.Adapter<ReviewAdapter.RevViewHolder>() {
        inner class RevViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val rb: RatingBar = view.findViewById(R.id.rbItemReview)
            val tvDate: TextView = view.findViewById(R.id.tvItemReviewDate)
            val tvText: TextView = view.findViewById(R.id.tvItemReviewText)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RevViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
            return RevViewHolder(view)
        }
        override fun onBindViewHolder(holder: RevViewHolder, position: Int) {
            val rev = reviews[position]
            holder.rb.rating = rev.rating.toFloat()
            holder.tvDate.text = "Verified Traveler • ${rev.dateStr}"
            holder.tvText.text = rev.feedback
        }
        override fun getItemCount() = reviews.size
    }
}