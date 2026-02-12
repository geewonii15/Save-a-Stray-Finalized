package com.example.saveastray

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class ManageCatsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CatAdapter
    private lateinit var catList: ArrayList<Cat>
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_cats)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddCat)
        fabAdd.setOnClickListener {
            val intent = Intent(this, AddCatActivity::class.java)
            startActivity(intent)
        }

        recyclerView = findViewById(R.id.rvCats)
        recyclerView.layoutManager = LinearLayoutManager(this)

        catList = arrayListOf()
        db = FirebaseFirestore.getInstance()

        adapter = CatAdapter(catList,
            onDeleteClick = { cat -> showDeleteConfirmation(cat) },

            onEditClick = { cat ->
                val intent = Intent(this, AddCatActivity::class.java)
                intent.putExtra("catId", cat.id)
                intent.putExtra("name", cat.name)
                intent.putExtra("breed", cat.breed)
                intent.putExtra("age", cat.age)
                intent.putExtra("description", cat.description)
                intent.putExtra("imageUrl", cat.imageUrl)
                startActivity(intent)
            },

            onItemClick = { cat ->
                val intent = Intent(this, CatDetailsActivity::class.java)
                intent.putExtra("CAT_ID", cat.id)
                intent.putExtra("CAT_NAME", cat.name)
                intent.putExtra("CAT_DESC", cat.description)
                intent.putExtra("CAT_IMAGE", cat.imageUrl)

                intent.putExtra("CAT_BREED", cat.breed)
                intent.putExtra("CAT_AGE", cat.age)
                intent.putExtra("IS_ADMIN", true)

                startActivity(intent)
            },
            isUser = false
        )

        recyclerView.adapter = adapter
        fetchCats()
    }

    override fun onResume() {
        super.onResume()
        fetchCats()
    }

    private fun fetchCats() {
        db.collection("cats").get()
            .addOnSuccessListener { documents ->
                catList.clear()
                for (document in documents) {
                    val cat = document.toObject(Cat::class.java)
                    cat.id = document.id
                    catList.add(cat)
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun showDeleteConfirmation(cat: Cat) {
        AlertDialog.Builder(this)
            .setTitle("Delete Cat")
            .setMessage("Are you sure you want to delete ${cat.name}?")
            .setPositiveButton("Delete") { _, _ ->
                db.collection("cats").document(cat.id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
                        fetchCats()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}