package com.example.saveastray

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class BrowseCatsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CatAdapter
    private lateinit var catList: ArrayList<Cat>
    private lateinit var tempCatList: ArrayList<Cat>
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browse_cats)

        val tvTitle = findViewById<TextView>(R.id.tvBrowseTitle)
        val searchView = findViewById<SearchView>(R.id.searchView)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        recyclerView = findViewById(R.id.rvBrowseCats)
        recyclerView.layoutManager = LinearLayoutManager(this)

        catList = arrayListOf()
        tempCatList = arrayListOf()
        db = FirebaseFirestore.getInstance()

        adapter = CatAdapter(tempCatList,
            { _ -> },
            { _ -> },
            { cat ->
                val intent = Intent(this, CatDetailsActivity::class.java)
                intent.putExtra("CAT_ID", cat.id)
                intent.putExtra("CAT_NAME", cat.name)
                intent.putExtra("CAT_BREED", cat.breed)
                intent.putExtra("CAT_AGE", cat.age)
                intent.putExtra("CAT_DESC", cat.description)
                intent.putExtra("CAT_IMAGE", cat.imageUrl)
                startActivity(intent)
            },
            isUser = true
        )

        recyclerView.adapter = adapter

        val isFiltered = intent.getBooleanExtra("IS_FILTERED", false)

        if (isFiltered) {
            tvTitle.text = "Your Purr-fect Matches"
            searchView.visibility = View.GONE

            val matchedCats = intent.getParcelableArrayListExtra<Cat>("MATCHED_LIST")

            if (matchedCats != null && matchedCats.isNotEmpty()) {
                catList.clear()
                catList.addAll(matchedCats)

                tempCatList.clear()
                tempCatList.addAll(matchedCats)

                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Showing your Purr-fect matches!", Toast.LENGTH_LONG).show()
            } else {
                fetchCats()
            }
        } else {
            tvTitle.text = "Find a Friend"
            searchView.visibility = View.VISIBLE
            fetchCats()
        }

        val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(Color.BLACK)
        searchEditText.setHintTextColor(Color.GRAY)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })
    }

    private fun fetchCats() {
        db.collection("cats")
            .whereEqualTo("status", "Available")
            .get()
            .addOnSuccessListener { documents ->
                catList.clear()
                tempCatList.clear()
                for (document in documents) {
                    val cat = document.toObject(Cat::class.java)
                    cat.id = document.id
                    catList.add(cat)
                    tempCatList.add(cat)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading cats", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterList(text: String?) {
        val searchText = text?.lowercase(Locale.getDefault()) ?: ""
        tempCatList.clear()

        if (searchText.isEmpty()) {
            tempCatList.addAll(catList)
        } else {
            for (cat in catList) {
                if (cat.name.lowercase(Locale.getDefault()).contains(searchText) ||
                    cat.breed.lowercase(Locale.getDefault()).contains(searchText)) {
                    tempCatList.add(cat)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }
}