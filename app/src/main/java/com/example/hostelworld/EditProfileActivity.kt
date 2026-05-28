package com.example.hostelworld

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        prefs = getSharedPreferences("UserProfile", MODE_PRIVATE)

        val etName = findViewById<EditText>(R.id.etEditName)
        val etBio = findViewById<EditText>(R.id.etEditBio)
        val etInterests = findViewById<EditText>(R.id.etEditInterests)
        val btnSave = findViewById<Button>(R.id.btnSaveChanges)

        // 1. Load Bio and Interests from local storage
        etBio.setText(prefs.getString("BIO", ""))
        etInterests.setText(prefs.getString("INTERESTS", ""))

        // 2. Load Name directly from Firestore database
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    etName.setText(doc.getString("name") ?: "")
                }
        }

        // 3. Handle Save Button
        btnSave.setOnClickListener {
            val newName = etName.text.toString().trim()
            val newBio = etBio.text.toString().trim()
            val newInterests = etInterests.text.toString().trim()

            if (newName.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save Bio and Interests locally
            prefs.edit().apply {
                putString("BIO", newBio)
                putString("INTERESTS", newInterests)
                apply()
            }

            // Save new Name to Firestore
            if (uid != null) {
                db.collection("users").document(uid).update("name", newName)
                    .addOnSuccessListener {
                        // Also save name locally so ProfileActivity updates instantly without reloading
                        prefs.edit().putString("NAME", newName).apply()

                        Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show()
                        finish() // Close screen and go back
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to update name: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}