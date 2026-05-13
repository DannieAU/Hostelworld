package com.example.hostelworld

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

// 1. ADDED THESE MISSING IMPORTS!
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    // 2. ADDED THE MISSING VARIABLES HERE!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 3. WAKE UP FIREBASE FIRST!
        FirebaseApp.initializeApp(this)

        // Load the screen
        setContentView(R.layout.activity_login)

        // Initialize Firebase Instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val tvGoToRegister = findViewById<TextView>(R.id.tvGoToRegister)

        // Pre-fill the email field if they just registered
        val passedEmail = intent.getStringExtra("REGISTERED_EMAIL")
        if (!passedEmail.isNullOrEmpty()) {
            etEmail.setText(passedEmail)
        }

        // Handle Login Click
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validation Checks
            if (email.isEmpty()) {
                etEmail.error = "Email address is required"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                etPassword.error = "Password is required"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            // 4. THE ACTUAL FIREBASE LOGIN LOGIC!
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid

                        // Fetch their role from Firestore
                        if (userId != null) {
                            db.collection("users").document(userId).get()
                                .addOnSuccessListener { document ->
                                    if (document != null && document.exists()) {
                                        val role = document.getString("role") ?: "TRAVELER"
                                        val userName = document.getString("name") ?: "Guest"

                                        Toast.makeText(this, "Welcome back, $userName!", Toast.LENGTH_SHORT).show()

                                        // Route them to the correct dashboard based on Firestore data!
                                        val intent = if (role == "HOST") {
                                            Intent(this, HostDashboardActivity::class.java)
                                        } else {
                                            Intent(this, TravelerDashboardActivity::class.java)
                                        }

                                        intent.putExtra("USER_NAME", userName)
                                        intent.putExtra("USER_EMAIL", email)
                                        intent.putExtra("USER_ROLE", role)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(this, "User data not found in database.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    } else {
                        Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // Navigate back to Registration
        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }
}