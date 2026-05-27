package com.example.hostelworld

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class AdminUser(val uid: String, val name: String, val email: String, val role: String, var status: String)

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var rvUsers: RecyclerView
    private lateinit var adapter: AdminUserAdapter
    private val userList = mutableListOf<AdminUser>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        db = FirebaseFirestore.getInstance()

        findViewById<TextView>(R.id.tvAdminLogout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this, "Admin Logged Out", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        rvUsers = findViewById(R.id.rvAdminUsers)
        rvUsers.layoutManager = LinearLayoutManager(this)
        adapter = AdminUserAdapter(userList)
        rvUsers.adapter = adapter

        fetchUsers()
    }

    private fun fetchUsers() {
        db.collection("users").get().addOnSuccessListener { docs ->
            userList.clear()
            for (doc in docs) {
                val uid = doc.id
                val role = doc.getString("role") ?: "TRAVELER"

                if (role == "ADMIN") continue

                val name = doc.getString("name") ?: "Unknown"
                val email = doc.getString("email") ?: "No Email"
                val status = doc.getString("status") ?: "Active"

                userList.add(AdminUser(uid, name, email, role, status))
            }
            adapter.notifyDataSetChanged()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show()
        }
    }

    inner class AdminUserAdapter(private val users: MutableList<AdminUser>) : RecyclerView.Adapter<AdminUserAdapter.UserViewHolder>() {

        inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvAdminUserName)
            val tvEmail: TextView = view.findViewById(R.id.tvAdminUserEmail)
            val tvRole: TextView = view.findViewById(R.id.tvAdminUserRole)
            val tvStatus: TextView = view.findViewById(R.id.tvAdminUserStatus)
            val btnToggle: Button = view.findViewById(R.id.btnAdminToggleStatus)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_user, parent, false)
            return UserViewHolder(view)
        }

        override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
            val user = users[position]
            holder.tvName.text = user.name
            holder.tvEmail.text = user.email
            holder.tvRole.text = "Role: ${user.role}"

            holder.tvStatus.text = "Status: ${user.status}"
            if (user.status == "Active") {
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"))
                holder.btnToggle.text = "Deactivate (Set IA)"
                holder.btnToggle.setBackgroundColor(Color.parseColor("#D32F2F"))
            } else {
                holder.tvStatus.setTextColor(Color.parseColor("#D32F2F"))
                holder.btnToggle.text = "Activate (Set A)"
                holder.btnToggle.setBackgroundColor(Color.parseColor("#4CAF50"))
            }

            holder.btnToggle.setOnClickListener {
                val newStatus = if (user.status == "Active") "Inactive" else "Active"

                db.collection("users").document(user.uid).update("status", newStatus)
                    .addOnSuccessListener {
                        user.status = newStatus
                        notifyItemChanged(position)
                        Toast.makeText(holder.itemView.context, "${user.name} is now $newStatus", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(holder.itemView.context, "Failed to update: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        override fun getItemCount() = users.size
    }
}