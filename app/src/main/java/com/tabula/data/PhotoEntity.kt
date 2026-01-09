package com.tabula.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey val id: Long,
    val dateTaken: Long,
    val uri: String
)
