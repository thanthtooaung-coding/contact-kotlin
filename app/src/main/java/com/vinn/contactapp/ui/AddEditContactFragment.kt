package com.vinn.contactapp.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.children
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vinn.contactapp.R
import com.vinn.contactapp.data.Contact
import com.vinn.contactapp.databinding.DialogContactDetailsBinding
import com.vinn.contactapp.databinding.FragmentAddEditContactBinding
import com.vinn.contactapp.utils.AvatarStore
import com.vinn.contactapp.viewmodel.ContactViewModel
import java.text.SimpleDateFormat
import java.util.*

class AddEditContactFragment : Fragment() {

    private var _binding: FragmentAddEditContactBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContactViewModel by activityViewModels()
    private val args: AddEditContactFragmentArgs by navArgs()

    private var currentContact: Contact? = null
    private var selectedDob: Long = System.currentTimeMillis()
    private var selectedAvatarResName: String = AvatarStore.avatars.first().resName

    private val suggestedLabels = listOf("Family", "Work", "Friend", "Emergency", "Vendor")

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
        setupLabelChipGroup()
        setupLabelFieldValidation()

        if (currentContact != null) {
            binding.toolbar.title = getString(R.string.edit_contact_title)
            binding.btnSave.text = getString(R.string.update_contact_button)
            populateFields()
        } else {
            binding.toolbar.title = getString(R.string.add_contact_title)
            binding.btnSave.text = getString(R.string.save_contact_button)
            updateDobText()
            updateAvatarPreview()
            avatarAdapter.setSelected(AvatarStore.avatars.first())
        }

        binding.etDob.setOnClickListener {
            showDatePicker()
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSave.setOnClickListener {
            validateAndSaveContact()
        }
    }

    private fun setupLabelChipGroup() {
        binding.chipGroupLabels.removeAllViews()
        suggestedLabels.forEach { label ->
            val chip = Chip(requireContext()).apply {
                text = label
                isClickable = true
                isCheckable = true
                chipBackgroundColor = resources.getColorStateList(R.color.chip_background_selector, null)
                setTextColor(resources.getColorStateList(R.color.chip_text_selector, null))
            }

            chip.setOnCheckedChangeListener { chipView, isChecked ->
                if (isChecked) {
                    binding.etLabel.setText(chipView.text)
                } else if (binding.etLabel.text.toString() == chipView.text.toString()) {
                    binding.etLabel.setText("")
                }
            }
            binding.chipGroupLabels.addView(chip)
        }
    }

    private fun setupLabelFieldValidation() {
        binding.etLabel.doAfterTextChanged { text ->
            val label = text.toString().trim()
            if (label.length > 15) {
                binding.tilLabel.error = getString(R.string.error_label_max_length)
            } else {
                binding.tilLabel.error = null
            }

            binding.chipGroupLabels.children.mapNotNull { it as? Chip }.forEach { chip ->
                if (chip.text.toString() != label && chip.isChecked) {
                    chip.isChecked = false
                }
            }
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
            setHasFixedSize(true)
        }
    }

    private fun populateFields() {
        currentContact?.let { contact ->
            binding.etName.setText(contact.name)
            binding.etEmail.setText(contact.email)
            binding.etLabel.setText(contact.label)
            selectedDob = contact.dob
            selectedAvatarResName = contact.avatarResName

            updateDobText()
            updateAvatarPreview()

            val selectedChip = binding.chipGroupLabels.children.mapNotNull { it as? Chip }.find { it.text == contact.label }
            selectedChip?.isChecked = true

            val selectedAvatar = AvatarStore.avatars.find { it.resName == contact.avatarResName }
            selectedAvatar?.let {
                avatarAdapter.setSelected(it)
                val position = AvatarStore.avatars.indexOf(it)
                if (position != -1) {
                    binding.rvAvatars.scrollToPosition(position)
                }
            }
        }
    }

    private fun updateDobText() {
        binding.etDob.setText(formatDate(selectedDob))
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
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
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
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

    private fun validateAndSaveContact() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val label = binding.etLabel.text.toString().trim()

        if (name.isEmpty()) {
            binding.tilName.error = getString(R.string.error_name_required)
            return
        } else {
            binding.tilName.error = null
        }

        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_email_required)
            return
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.error_email_invalid)
            return
        }
        else {
            binding.tilEmail.error = null
        }

        if (label.length > 15) {
            binding.tilLabel.error = getString(R.string.error_label_max_length)
            return
        }
        else {
            binding.tilLabel.error = null
        }


        val contactToSave = (currentContact?.copy(
            name = name,
            email = email,
            dob = selectedDob,
            avatarResName = selectedAvatarResName,
            label = label
        ) ?: Contact(
            name = name,
            email = email,
            dob = selectedDob,
            avatarResName = selectedAvatarResName,
            label = label
        ))


        val dialogTitleRes = if (currentContact == null) R.string.save_contact_title else R.string.update_contact_title
        val dialogMessageRes = if (currentContact == null) R.string.save_confirmation_message else R.string.update_confirmation_message
        val positiveButtonTextRes = if (currentContact == null) R.string.save_button else R.string.update_button


        showSaveConfirmationDialog(
            getString(dialogTitleRes),
            getString(dialogMessageRes),
            getString(positiveButtonTextRes),
            contactToSave
        )
    }

    private fun showSaveConfirmationDialog(title: String, message: String, positiveButtonText: String, contact: Contact) {
        val dialogBinding = DialogContactDetailsBinding.inflate(layoutInflater)

        dialogBinding.dialogTvName.text = contact.name
        dialogBinding.dialogTvEmail.text = contact.email
        dialogBinding.dialogTvMessage.text = message
        val avatarResId = AvatarStore.getAvatarResourceId(requireContext(), contact.avatarResName)
        dialogBinding.dialogImgAvatar.setImageResource(avatarResId)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setView(dialogBinding.root)
            .setPositiveButton(positiveButtonText) { _, _ ->
                if (currentContact == null) {
                    viewModel.insert(contact)
                    Toast.makeText(requireContext(), R.string.contact_saved_success, Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.update(contact)
                    Toast.makeText(requireContext(), R.string.contact_updated_success, Toast.LENGTH_SHORT).show()
                }
                findNavController().popBackStack()
            }
            .setNegativeButton(R.string.cancel_button, null)
            .show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
