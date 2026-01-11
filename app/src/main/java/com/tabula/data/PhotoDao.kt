package com.tabula.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(photos: List<PhotoEntity>)

    @Query("SELECT id FROM photos")
    suspend fun getAllIds(): List<Long>

    @Query("SELECT * FROM photos ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomPhotos(limit: Int): List<PhotoEntity>

    @Query("SELECT * FROM photos ORDER BY dateTaken ASC")
    suspend fun getAllPhotosByDateAsc(): List<PhotoEntity>

    @Query("SELECT MAX(dateTaken) FROM photos")
    suspend fun getMaxDateTaken(): Long?

    @Query("DELETE FROM photos")
    suspend fun deleteAll()
}
