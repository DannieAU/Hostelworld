package com.example.hostelworld

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class AddPropertyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_property)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarAddProperty)
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert)
        toolbar.setNavigationOnClickListener { finish() }

        val ivUpload = findViewById<ImageView>(R.id.ivUploadPhoto)
        ivUpload.setOnClickListener {
            Toast.makeText(this, "Opening gallery to upload photo...", Toast.LENGTH_SHORT).show()
        }

        val etName = findViewById<EditText>(R.id.etPropName)
        val etLocation = findViewById<EditText>(R.id.etPropLocation)
        val etPrice = findViewById<EditText>(R.id.etPropPrice)
        val etBeds = findViewById<EditText>(R.id.etAvailableBeds)
        val btnSave = findViewById<Button>(R.id.btnSaveListing)

        val swCancelPolicy = findViewById<SwitchCompat>(R.id.swCancelPolicy)

        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()

        val editPropId = intent.getStringExtra("PROPERTY_ID")

        if (editPropId != null) {
            db.collection("properties").document(editPropId).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        etName.setText(doc.getString("name"))
                        etLocation.setText(doc.getString("location"))
                        etPrice.setText(doc.getDouble("pricePerNight")?.toString())
                        etBeds.setText(doc.getDouble("availableBeds")?.toInt().toString())

                        val policy = doc.getString("cancellationPolicy")
                        swCancelPolicy.isChecked = (policy == "Flexible (Free Cancellation)")

                        btnSave.text = "Update Listing"
                    }
                }
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString()
            val location = etLocation.text.toString()
            val price = etPrice.text.toString().toDoubleOrNull() ?: 0.0
            val beds = etBeds.text.toString().toIntOrNull() ?: 0
            val hostUid = auth.currentUser?.uid

            val policyStr = if (swCancelPolicy.isChecked) "Flexible (Free Cancellation)" else "Non-Refundable"

            if (name.isNotEmpty() && hostUid != null) {

                val propertyData = hashMapOf(
                    "hostUid" to hostUid,
                    "name" to name,
                    "location" to location,
                    "pricePerNight" to price,
                    "availableBeds" to beds,
                    "cancellationPolicy" to policyStr,
                    "imageUrl" to "default_image"
                )

                if (editPropId != null) {
                    db.collection("properties").document(editPropId)
                        .set(propertyData, SetOptions.merge())
                        .addOnSuccessListener {
                            Toast.makeText(this, "Listing Updated!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                } else {
                    db.collection("properties").add(propertyData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Listing Published!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                }
            } else {
                Toast.makeText(this, "Please enter all details.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}