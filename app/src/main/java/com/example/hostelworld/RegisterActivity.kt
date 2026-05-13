package com.example.hostelworld

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. WAKE UP FIREBASE FIRST!
        com.google.firebase.FirebaseApp.initializeApp(this)

        // 2. Then load the screen
        setContentView(R.layout.activity_login)

        // 3. Then get the instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        setContentView(R.layout.activity_register)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etName = findViewById<EditText>(R.id.etRegName)
        val etEmail = findViewById<EditText>(R.id.etRegEmail)
        val etPassword = findViewById<EditText>(R.id.etRegPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        // NOTE: Check your XML to make sure your Host RadioButton has the ID 'rbHost'
        val rbHost = findViewById<RadioButton>(R.id.rbHost)

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val role = if (rbHost.isChecked) "HOST" else "TRAVELER"

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {

                // 1. Create the user in Firebase Auth
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid

                            // 2. Save their Role and Name to Firestore
                            val userMap = hashMapOf(
                                "name" to name,
                                "email" to email,
                                "role" to role
                            )

                            if (userId != null) {
                                db.collection("users").document(userId).set(userMap)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()

                                        // 3. Send them to the correct dashboard!
                                        if (role == "HOST") {
                                            startActivity(Intent(this, HostDashboardActivity::class.java))
                                        } else {
                                            startActivity(Intent(this, TravelerDashboardActivity::class.java))
                                        }
                                        finish()
                                    }
                            }
                        } else {
                            Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}