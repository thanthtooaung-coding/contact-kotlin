package com.vinn.contactapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.vinn.contactapp.data.Contact
import com.vinn.contactapp.data.ContactDatabase
import com.vinn.contactapp.data.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class ContactFilter { ALL, FAVORITES, TRASH }

private data class FilterAndQuery(val filter: ContactFilter, val query: String)

class ContactViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ContactRepository

    private val _filterState = MutableLiveData(ContactFilter.ALL)
    val currentFilter: LiveData<ContactFilter> = _filterState

    private val _searchQuery = MutableLiveData("")
    private val filterAndQuery = MediatorLiveData<FilterAndQuery>().apply {
        addSource(_filterState) { filter ->
            value = FilterAndQuery(filter, _searchQuery.value ?: "")
        }

        addSource(_searchQuery) { query ->
            value = FilterAndQuery(_filterState.value!!, query)
        }
    }

    val filteredContacts: LiveData<List<Contact>> = filterAndQuery.switchMap { params ->
        when (params.filter) {
            ContactFilter.ALL -> {
                val q = params.query.trim()
                if (q.isEmpty()) {
                    repository.allActiveContacts
                } else {
                    repository.searchActiveContacts("%$q%")
                }
            }
            ContactFilter.FAVORITES -> repository.favoriteContacts
            ContactFilter.TRASH -> repository.deletedContacts
        }
    }


    init {
        val contactDao = ContactDatabase.getDatabase(application).contactDao()
        repository = ContactRepository(contactDao)
        filterAndQuery.value = FilterAndQuery(ContactFilter.ALL, "")
    }

    fun insert(contact: Contact) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(contact)
    }

    fun update(contact: Contact) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(contact)
    }

    // Soft delete
    fun delete(contact: Contact) = viewModelScope.launch(Dispatchers.IO) {
        repository.markAsDeleted(contact)
    }

    fun restore(contact: Contact) = viewModelScope.launch(Dispatchers.IO) {
        repository.restoreContact(contact)
    }

    fun deletePermanently(contact: Contact) = viewModelScope.launch(Dispatchers.IO) {
        repository.deletePermanently(contact)
    }

    fun toggleFavorite(contact: Contact) = viewModelScope.launch(Dispatchers.IO) {
        repository.toggleFavorite(contact)
    }

    fun emptyTrash() = viewModelScope.launch(Dispatchers.IO) {
        repository.emptyTrash()
    }

    fun setFilter(filter: ContactFilter) {
        val oldFilter = _filterState.value

        if (filter != ContactFilter.ALL && oldFilter == ContactFilter.ALL) {
            if (_searchQuery.value != "") {
                _searchQuery.value = ""
            }
        }

        if (oldFilter != filter) {
            _filterState.value = filter
        }
    }

    fun setSearchQuery(query: String) {
        val trimmedQuery = query.trim()

        Log.d("SearchDebug", "ViewModel.setSearchQuery: Received '$trimmedQuery'. Current value is '${_searchQuery.value}'")

        if (_searchQuery.value != trimmedQuery) {
            Log.d("SearchDebug", "ViewModel.setSearchQuery: Updating query to '$trimmedQuery'")
            _searchQuery.value = trimmedQuery
        } else {
            Log.d("SearchDebug", "ViewModel.setSearchQuery: Query '$trimmedQuery' is the same. Ignoring.")
        }
    }

    fun getSearchQuery(): String {
        return _searchQuery.value ?: ""
    }
}
