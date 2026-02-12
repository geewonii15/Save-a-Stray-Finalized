package com.example.saveastray

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CatAdapter(
    private val catList: ArrayList<Cat>,
    private val onDeleteClick: (Cat) -> Unit,
    private val onEditClick: (Cat) -> Unit,
    private val onItemClick: (Cat) -> Unit,
    private val isUser: Boolean = false
) : RecyclerView.Adapter<CatAdapter.CatViewHolder>() {

    class CatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvCatName)
        val tvBreed: TextView = itemView.findViewById(R.id.tvCatBreed)
        val ivImage: ImageView = itemView.findViewById(R.id.ivCatImage)
        val btnDelete: ImageButton? = itemView.findViewById(R.id.btnDelete)
        val btnEdit: ImageButton? = itemView.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cat, parent, false)
        return CatViewHolder(view)
    }

    override fun onBindViewHolder(holder: CatViewHolder, position: Int) {
        val cat = catList[position]

        holder.tvName.text = cat.name

        val ageNumber = cat.age.filter { it.isDigit() }

        val formattedAge = if (ageNumber.isNotEmpty()) {
            "$ageNumber Years Old"
        } else {
            cat.age
        }

        holder.tvBreed.text = "${cat.breed} • $formattedAge"

        if (cat.imageUrl.isNotEmpty()) {
            try {
                val imageBytes = Base64.decode(cat.imageUrl, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.ivImage.setImageBitmap(decodedImage)
            } catch (e: Exception) {
                holder.ivImage.setImageResource(R.drawable.img_no_preview)
            }
        } else {
            holder.ivImage.setImageResource(R.drawable.img_no_preview)
        }

        if (isUser) {
            holder.btnDelete?.visibility = View.GONE
            holder.btnEdit?.visibility = View.GONE
        } else {
            holder.btnDelete?.visibility = View.VISIBLE
            holder.btnEdit?.visibility = View.VISIBLE

            holder.btnDelete?.setOnClickListener { onDeleteClick(cat) }
            holder.btnEdit?.setOnClickListener { onEditClick(cat) }
        }

        holder.itemView.setOnClickListener { onItemClick(cat) }
    }

    override fun getItemCount(): Int {
        return catList.size
    }
}