package com.example.hostelworld

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etName = findViewById<EditText>(R.id.etRegisterName)
        val etEmail = findViewById<EditText>(R.id.etRegisterEmail)
        val etPassword = findViewById<EditText>(R.id.etRegisterPassword)
        val rgRole = findViewById<RadioGroup>(R.id.rgRole)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Determine if they selected Host or Traveler
            val role = if (rgRole.checkedRadioButtonId == R.id.rbHost) "HOST" else "TRAVELER"

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val uid = auth.currentUser?.uid ?: return@addOnSuccessListener

                    val userData = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "role" to role,
                        "status" to "Active" // New accounts are Active by default!
                    )

                    db.collection("users").document(uid).set(userData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()

                            // Route them to the correct dashboard right after signing up
                            if (role == "HOST") {
                                val intent = Intent(this, HostDashboardActivity::class.java)
                                intent.putExtra("USER_NAME", name)
                                intent.putExtra("USER_EMAIL", email)
                                startActivity(intent)
                            } else {
                                val intent = Intent(this, TravelerDashboardActivity::class.java)
                                intent.putExtra("USER_NAME", name)
                                intent.putExtra("USER_EMAIL", email)
                                startActivity(intent)
                            }
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Registration Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        tvLogin.setOnClickListener {
            finish() // Closes the register screen, instantly taking them back to the Login screen
        }
    }
}