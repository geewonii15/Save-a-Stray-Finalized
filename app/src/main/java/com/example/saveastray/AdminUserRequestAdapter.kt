package com.example.saveastray

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdminUserRequestAdapter(
    private var adminList: MutableList<User>,
    private val onApprove: (User) -> Unit,
    private val onDeny: (User) -> Unit
) : RecyclerView.Adapter<AdminUserRequestAdapter.AdminViewHolder>() {

    class AdminViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvAdminName)
        val tvEmail: TextView = itemView.findViewById(R.id.tvAdminEmail)
        val btnApprove: ImageButton = itemView.findViewById(R.id.btnApproveAdmin)
        val btnDeny: ImageButton = itemView.findViewById(R.id.btnDenyAdmin)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_request, parent, false)
        return AdminViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminViewHolder, position: Int) {
        val user = adminList[position]
        holder.tvName.text = user.fullName
        holder.tvEmail.text = user.email

        holder.btnApprove.setOnClickListener { onApprove(user) }
        holder.btnDeny.setOnClickListener { onDeny(user) }
    }

    override fun getItemCount() = adminList.size

    fun updateList(newList: List<User>) {
        adminList.clear()
        adminList.addAll(newList)
        notifyDataSetChanged()
    }
}