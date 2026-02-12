package com.example.saveastray

import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ApplicationStatusAdapter(
    private val requestList: ArrayList<AdoptionRequest>
) : RecyclerView.Adapter<ApplicationStatusAdapter.StatusViewHolder>() {

    class StatusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvCatName)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val ivImage: ImageView = itemView.findViewById(R.id.ivCatImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_application_status, parent, false)
        return StatusViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        val request = requestList[position]

        holder.tvName.text = request.catName
        holder.tvStatus.text = request.status

        when (request.status) {
            "Approved" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"))
                holder.tvStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E8F5E9"))
            }
            "Rejected" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#C62828"))
                holder.tvStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFEBEE"))
            }
            else -> {
                holder.tvStatus.setTextColor(Color.parseColor("#EF6C00"))
                holder.tvStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFF3E0"))
            }
        }

        if (request.catImageUrl.isNotEmpty()) {
            try {
                val imageBytes = Base64.decode(request.catImageUrl, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.ivImage.setImageBitmap(decodedImage)
            } catch (e: Exception) {
                holder.ivImage.setImageResource(R.drawable.img_no_preview)
            }
        } else {
            holder.ivImage.setImageResource(R.drawable.img_no_preview)
        }
    }

    override fun getItemCount(): Int {
        return requestList.size
    }
}