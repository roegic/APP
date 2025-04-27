package com.example.bondbuddy.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bondbuddy.R
import com.example.bondbuddy.data.remote.models.UserInfo
import com.example.bondbuddy.utils.byteArrayToBitmap

class DiscoverAdapter(
    private val onItemClick: (UserInfo) -> Unit,
    private val onFavoriteClick: (UserInfo) -> Unit
) : ListAdapter<UserInfo, DiscoverAdapter.DiscoverViewHolder>(UserDiffCallback()) {

    class DiscoverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewProfile: ImageView = itemView.findViewById(R.id.imageViewProfile)
        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        val textViewBio: TextView = itemView.findViewById(R.id.textViewBio)
        val imageButtonFavorite: ImageView = itemView.findViewById(R.id.imageButtonFavorite)
        val textViewInterests: TextView = itemView.findViewById(R.id.textViewInterests)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscoverViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.profile_card, parent, false)
        return DiscoverViewHolder(view)
    }

    override fun onBindViewHolder(holder: DiscoverViewHolder, position: Int) {
        val user = getItem(position)

        holder.textViewName.text = "${user.firstName} ${user.lastName}"
        holder.textViewBio.text = user.bio ?: "Нет информации"
        holder.textViewInterests.text = user.interests

        val photoByteArray = user.photo
        val bitmap = byteArrayToBitmap(photoByteArray)
        if (bitmap != null) {
            holder.imageViewProfile.setImageBitmap(bitmap)
        } else {
            holder.imageViewProfile.setImageResource(R.drawable.ic_person)
        }

        holder.itemView.setOnClickListener {
            onItemClick(user)
        }

        holder.imageButtonFavorite.setOnClickListener {
            onFavoriteClick(user)
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<UserInfo>() {
        override fun areItemsTheSame(oldItem: UserInfo, newItem: UserInfo): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(oldItem: UserInfo, newItem: UserInfo): Boolean {
            return oldItem == newItem
        }
    }
}
