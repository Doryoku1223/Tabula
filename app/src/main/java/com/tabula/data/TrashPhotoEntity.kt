package com.tabula.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trash_photos")
data class TrashPhotoEntity(
    @PrimaryKey val id: Long,
    val uri: String,
    val dateAdded: Long
)
