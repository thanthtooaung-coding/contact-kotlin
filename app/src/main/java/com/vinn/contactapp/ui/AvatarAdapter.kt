package com.vinn.contactapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vinn.contactapp.databinding.ListItemAvatarBinding
import com.vinn.contactapp.utils.Avatar
import com.vinn.contactapp.utils.AvatarStore

class AvatarAdapter(
    private val avatars: List<Avatar>,
    private val onClick: (Avatar) -> Unit
) : RecyclerView.Adapter<AvatarAdapter.AvatarViewHolder>() {

    private var selectedPosition = 0

    inner class AvatarViewHolder(val binding: ListItemAvatarBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(avatar: Avatar) {
            val resId = AvatarStore.getAvatarResourceId(itemView.context, avatar.resName)
            binding.imgAvatar.setImageResource(resId)
            
            // Show selection state
            binding.avatarCard.strokeWidth = if (selectedPosition == adapterPosition) 8 else 0

            binding.root.setOnClickListener {
                onClick(avatar)
            }
        }
    }
    
    fun setSelected(avatar: Avatar) {
        val newPosition = avatars.indexOf(avatar)
        if (newPosition != -1) {
            notifyItemChanged(selectedPosition) // Unselect old
            selectedPosition = newPosition
            notifyItemChanged(selectedPosition) // Select new
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvatarViewHolder {
        val binding = ListItemAvatarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AvatarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AvatarViewHolder, position: Int) {
        holder.bind(avatars[position])
    }

    override fun getItemCount() = avatars.size
}
