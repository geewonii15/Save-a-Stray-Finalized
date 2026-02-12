package com.example.saveastray

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private var tapCount = 0
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val runnable = Runnable {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        handler.postDelayed(runnable, 3000)

        val logo = findViewById<ImageView>(R.id.ivSplashLogo)
        logo.setOnClickListener {
            tapCount++

            if (tapCount == 7) {
                handler.removeCallbacks(runnable)

                Toast.makeText(this, "Secret Admin Mode Activated!", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, AdminLoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}