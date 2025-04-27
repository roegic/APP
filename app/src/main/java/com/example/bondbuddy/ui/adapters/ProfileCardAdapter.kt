package com.example.bondbuddy.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bondbuddy.R
import com.example.bondbuddy.data.remote.models.UserInfo
import com.example.bondbuddy.utils.byteArrayToBitmap

class ProfileCardAdapter(private val profiles: List<UserInfo>) :
    RecyclerView.Adapter<ProfileCardAdapter.ProfileViewHolder>() {

    class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewProfile: ImageView = itemView.findViewById(R.id.imageViewProfile)
        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        val textViewBio: TextView = itemView.findViewById(R.id.textViewBio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.profile_card, parent, false)
        return ProfileViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val currentProfile = profiles[position]
        holder.textViewName.text = "${currentProfile.firstName} ${currentProfile.lastName}, ${currentProfile.age}"
        holder.textViewBio.text = currentProfile.bio

        val photoByteArray = currentProfile.photo
        val bitmap = byteArrayToBitmap(photoByteArray)
        if (bitmap != null) {
            holder.imageViewProfile.setImageBitmap(bitmap)
        } else {
            holder.imageViewProfile.setImageResource(R.drawable.ic_person)
        }
    }

    override fun getItemCount() = profiles.size
}