package com.example.saveastray

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserApplicationsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ApplicationStatusAdapter
    private lateinit var requestList: ArrayList<AdoptionRequest>
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_applications)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        recyclerView = findViewById(R.id.rvApplications)
        recyclerView.layoutManager = LinearLayoutManager(this)

        requestList = arrayListOf()
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        adapter = ApplicationStatusAdapter(requestList)
        recyclerView.adapter = adapter

        fetchMyApplications()
    }

    private fun fetchMyApplications() {
        val currentUser = auth.currentUser
        if (currentUser == null) return

        db.collection("adoption_requests")
            .whereEqualTo("userId", currentUser.uid)
            .addSnapshotListener { documents, e ->
                if (e != null) {
                    Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (documents != null) {
                    requestList.clear()
                    for (document in documents) {
                        val req = document.toObject(AdoptionRequest::class.java)
                        req.id = document.id
                        requestList.add(req)
                    }
                    adapter.notifyDataSetChanged()

                    if (requestList.isEmpty()) {
                        Toast.makeText(this, "No applications yet.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
}