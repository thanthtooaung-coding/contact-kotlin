package com.vinn.contactapp.data

import androidx.lifecycle.LiveData

class ContactRepository(private val contactDao: ContactDao) {

    val allContacts: LiveData<List<Contact>> = contactDao.getAllContacts()

    suspend fun insert(contact: Contact) {
        contactDao.insertContact(contact)
    }

    suspend fun update(contact: Contact) {
        contactDao.updateContact(contact)
    }

    suspend fun delete(contact: Contact) {
        contactDao.deleteContact(contact)
    }

    fun searchContacts(query: String): LiveData<List<Contact>> {
        return contactDao.searchContacts(query)
    }
}
