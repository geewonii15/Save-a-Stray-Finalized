package com.example.saveastray

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminSettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: AdminUserRequestAdapter
    private val pendingAdmins = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_settings)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val rvRequests = findViewById<RecyclerView>(R.id.rvAdminRequests)
        rvRequests.layoutManager = LinearLayoutManager(this)

        adapter = AdminUserRequestAdapter(pendingAdmins,
            onApprove = { user -> approveAdmin(user) },
            onDeny = { user -> denyAdmin(user) }
        )
        rvRequests.adapter = adapter

        findViewById<Button>(R.id.btnAdminChangePassword).setOnClickListener {
            val email = auth.currentUser?.email ?: return@setOnClickListener
            AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage("Send reset link to $email?")
                .setPositiveButton("Send") { _, _ ->
                    auth.sendPasswordResetEmail(email).addOnSuccessListener {
                        Toast.makeText(this, "Email sent!", Toast.LENGTH_SHORT).show()
                    }
                }.show()
        }

        findViewById<Button>(R.id.btnAdminSettingsLogout).setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        fetchPendingAdmins()
    }

    private fun fetchPendingAdmins() {
        db.collection("users")
            .whereEqualTo("role", "admin")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshots, _ ->
                if (snapshots != null) {
                    val list = snapshots.documents.mapNotNull { doc ->
                        doc.toObject(User::class.java)?.apply { uid = doc.id }
                    }
                    adapter.updateList(list)
                }
            }
    }

    private fun approveAdmin(user: User) {
        db.collection("users").document(user.uid)
            .update("status", "approved")
            .addOnSuccessListener {
                Toast.makeText(this, "${user.email} Approved!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun denyAdmin(user: User) {
        AlertDialog.Builder(this)
            .setTitle("Deny Admin")
            .setMessage("Delete this request from ${user.email}?")
            .setPositiveButton("Delete") { _, _ ->
                db.collection("users").document(user.uid).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Request deleted", Toast.LENGTH_SHORT).show()
                    }
            }.show()
    }
}