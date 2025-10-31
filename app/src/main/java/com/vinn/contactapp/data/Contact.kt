package com.vinn.contactapp.data
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
@Entity(tableName = "contact_table")
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val dob: Long,
    val email: String,
    val avatarResName: String,
    val label: String = "",
    var isFavorite: Boolean = false,
    var isDeleted: Boolean = false,
    var deletedTimestamp: Long? = null
) : Parcelable {
    fun getFormattedDob(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(dob))
    }
}
