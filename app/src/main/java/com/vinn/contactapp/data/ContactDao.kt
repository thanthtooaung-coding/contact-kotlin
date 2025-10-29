package com.vinn.contactapp.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact)

    @Update
    suspend fun updateContact(contact: Contact)

    @Delete
    suspend fun deleteContact(contact: Contact)

    @Query("SELECT * FROM contact_table ORDER BY name ASC")
    fun getAllContacts(): LiveData<List<Contact>>

    @Query("SELECT * FROM contact_table WHERE name LIKE :searchQuery OR email LIKE :searchQuery ORDER BY name ASC")
    fun searchContacts(searchQuery: String): LiveData<List<Contact>>
}
