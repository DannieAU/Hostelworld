package com.example.hostelworld

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etEmail = findViewById<EditText>(R.id.etLoginEmail)
        val etPassword = findViewById<EditText>(R.id.etLoginPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val uid = auth.currentUser?.uid ?: return@addOnSuccessListener

                    db.collection("users").document(uid).get()
                        .addOnSuccessListener { doc ->

                            val status = doc.getString("status") ?: "Active"

                            if (status == "Inactive") {
                                auth.signOut()
                                Toast.makeText(this, "Access Denied: Your account has been deactivated by an Admin.", Toast.LENGTH_LONG).show()
                                return@addOnSuccessListener
                            }

                            val role = doc.getString("role") ?: "TRAVELER"
                            val name = doc.getString("name") ?: "User"

                            // Intelligent Role Routing (Admin will be safely routed here!)
                            when (role) {
                                "ADMIN" -> {
                                    startActivity(Intent(this, AdminDashboardActivity::class.java))
                                }
                                "HOST" -> {
                                    val intent = Intent(this, HostDashboardActivity::class.java)
                                    intent.putExtra("USER_NAME", name)
                                    intent.putExtra("USER_EMAIL", email)
                                    startActivity(intent)
                                }
                                else -> {
                                    val intent = Intent(this, TravelerDashboardActivity::class.java)
                                    intent.putExtra("USER_NAME", name)
                                    intent.putExtra("USER_EMAIL", email)
                                    startActivity(intent)
                                }
                            }
                            finish()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Login Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        tvSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}