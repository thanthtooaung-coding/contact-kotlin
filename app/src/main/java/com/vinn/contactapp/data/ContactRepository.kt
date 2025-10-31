package com.vinn.contactapp.data

import androidx.lifecycle.LiveData

class ContactRepository(private val contactDao: ContactDao) {

    val allActiveContacts: LiveData<List<Contact>> = contactDao.getAllActiveContacts()
    val favoriteContacts: LiveData<List<Contact>> = contactDao.getFavoriteContacts()
    val deletedContacts: LiveData<List<Contact>> = contactDao.getDeletedContacts()

    fun searchActiveContacts(query: String): LiveData<List<Contact>> {
        print(query)
        return contactDao.searchActiveContacts(query)
    }

    suspend fun insert(contact: Contact) {
        contactDao.insertContact(contact)
    }

    suspend fun update(contact: Contact) {
        contactDao.updateContact(contact)
    }

    suspend fun markAsDeleted(contact: Contact) {
        contactDao.markAsDeleted(contact.id, System.currentTimeMillis())
    }

    suspend fun restoreContact(contact: Contact) {
        contactDao.restoreContact(contact.id)
    }

    suspend fun deletePermanently(contact: Contact) {
        contactDao.deleteContactPermanently(contact)
    }

    suspend fun toggleFavorite(contact: Contact) {
        contactDao.setFavoriteStatus(contact.id, !contact.isFavorite)
    }

    suspend fun emptyTrash() {
        contactDao.emptyTrash()
    }
}
