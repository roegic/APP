package com.example.bondbuddy.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bondbuddy.data.remote.models.Interest
import com.example.bondbuddy.databinding.InterestItemBinding
import com.example.bondbuddy.ui.profile.ProfileFragment

class InterestAdapter(
    private val onInterestClick: (position: Int, interest: Interest) -> Unit,
    private val isEditModeEnabled: () -> Boolean
) : RecyclerView.Adapter<InterestAdapter.InterestViewHolder>() {

    class InterestViewHolder(val binding: InterestItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<Interest>() {
        override fun areItemsTheSame(oldItem: Interest, newItem: Interest): Boolean {
            return oldItem.name == newItem.name && oldItem.type == newItem.type
        }

        override fun areContentsTheSame(oldItem: Interest, newItem: Interest): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var interests: List<Interest>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InterestViewHolder {
        return InterestViewHolder(
            InterestItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int = interests.size

    override fun onBindViewHolder(holder: InterestViewHolder, position: Int) {
        val interest = interests[position]
        holder.binding.tvInterest.text = "${interest.name} [${interest.type}]"

        holder.binding.tvInterest.setOnClickListener {
            if (isEditModeEnabled()) {
                onInterestClick(holder.adapterPosition, interest)
            }
        }
    }
}
