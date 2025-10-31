package com.vinn.contactapp.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.vinn.contactapp.R
import com.vinn.contactapp.adapter.ContactAdapter
import com.vinn.contactapp.data.Contact
import com.vinn.contactapp.databinding.DialogContactDetailsBinding
import com.vinn.contactapp.databinding.FragmentContactListBinding
import com.vinn.contactapp.utils.AvatarStore
import com.vinn.contactapp.viewmodel.ContactFilter
import com.vinn.contactapp.viewmodel.ContactViewModel

class ContactListFragment : Fragment() {

    private var _binding: FragmentContactListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContactViewModel by activityViewModels()
    private lateinit var contactAdapter: ContactAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper
    private var currentFilter = ContactFilter.ALL

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
            navigateToAddEditFragment(null)
        }

        viewModel.filteredContacts.observe(viewLifecycleOwner) { contacts ->
            contactAdapter.submitList(contacts)
            binding.tvEmptyState.visibility = if (contacts.isNullOrEmpty()) View.VISIBLE else View.GONE
            updateEmptyStateText()
        }

        viewModel.currentFilter.observe(viewLifecycleOwner) { filter ->
            currentFilter = filter
            updateUiForFilter()
            requireActivity().invalidateOptionsMenu()
            updateEmptyStateText()
        }
    }

    private fun updateEmptyStateText() {
        binding.tvEmptyState.text = when (currentFilter) {
            ContactFilter.ALL -> getString(R.string.empty_state_all)
            ContactFilter.FAVORITES -> getString(R.string.empty_state_favorites)
            ContactFilter.TRASH -> getString(R.string.empty_state_trash)
        }
    }

    private fun updateUiForFilter() {
        when (currentFilter) {
            ContactFilter.TRASH -> {
                binding.fabAddContact.hide()
                binding.toolbar.title = getString(R.string.title_trash)
                setupItemTouchHelper(isTrashView = true)
            }
            ContactFilter.FAVORITES -> {
                binding.fabAddContact.show()
                binding.toolbar.title = getString(R.string.title_favorites)
                setupItemTouchHelper(isTrashView = false)
            }
            else -> {
                binding.fabAddContact.show()
                binding.toolbar.title = getString(R.string.title_all_contacts)
                setupItemTouchHelper(isTrashView = false)
            }
        }
        binding.toolbar.invalidateMenu()
    }

    private fun setupMenu() {
        val menuHost: MenuHost = binding.toolbar

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.list_menu, menu)

                val searchItem = menu.findItem(R.id.action_search)
                searchItem.isVisible = currentFilter == ContactFilter.ALL

                menu.findItem(R.id.action_empty_trash).isVisible = currentFilter == ContactFilter.TRASH

                val searchView = searchItem.actionView as SearchView

                searchItem.isVisible = currentFilter == ContactFilter.ALL

                val currentQuery = viewModel.getSearchQuery()
                if (currentFilter == ContactFilter.ALL && currentQuery.isNotEmpty()) {
                    searchItem.expandActionView()
                    searchView.setQuery(currentQuery, false)
                }

                when (currentFilter) {
                    ContactFilter.ALL ->
                        menu.findItem(R.id.action_show_all).isChecked = true
                    ContactFilter.FAVORITES ->
                        menu.findItem(R.id.action_show_favorites).isChecked = true
                    ContactFilter.TRASH ->
                        menu.findItem(R.id.action_show_trash).isChecked = true
                }

                searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                    override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                        return true
                    }

                    override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                        viewModel.setSearchQuery("")
                        return true
                    }
                })

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        val query = newText.orEmpty()
                        val currentVMQuery = viewModel.getSearchQuery()

                        Log.d("SearchDebug", "Fragment listener: newText is '$query'")
                        Log.d("SearchDebug", "Fragment listener: current VM query is '$currentVMQuery'")

                         if (currentVMQuery != query) {
                            Log.d("SearchDebug", "Fragment listener: Queries are DIFFERENT. Calling setSearchQuery.")
                            viewModel.setSearchQuery(query)
                         } else {
                            Log.d("SearchDebug", "Fragment listener: Queries are the SAME. Blocking call.")
                         }
                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.action_show_all -> {
                        viewModel.setFilter(ContactFilter.ALL)
                        return true
                    }
                    R.id.action_show_favorites -> {
                        viewModel.setFilter(ContactFilter.FAVORITES)
                        return true
                    }
                    R.id.action_show_trash -> {
                        viewModel.setFilter(ContactFilter.TRASH)
                        return true
                    }
                    R.id.action_empty_trash -> {
                        showEmptyTrashConfirmationDialog()
                        return true
                    }
                    R.id.action_search -> return false
                    else -> return false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupRecyclerView() {
        contactAdapter = ContactAdapter(
            onClick = { contact ->
                if (currentFilter != ContactFilter.TRASH) {
                    navigateToAddEditFragment(contact)
                } else {
                    Toast.makeText(context, R.string.edit_not_allowed_in_trash, Toast.LENGTH_SHORT).show()
                }
            },
            onFavoriteClick = { contact ->
                viewModel.toggleFavorite(contact)
            }
        )

        binding.rvContacts.apply {
            adapter = contactAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

        setupItemTouchHelper(isTrashView = false)
    }

    private fun setupItemTouchHelper(isTrashView: Boolean) {
        if (::itemTouchHelper.isInitialized) {
            itemTouchHelper.attachToRecyclerView(null)
        }

        val swipeDirs = if (isTrashView) {
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        } else {
            ItemTouchHelper.LEFT
        }

        val simpleCallback = object : ItemTouchHelper.SimpleCallback(0, swipeDirs) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val contact = contactAdapter.currentList[position]
                    if (isTrashView) {
                        if (direction == ItemTouchHelper.LEFT) {
                            showPermanentDeleteConfirmationDialog(contact)
                        } else {
                            showRestoreConfirmationDialog(contact)
                        }
                    } else {
                        showSoftDeleteConfirmationDialog(contact)
                    }
                }
            }
        }

        itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvContacts)
    }


    private fun navigateToAddEditFragment(contact: Contact?) {
        val action = ContactListFragmentDirections.actionContactListFragmentToAddEditContactFragment(contact)
        findNavController().navigate(action)
    }

    private fun showSoftDeleteConfirmationDialog(contact: Contact) {
        showConfirmationDialog(
            titleRes = R.string.delete_contact_title,
            messageFormatRes = R.string.delete_confirmation_message,
            positiveButtonTextRes = R.string.delete_button,
            iconRes = R.drawable.ic_delete,
            contact = contact
        ) {
            viewModel.delete(contact)
            Snackbar.make(binding.root, getString(R.string.contact_moved_to_trash, contact.name), Snackbar.LENGTH_LONG)
                .setAction(R.string.undo) {
                    viewModel.restore(contact)
                }
                .show()
        }
    }

    private fun showRestoreConfirmationDialog(contact: Contact) {
        showConfirmationDialog(
            titleRes = R.string.restore_contact_title,
            messageFormatRes = R.string.restore_confirmation_message,
            positiveButtonTextRes = R.string.restore_button,
            iconRes = R.drawable.ic_restore,
            contact = contact
        ) {
            viewModel.restore(contact)
            Toast.makeText(context, getString(R.string.contact_restored, contact.name), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPermanentDeleteConfirmationDialog(contact: Contact) {
        showConfirmationDialog(
            titleRes = R.string.permanent_delete_title,
            messageFormatRes = R.string.permanent_delete_confirmation_message,
            positiveButtonTextRes = R.string.delete_permanently_button,
            iconRes = R.drawable.ic_delete_forever,
            contact = contact
        ) {
            viewModel.deletePermanently(contact)
            Toast.makeText(context, getString(R.string.contact_permanently_deleted, contact.name), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEmptyTrashConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.empty_trash_title)
            .setMessage(R.string.empty_trash_confirmation_message)
            .setIcon(R.drawable.ic_delete_sweep)
            .setPositiveButton(R.string.empty_trash_button) { _, _ ->
                viewModel.emptyTrash()
                Toast.makeText(context, R.string.trash_emptied, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel_button, null)
            .show()
    }

    private fun showConfirmationDialog(
        titleRes: Int,
        messageFormatRes: Int,
        positiveButtonTextRes: Int,
        iconRes: Int,
        contact: Contact,
        onConfirm: () -> Unit
    ) {
        val dialogBinding = DialogContactDetailsBinding.inflate(layoutInflater)
        dialogBinding.dialogTvName.text = contact.name
        dialogBinding.dialogTvEmail.text = contact.email
        dialogBinding.dialogTvMessage.text = getString(messageFormatRes, contact.name)
        val avatarResId = AvatarStore.getAvatarResourceId(requireContext(), contact.avatarResName)
        dialogBinding.dialogImgAvatar.setImageResource(avatarResId)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(titleRes))
            .setIcon(iconRes)
            .setView(dialogBinding.root)
            .setPositiveButton(getString(positiveButtonTextRes)) { _, _ ->
                onConfirm()
            }
            .setNegativeButton(getString(R.string.cancel_button)) { dialog, _ ->
                if (currentFilter != ContactFilter.TRASH) {
                    val index = contactAdapter.currentList.indexOf(contact)
                    if (index != -1) contactAdapter.notifyItemChanged(index)
                } else {
                    val index = contactAdapter.currentList.indexOf(contact)
                    if (index != -1) contactAdapter.notifyItemChanged(index)
                }
                dialog.dismiss()
            }
            .setOnCancelListener {
                val index = contactAdapter.currentList.indexOf(contact)
                if (index != -1) contactAdapter.notifyItemChanged(index)
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        itemTouchHelper.attachToRecyclerView(null)
        _binding = null
    }
}
