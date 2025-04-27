package com.example.bondbuddy.ui.favourites

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bondbuddy.R
import com.example.bondbuddy.data.remote.models.UserInfo
import com.example.bondbuddy.databinding.SavedUserItemBinding
import com.example.bondbuddy.ui.adapters.DiscoverAdapter

class SavedUsersAdapter(
    private var users: List<UserInfo>,
    private val onRemoveClick: (UserInfo) -> Unit,
    private val onUserClick: (UserInfo) -> Unit
) : RecyclerView.Adapter<SavedUsersAdapter.SavedUsersViewHolder>() {

    inner class SavedUsersViewHolder(private val binding: SavedUserItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: UserInfo) {
            Glide.with(binding.ivUserAvatar.context)
                .load(user.photo)
                .placeholder(R.drawable.ic_person)
                .circleCrop()
                .into(binding.ivUserAvatar)

            binding.tvUserName.text = "${user.firstName} ${user.lastName}"

            binding.ivRemoveFromSaved.setOnClickListener {
                onRemoveClick(user)
            }

            binding.root.setOnClickListener {
                onUserClick(user)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedUsersViewHolder {
        val binding =
            SavedUserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SavedUsersViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SavedUsersViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    fun updateList(newList: List<UserInfo>) {
        users = newList
        notifyDataSetChanged()
    }
}
