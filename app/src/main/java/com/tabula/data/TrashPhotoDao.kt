package com.tabula.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TrashPhotoDao {
    @Query("SELECT * FROM trash_photos ORDER BY dateAdded DESC")
    suspend fun getAll(): List<TrashPhotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(photos: List<TrashPhotoEntity>)

    @Query("DELETE FROM trash_photos WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM trash_photos")
    suspend fun deleteAll()
}
