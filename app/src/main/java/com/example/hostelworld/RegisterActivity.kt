package com.example.hostelworld

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etRegName = findViewById<TextInputEditText>(R.id.etRegName)
        val etRegEmail = findViewById<TextInputEditText>(R.id.etRegEmail)
        val btnRegister = findViewById<MaterialButton>(R.id.btnRegister)
        val tvGoToLogin = findViewById<TextView>(R.id.tvGoToLogin)
        val rbHost = findViewById<RadioButton>(R.id.rbHost)


        // Handle Registration Click
        btnRegister.setOnClickListener {
            val email = etRegEmail.text.toString().trim()

            // Determine the selected role
            val selectedRole = if (rbHost.isChecked) "HOST" else "TRAVELER"

            // Route to Login Activity with the data attached
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("REGISTERED_EMAIL", email)
            intent.putExtra("USER_ROLE", selectedRole) // Pass the role to Login
            startActivity(intent)
            finish()
        }

        // Navigate to Login screen normally if they already have an account
        tvGoToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnRegister.setOnClickListener {
            val name = etRegName.text.toString().trim() // Add this line
            val email = etRegEmail.text.toString().trim()

            val selectedRole = if (rbHost.isChecked) "HOST" else "TRAVELER"

            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("USER_NAME", name) // Pass the name
            intent.putExtra("REGISTERED_EMAIL", email)
            intent.putExtra("USER_ROLE", selectedRole)
            startActivity(intent)
            finish()
        }
    }
}