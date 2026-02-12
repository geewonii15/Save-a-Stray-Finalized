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

class AdoptionRequestAdapter(
    private val requestList: ArrayList<AdoptionRequest>,
    private val onApproveClick: (AdoptionRequest) -> Unit,
    private val onRejectClick: (AdoptionRequest) -> Unit
) : RecyclerView.Adapter<AdoptionRequestAdapter.RequestViewHolder>() {

    class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCatName: TextView = itemView.findViewById(R.id.tvRequestCatName)
        val tvEmail: TextView = itemView.findViewById(R.id.tvRequestEmail)
        val tvStatus: TextView = itemView.findViewById(R.id.tvRequestStatus)
        val ivImage: ImageView = itemView.findViewById(R.id.ivRequestImage)
        val btnApprove: ImageButton = itemView.findViewById(R.id.btnApprove)
        val btnReject: ImageButton = itemView.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_request, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requestList[position]

        holder.tvCatName.text = request.catName
        holder.tvEmail.text = request.userEmail
        holder.tvStatus.text = request.status

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

        holder.btnApprove.setOnClickListener { onApproveClick(request) }
        holder.btnReject.setOnClickListener { onRejectClick(request) }
    }

    override fun getItemCount(): Int {
        return requestList.size
    }
}