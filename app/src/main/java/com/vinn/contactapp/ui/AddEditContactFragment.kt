package com.vinn.contactapp.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.vinn.contactapp.R
import com.vinn.contactapp.data.Contact
import com.vinn.contactapp.databinding.FragmentAddEditContactBinding
import com.vinn.contactapp.utils.AvatarStore
import com.vinn.contactapp.viewmodel.ContactViewModel
import java.text.SimpleDateFormat
import java.util.*

class AddEditContactFragment : Fragment() {

    private var _binding: FragmentAddEditContactBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContactViewModel by viewModels()
    private val args: AddEditContactFragmentArgs by navArgs()

    private var currentContact: Contact? = null
    private var selectedDob: Long = System.currentTimeMillis()
    private var selectedAvatarResName: String = AvatarStore.avatars.first().resName

    private lateinit var avatarAdapter: AvatarAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentContact = args.currentContact

        setupAvatarRecyclerView()

        if (currentContact != null) {
            // Edit mode
            binding.toolbar.title = "Edit Contact"
            binding.btnSave.text = "Update Contact"
            populateFields()
        } else {
            // Add mode
            binding.toolbar.title = "Add Contact"
            binding.btnSave.text = "Save Contact"
            updateDobText()
            updateAvatarPreview()
        }

        binding.etDob.setOnClickListener {
            showDatePicker()
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSave.setOnClickListener {
            saveContact()
        }
    }

    private fun setupAvatarRecyclerView() {
        avatarAdapter = AvatarAdapter(AvatarStore.avatars) { avatar ->
            selectedAvatarResName = avatar.resName
            updateAvatarPreview()
            avatarAdapter.setSelected(avatar)
        }

        binding.rvAvatars.apply {
            adapter = avatarAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun populateFields() {
        currentContact?.let {
            binding.etName.setText(it.name)
            binding.etEmail.setText(it.email)
            selectedDob = it.dob
            selectedAvatarResName = it.avatarResName

            updateDobText()
            updateAvatarPreview()

            val selectedAvatar = AvatarStore.avatars.find { a -> a.resName == it.avatarResName }
            if (selectedAvatar != null) {
                avatarAdapter.setSelected(selectedAvatar)
                binding.rvAvatars.scrollToPosition(AvatarStore.avatars.indexOf(selectedAvatar))
            }
        }
    }

    private fun updateDobText() {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.etDob.setText(sdf.format(Date(selectedDob)))
    }

    private fun updateAvatarPreview() {
        val resId = AvatarStore.getAvatarResourceId(requireContext(), selectedAvatarResName)
        binding.imgAvatarPreview.setImageResource(resId)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDob

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            selectedDob = calendar.timeInMillis
            updateDobText()
        }

        DatePickerDialog(
            requireContext(),
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveContact() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val title = if (currentContact == null) "Save Contact" else "Update Contact"
        val message = "Are you sure you want to $title?"

        showSaveConfirmationDialog(title, message, name, email)
    }

    private fun showSaveConfirmationDialog(title: String, message: String, name: String, email: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setIcon(R.drawable.ic_save)
            .setPositiveButton(title) { _, _ ->
                if (currentContact == null) {
                    // Create new
                    val newContact = Contact(
                        name = name,
                        email = email,
                        dob = selectedDob,
                        avatarResName = selectedAvatarResName
                    )
                    viewModel.insert(newContact)
                } else {
                    // Update existing
                    val updatedContact = currentContact!!.copy(
                        name = name,
                        email = email,
                        dob = selectedDob,
                        avatarResName = selectedAvatarResName
                    )
                    viewModel.update(updatedContact)
                }
                findNavController().popBackStack()
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
