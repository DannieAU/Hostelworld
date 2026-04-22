package com.example.hostelworld

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val tvGoToRegister = findViewById<TextView>(R.id.tvGoToRegister)

        val passedName = intent.getStringExtra("USER_NAME") ?: "Guest" // Catch the name

        // Retrieve data passed from RegisterActivity (if any)
        val passedEmail = intent.getStringExtra("REGISTERED_EMAIL")

        // Grab the role, default to "TRAVELER" if no role was passed
        val passedRole = intent.getStringExtra("USER_ROLE") ?: "TRAVELER"

        // Pre-fill the email field if they just registered
        if (!passedEmail.isNullOrEmpty()) {
            etEmail.setText(passedEmail)
        }

        // Handle Login Click
        btnLogin.setOnClickListener {
            // Grab the text from the inputs
            val email = etEmail.text.toString().trim()
            val etPassword = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etPassword)
            val password = etPassword.text.toString().trim()

            // --- VALIDATION CHECKS ---

            // 1. Check if Email is empty
            if (email.isEmpty()) {
                etEmail.error = "Email address is required"
                etEmail.requestFocus()
                return@setOnClickListener // This STOPS the code so it doesn't log in
            }

            // 2. Check if Password is empty
            if (password.isEmpty()) {
                etPassword.error = "Password is required"
                etPassword.requestFocus()
                return@setOnClickListener // This STOPS the code so it doesn't log in
            }

            // --- SUCCESS ---
            // If the code makes it down here, both fields are filled out!
            // Paste your EXISTING intent logic here:

            val intent = if (passedRole == "HOST") {
                Intent(this, HostDashboardActivity::class.java)
            } else {
                Intent(this, TravelerDashboardActivity::class.java)
            }

            intent.putExtra("USER_NAME", passedName)
            intent.putExtra("USER_EMAIL", email)
            intent.putExtra("USER_ROLE", passedRole)
            startActivity(intent)
            finish()
        }

        // Navigate back to Registration
        tvGoToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}