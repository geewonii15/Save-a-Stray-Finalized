package com.example.saveastray

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.util.Locale

class UserProfileActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var ivProfileImage: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvRole: TextView

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                val base64String = bitmapToBase64(bitmap)

                ivProfileImage.setImageBitmap(bitmap)
                ivProfileImage.setPadding(0, 0, 0, 0)
                ivProfileImage.scaleType = ImageView.ScaleType.CENTER_CROP
                ivProfileImage.imageTintList = null

                saveProfileImage(base64String)
            } catch (e: Exception) {
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnLogout = findViewById<Button>(R.id.btnProfileLogout)
        val btnChangePass = findViewById<Button>(R.id.btnChangePassword)
        val cardProfileImage = findViewById<CardView>(R.id.cardProfileImage)

        ivProfileImage = findViewById(R.id.ivProfileImage)
        tvName = findViewById(R.id.tvProfileName)
        tvEmail = findViewById(R.id.tvProfileEmail)
        tvRole = findViewById(R.id.tvRole)

        btnBack.setOnClickListener { finish() }

        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        btnChangePass.setOnClickListener {
            showResetPasswordDialog()
        }

        cardProfileImage.setOnClickListener {
            getContent.launch("image/*")
        }

        fetchUserData()
    }

    private fun fetchUserData() {
        val userId = auth.currentUser?.uid ?: return
        val email = auth.currentUser?.email ?: ""
        tvEmail.text = email

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {

                    var displayName = document.getString("firstName")

                    if (displayName.isNullOrEmpty()) {
                        displayName = email.substringBefore("@")
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    }

                    val lastName = document.getString("lastName") ?: ""
                    if (lastName.isNotEmpty()) {
                        displayName = "$displayName $lastName"
                    }

                    tvName.text = displayName

                    val rawRole = document.getString("role") ?: "Adopter"
                    tvRole.text = rawRole.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

                    val imageString = document.getString("profileImage")
                    if (!imageString.isNullOrEmpty()) {
                        try {
                            val decodedByte = Base64.decode(imageString, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)

                            ivProfileImage.setImageBitmap(bitmap)
                            ivProfileImage.setPadding(0, 0, 0, 0)
                            ivProfileImage.scaleType = ImageView.ScaleType.CENTER_CROP
                            ivProfileImage.imageTintList = null

                        } catch (e: Exception) {
                        }
                    }
                }
            }
    }

    private fun saveProfileImage(base64String: String) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .update("profileImage", base64String)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save image.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showResetPasswordDialog() {
        val email = auth.currentUser?.email ?: return

        AlertDialog.Builder(this)
            .setTitle("Reset Password")
            .setMessage("Send a password reset link to $email?")
            .setPositiveButton("Send") { _, _ ->
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Reset email sent! Check your inbox.", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val resized = Bitmap.createScaledBitmap(bitmap, 300, 300, true)
        val byteArrayOutputStream = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}