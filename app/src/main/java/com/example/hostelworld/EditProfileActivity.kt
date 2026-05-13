package com.example.hostelworld

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val etBio = findViewById<EditText>(R.id.etEditBio)
        val etInterests = findViewById<EditText>(R.id.etEditInterests)
        val btnSave = findViewById<Button>(R.id.btnSaveEdits)

        // 1. Load existing data so the boxes aren't empty when they open the screen
        val prefs = getSharedPreferences("UserProfile", MODE_PRIVATE)
        etBio.setText(prefs.getString("BIO", ""))
        etInterests.setText(prefs.getString("INTERESTS", ""))

        // 2. Save the data when clicked
        btnSave.setOnClickListener {
            val editor = prefs.edit()
            editor.putString("BIO", etBio.text.toString())
            editor.putString("INTERESTS", etInterests.text.toString())
            editor.apply() // This saves it permanently!

            Toast.makeText(this, "Profile Saved!", Toast.LENGTH_SHORT).show()
            finish() // Closes the edit screen and goes back to the main profile
        }
    }
}