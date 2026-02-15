package com.example.saveastray

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore // Import Firestore

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore // Declare Firestore

    private var logoTapCount = 0
    private var lastTapTime: Long = 0

    public override fun onStart() {
        super.onStart()
        // Auto-redirect logic is REMOVED here.
        // SplashActivity now handles the routing before this screen even appears.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance() // Ensure initialized here too

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)
        val ivLogo = findViewById<ImageView>(R.id.ivLogo)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Disable button to prevent double clicks
            btnLogin.isEnabled = false
            btnLogin.text = "Loading..."

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // FIX: Check role here as well for manual logins
                        checkUserRoleAndRedirect(auth.currentUser?.uid)
                    } else {
                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        btnLogin.isEnabled = true
                        btnLogin.text = "Log In"
                    }
                }
        }

        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        ivLogo.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastTapTime > 1000) {
                logoTapCount = 0
            }
            logoTapCount++
            lastTapTime = currentTime

            if (logoTapCount == 7) {
                Toast.makeText(this, "🕵️ Secret Admin Mode Activated!", Toast.LENGTH_LONG).show()
                logoTapCount = 0
                // Optional: You could redirect to AdminLoginActivity here immediately
                val intent = Intent(this, AdminLoginActivity::class.java)
                startActivity(intent)
            }
        }
    }

    // NEW HELPER FUNCTION
    private fun checkUserRoleAndRedirect(uid: String?) {
        if (uid == null) return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role")
                    if (role == "admin") {
                        goToAdminDashboard()
                    } else {
                        goToAdopterHome()
                    }
                } else {
                    // Fallback if no user doc exists
                    goToAdopterHome()
                }
            }
            .addOnFailureListener {
                // Handle offline or error cases - usually safe to default to Adopter or stay on login
                Toast.makeText(this, "Error checking role. Please try again.", Toast.LENGTH_SHORT).show()
                findViewById<Button>(R.id.btnLogin).isEnabled = true
                findViewById<Button>(R.id.btnLogin).text = "Log In"
            }
    }

    private fun goToAdopterHome() {
        val intent = Intent(this, AdopterHomeActivity::class.java)
        // Clear back stack so they can't go back to login
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun goToAdminDashboard() {
        // Redirect to Admin Dashboard
        val intent = Intent(this, AdminDashboardActivity::class.java)
        // Clear back stack so they can't go back to login
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}