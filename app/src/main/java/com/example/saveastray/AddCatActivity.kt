package com.example.saveastray

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.InputStream

class AddCatActivity : AppCompatActivity() {

    private lateinit var ivCatPhoto: ImageView
    private lateinit var etName: EditText
    private lateinit var etBreed: EditText
    private lateinit var etAge: EditText
    private lateinit var etDesc: EditText
    private lateinit var btnSave: Button
    private var imageUri: Uri? = null
    private var existingImageString: String = ""
    private var catId: String? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imageUri = uri
            ivCatPhoto.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_cat)

        ivCatPhoto = findViewById(R.id.ivCatPhoto)
        etName = findViewById(R.id.etCatName)
        etBreed = findViewById(R.id.etCatBreed)
        etAge = findViewById(R.id.etCatAge)
        etDesc = findViewById(R.id.etCatDesc)
        btnSave = findViewById(R.id.btnSaveCat)

        ivCatPhoto.setOnClickListener {
            pickImage.launch("image/*")
        }

        catId = intent.getStringExtra("catId")

        if (catId != null) {
            btnSave.text = "Update Cat"

            etName.setText(intent.getStringExtra("name"))
            etBreed.setText(intent.getStringExtra("breed"))
            etAge.setText(intent.getStringExtra("age"))
            etDesc.setText(intent.getStringExtra("description"))

            existingImageString = intent.getStringExtra("imageUrl") ?: ""

            if (existingImageString.isNotEmpty()) {
                try {
                    val imageBytes = Base64.decode(existingImageString, Base64.DEFAULT)
                    val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    ivCatPhoto.setImageBitmap(decodedImage)
                } catch (e: Exception) {
                    ivCatPhoto.setImageResource(R.drawable.img_no_preview)
                }
            }
        }

        btnSave.setOnClickListener {
            saveOrUpdateCat()
        }
    }

    private fun saveOrUpdateCat() {
        val name = etName.text.toString().trim()
        val breed = etBreed.text.toString().trim()
        val age = etAge.text.toString().trim()
        val desc = etDesc.text.toString().trim()

        if (name.isEmpty() || breed.isEmpty()) {
            Toast.makeText(this, "Name and Breed are required", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Saving...", Toast.LENGTH_SHORT).show()

        if (imageUri != null) {
            try {
                val inputStream: InputStream? = contentResolver.openInputStream(imageUri!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
                val byteArray = stream.toByteArray()
                val newImageString = Base64.encodeToString(byteArray, Base64.DEFAULT)

                sendToFirestore(newImageString, name, breed, age, desc)
            } catch (e: Exception) {
                Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show()
            }
        } else {
            sendToFirestore(existingImageString, name, breed, age, desc)
        }
    }

    private fun sendToFirestore(imageString: String, name: String, breed: String, age: String, desc: String) {
        val db = FirebaseFirestore.getInstance()

        val rgEnergy = findViewById<RadioGroup>(R.id.rgEnergy)
        val rgSociability = findViewById<RadioGroup>(R.id.rgSociability)
        val rgPlayfulness = findViewById<RadioGroup>(R.id.rgPlayfulness)
        val rgInteraction = findViewById<RadioGroup>(R.id.rgInteraction)

        val energyLevel = getSelectedValue(rgEnergy)
        val sociability = getSelectedValue(rgSociability)
        val playfulness = getSelectedValue(rgPlayfulness)
        val interactionNeeds = getSelectedValue(rgInteraction)

        val catData = hashMapOf(
            "name" to name,
            "breed" to breed,
            "age" to age,
            "description" to desc,
            "imageUrl" to imageString,
            "status" to "Available",
            "timestamp" to System.currentTimeMillis(),
            "personality_energy" to energyLevel,
            "personality_social" to sociability,
            "personality_play" to playfulness,
            "personality_interaction" to interactionNeeds
        )

        if (catId != null) {
            db.collection("cats").document(catId!!)
                .update(catData as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(this, "Cat Updated!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Update Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            db.collection("cats")
                .add(catData)
                .addOnSuccessListener {
                    Toast.makeText(this, "New Cat Added!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Save Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun getSelectedValue(radioGroup: RadioGroup): Int {
        val selectedId = radioGroup.checkedRadioButtonId
        if (selectedId == -1) return 0

        val radioButton = findViewById<RadioButton>(selectedId)
        val index = radioGroup.indexOfChild(radioButton)

        return index + 1
    }
}