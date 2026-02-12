package com.example.saveastray

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AdminRequestsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdoptionRequestAdapter
    private lateinit var requestList: ArrayList<AdoptionRequest>
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_requests)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        recyclerView = findViewById(R.id.rvRequests)
        recyclerView.layoutManager = LinearLayoutManager(this)

        requestList = arrayListOf()
        db = FirebaseFirestore.getInstance()

        adapter = AdoptionRequestAdapter(requestList,
            onApproveClick = { request -> confirmAction(request, "Approve") },
            onRejectClick = { request -> confirmAction(request, "Reject") }
        )

        recyclerView.adapter = adapter
        fetchRequests()
    }

    private fun fetchRequests() {
        db.collection("adoption_requests")
            .whereEqualTo("status", "Pending")
            .get()
            .addOnSuccessListener { documents ->
                requestList.clear()
                for (document in documents) {
                    val req = document.toObject(AdoptionRequest::class.java)
                    req.id = document.id
                    requestList.add(req)
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun confirmAction(request: AdoptionRequest, action: String) {
        AlertDialog.Builder(this)
            .setTitle("$action Request?")
            .setMessage("Are you sure you want to $action the request for ${request.catName}?")
            .setPositiveButton("Yes") { _, _ ->
                if (action == "Approve") approveRequest(request)
                else rejectRequest(request)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun approveRequest(request: AdoptionRequest) {
        db.collection("adoption_requests").document(request.id)
            .update("status", "Approved")
            .addOnSuccessListener {
                db.collection("cats").document(request.catId)
                    .update("status", "Adopted")
                    .addOnSuccessListener {
                        Toast.makeText(this, "Approved!", Toast.LENGTH_SHORT).show()
                        fetchRequests()
                    }
            }
    }

    private fun rejectRequest(request: AdoptionRequest) {
        db.collection("adoption_requests").document(request.id)
            .update("status", "Rejected")
            .addOnSuccessListener {
                Toast.makeText(this, "Rejected", Toast.LENGTH_SHORT).show()
                fetchRequests()
            }
    }
}