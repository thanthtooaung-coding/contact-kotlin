package com.vinn.contactapp.ui

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vinn.contactapp.R
import com.vinn.contactapp.adapter.ContactAdapter
import com.vinn.contactapp.data.Contact
import com.vinn.contactapp.databinding.FragmentContactListBinding
import com.vinn.contactapp.viewmodel.ContactViewModel

class ContactListFragment : Fragment() {

    private var _binding: FragmentContactListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContactViewModel by viewModels()
    private lateinit var contactAdapter: ContactAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupMenu()

        binding.fabAddContact.setOnClickListener {
            // Navigate to AddEditFragment with no contact (for creating new)
            val action = ContactListFragmentDirections.actionContactListFragmentToAddEditContactFragment(null)
            findNavController().navigate(action)
        }

        viewModel.allContacts.observe(viewLifecycleOwner) { contacts ->
            contacts?.let {
                contactAdapter.submitList(it)
                binding.tvEmptyState.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun setupMenu() {
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_search -> {
                    val searchView = it.actionView as SearchView
                    searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            return false
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            viewModel.setSearchQuery(newText.orEmpty())
                            return true
                        }
                    })
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        contactAdapter = ContactAdapter { contact ->
            // On Click: Navigate to AddEditFragment with the contact (for editing)
            val action = ContactListFragmentDirections.actionContactListFragmentToAddEditContactFragment(contact)
            findNavController().navigate(action)
        }

        binding.rvContacts.apply {
            adapter = contactAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // Add swipe-to-delete
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val contact = contactAdapter.currentList[viewHolder.adapterPosition]
                showDeleteConfirmationDialog(contact)
            }
        }).attachToRecyclerView(binding.rvContacts)
    }

    private fun showDeleteConfirmationDialog(contact: Contact) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Contact")
            .setMessage("Are you sure you want to delete ${contact.name}?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Delete") { _, _ ->
                viewModel.delete(contact)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                // Undo the swipe animation
                contactAdapter.notifyItemChanged(contactAdapter.currentList.indexOf(contact))
            }
            .create()
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
