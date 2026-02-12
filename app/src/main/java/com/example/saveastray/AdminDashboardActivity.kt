package com.example.saveastray

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var tvPendingRequests: TextView
    private lateinit var tvTotalCats: TextView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        tvPendingRequests = findViewById(R.id.tvPendingRequests)
        tvTotalCats = findViewById(R.id.tvTotalCats)

        val cardManageCats = findViewById<MaterialCardView>(R.id.cardManageCats)
        cardManageCats.setOnClickListener {
            val intent = Intent(this, ManageCatsActivity::class.java)
            startActivity(intent)
        }

        val cardRequests = findViewById<MaterialCardView>(R.id.cardRequests)
        cardRequests.setOnClickListener {
            val intent = Intent(this, AdminRequestsActivity::class.java)
            startActivity(intent)
        }

        val cardAdminSettings = findViewById<MaterialCardView>(R.id.cardAdminSettings)
        cardAdminSettings.setOnClickListener {
            val intent = Intent(this, AdminSettingsActivity::class.java)
            startActivity(intent)
        }

        refreshDashboard()
    }

    override fun onResume() {
        super.onResume()
        refreshDashboard()
    }

    private fun refreshDashboard() {
        db.collection("adoption_requests")
            .whereEqualTo("status", "Pending")
            .get()
            .addOnSuccessListener { documents ->
                tvPendingRequests.text = "${documents.size()} Pending Review"
            }

        db.collection("cats")
            .get()
            .addOnSuccessListener { documents ->
                tvTotalCats.text = "Total Active: ${documents.size()}"
            }
    }
}