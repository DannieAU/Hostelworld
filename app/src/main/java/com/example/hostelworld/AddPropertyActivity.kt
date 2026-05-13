package com.example.hostelworld

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddPropertyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_property)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarAddProperty)
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_revert)
        toolbar.setNavigationOnClickListener { finish() }

        // Mock Photo Upload click
        val ivUpload = findViewById<ImageView>(R.id.ivUploadPhoto)
        ivUpload.setOnClickListener {
            Toast.makeText(this, "Opening gallery to upload photo...", Toast.LENGTH_SHORT).show()
        }

        val btnSave = findViewById<Button>(R.id.btnSaveListing)
        btnSave.setOnClickListener {
            val name = findViewById<android.widget.EditText>(R.id.etPropName).text.toString()
            val location = findViewById<android.widget.EditText>(R.id.etPropLocation).text.toString()
            val price = findViewById<android.widget.EditText>(R.id.etPropPrice).text.toString()
            val beds = findViewById<android.widget.EditText>(R.id.etAvailableBeds).text.toString()

            if (name.isNotEmpty()) {
                val randomImage = listOf(R.drawable.room_1, R.drawable.room_2, R.drawable.room_3, R.drawable.room_4, R.drawable.room_5).random()
                HostManager.myListings.add(HostManager.HostProperty(name, location, price, beds, randomImage))

                android.widget.Toast.makeText(this, "Listing Published Successfully!", android.widget.Toast.LENGTH_SHORT).show()
                finish()
            } else {
                android.widget.Toast.makeText(this, "Please enter a Property Name", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}