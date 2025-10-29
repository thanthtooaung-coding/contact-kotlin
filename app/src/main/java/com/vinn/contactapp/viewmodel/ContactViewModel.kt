package com.vinn.contactapp.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.vinn.contactapp.data.Contact
import com.vinn.contactapp.data.ContactDatabase
import com.vinn.contactapp.data.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContactViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ContactRepository
    val allContacts: LiveData<List<Contact>>

    private val _searchQuery = MutableLiveData<String>("")

    init {
        val contactDao = ContactDatabase.getDatabase(application).contactDao()
        repository = ContactRepository(contactDao)

        allContacts = _searchQuery.switchMap { query ->
            if (query.isEmpty()) {
                repository.allContacts
            } else {
                repository.searchContacts("%$query%")
            }
        }
    }

    fun insert(contact: Contact) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(contact)
    }

    fun update(contact: Contact) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(contact)
    }

    fun delete(contact: Contact) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(contact)
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
}
