package com.example.saveastray

import android.content.Intent
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
import androidx.appcompat.app.AlertDialog // Import for the Popup
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CatDetailsActivity : AppCompatActivity() {

    private lateinit var btnAdopt: Button
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var currentCatId: String = ""

    private var currentCatName: String = ""
    private var currentCatImage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cat_details)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        currentCatId = intent.getStringExtra("CAT_ID") ?: ""
        currentCatName = intent.getStringExtra("CAT_NAME") ?: "Unknown"
        val catBreed = intent.getStringExtra("CAT_BREED") ?: "Mixed Breed"
        val rawCatAge = intent.getStringExtra("CAT_AGE") ?: ""
        val catDesc = intent.getStringExtra("CAT_DESC") ?: "No description."
        currentCatImage = intent.getStringExtra("CAT_IMAGE") ?: ""

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

        tvName.text = currentCatName
        tvBreedAge.text = "$catBreed • $finalAge"
        tvDesc.text = catDesc

        if (currentCatImage.isNotEmpty()) {
            try {
                val imageBytes = Base64.decode(currentCatImage, Base64.DEFAULT)
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

            checkIfAlreadyApplied()

            btnAdopt.setOnClickListener {
                showAdoptionInfoDialog()
            }
        }
    }

    private fun showAdoptionInfoDialog() {
        AlertDialog.Builder(this)
            .setTitle("Adoption Process")
            .setMessage("Thank you for choosing to adopt!\n\n" +
                    "To ensure the safety and well-being of our cats, all adopters are required to attend an in-person interview.\n\n" +
                    "Please visit the shelter during our opening hours to proceed with the adoption process.\n\n" +
                    "Our team will guide you through the next steps.")
            .setPositiveButton("Understood") { dialog, _ ->
                sendAdoptionRequest(currentCatId, currentCatName, currentCatImage)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
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