package com.example.hostelworld

import android.content.Intent
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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ReportBooking(
    val ref: String,
    val propName: String,
    val dates: String,
    val paymentMode: String,
    val total: Double,
    val timestamp: Long
)

class HostReportsActivity : AppCompatActivity() {

    private val allBookings = mutableListOf<ReportBooking>()
    private var displayedBookings = mutableListOf<ReportBooking>()
    private lateinit var adapter: ReportAdapter
    private lateinit var tvTotalRevenue: TextView
    private lateinit var tvReportDates: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host_reports)

        tvTotalRevenue = findViewById(R.id.tvTotalRevenue)
        tvReportDates = findViewById(R.id.tvReportDates)

        val rvReports = findViewById<RecyclerView>(R.id.rvReports)
        rvReports.layoutManager = LinearLayoutManager(this)
        adapter = ReportAdapter(displayedBookings)
        rvReports.adapter = adapter

        fetchReportData()

        // Filter Reports by Date Ranges
        findViewById<Button>(R.id.btnFilterDates).setOnClickListener {
            val builder = MaterialDatePicker.Builder.dateRangePicker()
            builder.setTitleText("Select Booking Range")
            val picker = builder.build()

            picker.addOnPositiveButtonClickListener { selection ->
                val start = selection.first ?: 0L
                val end = (selection.second ?: 0L) + 86400000 // Add 1 day to encapsulate the entire end date

                // Filter the master list
                val filtered = allBookings.filter { it.timestamp in start..end }

                val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
                tvReportDates.text = "${sdf.format(Date(start))} - ${sdf.format(Date(selection.second ?: 0L))}"

                updateListAndTotal(filtered)
            }
            picker.show(supportFragmentManager, "REPORT_DATE_PICKER")
        }

        // --- NEW: BOTTOM NAVIGATION LOGIC ---
        val bottomNavHost = findViewById<BottomNavigationView>(R.id.bottomNavHost)
        bottomNavHost.selectedItemId = R.id.nav_host_reports

        bottomNavHost.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_host_dashboard -> {
                    startActivity(Intent(this, HostDashboardActivity::class.java))
                    finish()
                }
                R.id.nav_host_listings -> {
                    startActivity(Intent(this, HostListingsActivity::class.java))
                    finish()
                }
                R.id.nav_host_reports -> { } // Already here!
                R.id.nav_host_profile -> {
                    val intent = Intent(this, HostProfileActivity::class.java)
                    intent.putExtra("USER_NAME", this.intent.getStringExtra("USER_NAME"))
                    intent.putExtra("USER_EMAIL", this.intent.getStringExtra("USER_EMAIL"))
                    startActivity(intent)
                    finish()
                }
            }
            true
        }
    }

    private fun fetchReportData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("bookings")
            .whereEqualTo("hostUid", uid)
            .get()
            .addOnSuccessListener { docs ->
                allBookings.clear()
                for (doc in docs) {
                    val ref = doc.getString("bookingRef") ?: doc.id
                    val checkIn = doc.getString("checkInDate") ?: ""
                    val checkOut = doc.getString("checkOutDate") ?: ""
                    val payment = doc.getString("paymentMode") ?: "Unknown"
                    val cost = doc.getDouble("totalCost") ?: 0.0
                    val time = doc.getLong("timestamp") ?: 0L

                    val propId = doc.getString("propertyId") ?: ""

                    if (propId.isNotEmpty()) {
                        FirebaseFirestore.getInstance().collection("properties").document(propId).get()
                            .addOnSuccessListener { propDoc ->
                                val propName = propDoc.getString("name") ?: "Deleted Property"
                                val item = ReportBooking(ref, propName, "$checkIn to $checkOut", payment, cost, time)
                                allBookings.add(item)

                                updateListAndTotal(allBookings)
                            }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load reports.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateListAndTotal(list: List<ReportBooking>) {
        displayedBookings.clear()
        displayedBookings.addAll(list.sortedByDescending { it.timestamp })
        adapter.notifyDataSetChanged()

        val sum = displayedBookings.sumOf { it.total }
        tvTotalRevenue.text = "Total Revenue: $$sum"
    }

    inner class ReportAdapter(private val items: List<ReportBooking>) : RecyclerView.Adapter<ReportAdapter.RepViewHolder>() {
        inner class RepViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvRepPropName)
            val tvRef: TextView = view.findViewById(R.id.tvRepRef)
            val tvDates: TextView = view.findViewById(R.id.tvRepDates)
            val tvPayment: TextView = view.findViewById(R.id.tvRepPayment)
            val tvTotal: TextView = view.findViewById(R.id.tvRepTotal)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_report_booking, parent, false)
            return RepViewHolder(view)
        }
        override fun onBindViewHolder(holder: RepViewHolder, position: Int) {
            val item = items[position]
            holder.tvName.text = item.propName
            holder.tvRef.text = "Ref: ${item.ref}"
            holder.tvDates.text = "Stay: ${item.dates}"
            holder.tvPayment.text = "Paid via: ${item.paymentMode}"
            holder.tvTotal.text = "Total: $${item.total}"
        }
        override fun getItemCount() = items.size
    }
}