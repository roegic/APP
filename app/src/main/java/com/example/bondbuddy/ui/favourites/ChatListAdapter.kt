package com.example.bondbuddy.ui.favourites
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bondbuddy.R
import com.example.bondbuddy.data.remote.models.ChatUserInfo
import com.example.bondbuddy.databinding.ChatUserItemBinding

class ChatListAdapter(
    private var users: List<ChatUserInfo>,
    private val onUserClick: (ChatUserInfo) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder>() {

    inner class ChatListViewHolder(private val binding: ChatUserItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: ChatUserInfo) {
            Glide.with(binding.ivUserAvatar.context)
                .load(user.photo)
                .placeholder(R.drawable.ic_person)
                .circleCrop()
                .into(binding.ivUserAvatar)

            binding.tvUserName.text = "${user.firstName} ${user.lastName}"

            binding.viewUnreadIndicator.visibility = if (!user.isRead) View.VISIBLE else View.GONE

            binding.root.setOnClickListener {
                onUserClick(user)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListViewHolder {
        val binding =
            ChatUserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    fun updateList(newList: List<ChatUserInfo>) {
        users = newList
        notifyDataSetChanged()
    }
}
