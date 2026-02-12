package com.example.saveastray

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CatDetailsActivity : AppCompatActivity() {

    private lateinit var btnAdopt: Button
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var currentCatId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cat_details)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        currentCatId = intent.getStringExtra("CAT_ID") ?: ""
        val catName = intent.getStringExtra("CAT_NAME") ?: "Unknown"
        val catBreed = intent.getStringExtra("CAT_BREED") ?: "Mixed Breed"
        val rawCatAge = intent.getStringExtra("CAT_AGE") ?: ""
        val catDesc = intent.getStringExtra("CAT_DESC") ?: "No description."
        val catImageBase64 = intent.getStringExtra("CAT_IMAGE") ?: ""

        val isAdmin = intent.getBooleanExtra("IS_ADMIN", false)

        val ivImage = findViewById<ImageView>(R.id.ivDetailImage)
        val tvName = findViewById<TextView>(R.id.tvDetailName)
        val tvBreedAge = findViewById<TextView>(R.id.tvDetailBreed)
        val tvDesc = findViewById<TextView>(R.id.tvDetailDesc)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnAdopt = findViewById<Button>(R.id.btnAdoptMe)

        val ageNumber = rawCatAge.filter { it.isDigit() }

        val finalAge = if (ageNumber.isNotEmpty()) {
            "$ageNumber Years Old"
        } else {
            "Unknown Age"
        }

        tvName.text = catName
        tvBreedAge.text = "$catBreed • $finalAge"
        tvDesc.text = catDesc

        if (catImageBase64.isNotEmpty()) {
            try {
                val imageBytes = Base64.decode(catImageBase64, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                ivImage.setImageBitmap(decodedImage)
            } catch (e: Exception) {
                ivImage.setImageResource(R.drawable.img_no_preview)
            }
        }

        btnBack.setOnClickListener { finish() }

        if (isAdmin) {
            btnAdopt.visibility = View.GONE
        } else {
            btnAdopt.visibility = View.VISIBLE
            btnAdopt.setOnClickListener {
                sendAdoptionRequest(currentCatId, catName, catImageBase64)
            }
            checkIfAlreadyApplied()
        }
    }

    private fun checkIfAlreadyApplied() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("adoption_requests")
            .whereEqualTo("userId", userId)
            .whereEqualTo("catId", currentCatId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    disableButton()
                }
            }
    }

    private fun disableButton() {
        btnAdopt.text = "Application Pending"
        btnAdopt.isEnabled = false
        btnAdopt.setBackgroundColor(Color.GRAY)
    }

    private fun sendAdoptionRequest(catId: String, name: String, imageUrl: String) {
        val userId = auth.currentUser?.uid ?: return
        val userEmail = auth.currentUser?.email ?: ""

        val request = hashMapOf(
            "catId" to catId,
            "catName" to name,
            "catImageUrl" to imageUrl,
            "userId" to userId,
            "userEmail" to userEmail,
            "status" to "Pending",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("adoption_requests").add(request)
            .addOnSuccessListener {
                Toast.makeText(this, "Application Sent!", Toast.LENGTH_SHORT).show()
                disableButton()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error sending request", Toast.LENGTH_SHORT).show()
            }
    }
}