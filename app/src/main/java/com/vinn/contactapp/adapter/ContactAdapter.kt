package com.vinn.contactapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vinn.contactapp.data.Contact
import com.vinn.contactapp.databinding.ListItemContactBinding
import com.vinn.contactapp.utils.AvatarStore

class ContactAdapter(private val onClick: (Contact) -> Unit) :
    ListAdapter<Contact, ContactAdapter.ContactViewHolder>(ContactDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ListItemContactBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class ContactViewHolder(private val binding: ListItemContactBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onClick(getItem(adapterPosition))
                }
            }
        }

        fun bind(contact: Contact) {
            binding.apply {
                tvName.text = contact.name
                tvEmail.text = contact.email

                val avatarResId = AvatarStore.getAvatarResourceId(itemView.context, contact.avatarResName)
                imgAvatar.setImageResource(avatarResId)
            }
        }
    }

    class ContactDiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact) =
            oldItem == newItem
    }
}
