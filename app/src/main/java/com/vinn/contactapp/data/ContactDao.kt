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
    suspend fun deleteContactPermanently(contact: Contact)

    @Query("SELECT * FROM contact_table WHERE isDeleted = 0 ORDER BY isFavorite DESC, name ASC")
    fun getAllActiveContacts(): LiveData<List<Contact>>

    @Query("SELECT * FROM contact_table WHERE isDeleted = 0 AND isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteContacts(): LiveData<List<Contact>>

    @Query("""
        SELECT * FROM contact_table 
        WHERE isDeleted = 0 
          AND (name LIKE :searchQuery ESCAPE '\' COLLATE NOCASE 
               OR email LIKE :searchQuery ESCAPE '\' COLLATE NOCASE)
        ORDER BY isFavorite DESC, name ASC
    """)
    fun searchActiveContacts(searchQuery: String): LiveData<List<Contact>>

    @Query("SELECT * FROM contact_table WHERE isDeleted = 1 ORDER BY deletedTimestamp DESC")
    fun getDeletedContacts(): LiveData<List<Contact>>

    @Query("UPDATE contact_table SET isFavorite = :isFavorite WHERE id = :contactId")
    suspend fun setFavoriteStatus(contactId: Int, isFavorite: Boolean)

    @Query("UPDATE contact_table SET isDeleted = 1, deletedTimestamp = :timestamp WHERE id = :contactId")
    suspend fun markAsDeleted(contactId: Int, timestamp: Long)

    @Query("UPDATE contact_table SET isDeleted = 0, deletedTimestamp = null WHERE id = :contactId")
    suspend fun restoreContact(contactId: Int)

    @Query("DELETE FROM contact_table WHERE isDeleted = 1")
    suspend fun emptyTrash()
}
