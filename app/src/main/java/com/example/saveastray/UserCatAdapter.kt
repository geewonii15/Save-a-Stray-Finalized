package com.example.saveastray

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserCatAdapter(
    private val catList: ArrayList<Cat>,
    private val onItemClick: (Cat) -> Unit
) : RecyclerView.Adapter<UserCatAdapter.UserCatViewHolder>() {

    class UserCatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvCatName)
        val tvBreed: TextView = itemView.findViewById(R.id.tvCatBreed)
        val ivImage: ImageView = itemView.findViewById(R.id.ivCatImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserCatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cat_user, parent, false)
        return UserCatViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserCatViewHolder, position: Int) {
        val cat = catList[position]

        holder.tvName.text = cat.name
        holder.tvBreed.text = cat.breed

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

        holder.itemView.setOnClickListener {
            onItemClick(cat)
        }
    }

    override fun getItemCount(): Int {
        return catList.size
    }
}