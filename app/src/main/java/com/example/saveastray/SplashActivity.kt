package com.example.saveastray

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashActivity : AppCompatActivity() {

    private var tapCount = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val runnable = Runnable {
            // If Secret Admin Mode was triggered, stop this runnable
            if (tapCount >= 7) return@Runnable

            val currentUser = auth.currentUser
            if (currentUser != null) {
                // User is logged in -> Check Role & Redirect
                checkRoleAndRedirect(currentUser.uid)
            } else {
                // No user -> Go to Login Screen
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        // Wait 3 seconds
        handler.postDelayed(runnable, 3000)

        // Secret Admin Mode Logic
        val logo = findViewById<ImageView>(R.id.ivSplashLogo)
        logo.setOnClickListener {
            tapCount++
            if (tapCount == 7) {
                handler.removeCallbacks(runnable) // Cancel the auto-redirect
                Toast.makeText(this, "Secret Admin Mode Activated!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, AdminLoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun checkRoleAndRedirect(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role")
                    if (role == "admin") {
                        startActivity(Intent(this, AdminDashboardActivity::class.java))
                    } else {
                        startActivity(Intent(this, AdopterHomeActivity::class.java))
                    }
                } else {
                    // Fallback: If no data, go to Adopter Home
                    startActivity(Intent(this, AdopterHomeActivity::class.java))
                }
                finish() // Close Splash
            }
            .addOnFailureListener {
                // If offline or error, safe fallback to Login
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
    }
}