package com.example.bondbuddy.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bondbuddy.R
import com.example.bondbuddy.data.remote.models.SocialMedia
import com.example.bondbuddy.databinding.InterestItemBinding
import com.example.bondbuddy.databinding.SocialMediaItemBinding

class SocialMediaAdapter(
    private val onEdit: (Int) -> Unit,
    private val onDelete: (Int) -> Unit,
    private val isEditModeEnabled: () -> Boolean
) : RecyclerView.Adapter<SocialMediaAdapter.SocialMediaViewHolder>() {

    class SocialMediaViewHolder(val binding: SocialMediaItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<SocialMedia>() {
        override fun areItemsTheSame(oldItem: SocialMedia, newItem: SocialMedia): Boolean {
            return oldItem.socialName == newItem.socialName
        }

        override fun areContentsTheSame(oldItem: SocialMedia, newItem: SocialMedia): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var socialMediaList: List<SocialMedia>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SocialMediaViewHolder {
        val binding =
            SocialMediaItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SocialMediaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SocialMediaViewHolder, position: Int) {
        val socialMedia = socialMediaList[position]

        holder.binding.tvPlatformName.text = socialMedia.socialName
        holder.binding.tvLink.text = socialMedia.socialLink

        if (isEditModeEnabled()) {
            holder.binding.optionsMenu.visibility = View.VISIBLE
            holder.binding.optionsMenu.setOnClickListener { view ->
                val popupMenu = PopupMenu(view.context, view)
                popupMenu.inflate(R.menu.social_media_options_menu)

                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.menu_edit -> {
                            onEdit(holder.adapterPosition)
                            true
                        }

                        R.id.menu_delete -> {
                            onDelete(holder.adapterPosition)
                            true
                        }

                        else -> false
                    }
                }
                popupMenu.show()
            }
        } else {
            holder.binding.optionsMenu.visibility = View.GONE
            holder.binding.optionsMenu.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int {
        return socialMediaList.size
    }
}