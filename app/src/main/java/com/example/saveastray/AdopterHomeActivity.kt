package com.example.saveastray

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdopterHomeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adopter_home)

        val cardQuiz = findViewById<MaterialCardView>(R.id.cardQuiz)
        cardQuiz.setOnClickListener {
            startActivity(Intent(this, QuizActivity::class.java))
        }

        val cardBrowse = findViewById<MaterialCardView>(R.id.cardBrowse)
        cardBrowse.setOnClickListener {
            startActivity(Intent(this, BrowseCatsActivity::class.java))
        }

        val cardMyApps = findViewById<MaterialCardView>(R.id.cardMyApps)
        cardMyApps.setOnClickListener {
            startActivity(Intent(this, UserApplicationsActivity::class.java))
        }

        val cardProfile = findViewById<MaterialCardView>(R.id.cardProfile)
        cardProfile.setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java))
        }

        val tvWelcome = findViewById<TextView>(R.id.tvWelcomeUser)
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("firstName") ?: "Adopter"
                        tvWelcome.text = "Welcome, $name!"
                    }
                }
        }
    }
}