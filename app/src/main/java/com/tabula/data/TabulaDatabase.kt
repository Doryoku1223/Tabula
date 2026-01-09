package com.tabula.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PhotoEntity::class], version = 1, exportSchema = false)
abstract class TabulaDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao

    companion object {
        @Volatile
        private var INSTANCE: TabulaDatabase? = null

        fun getInstance(context: Context): TabulaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TabulaDatabase::class.java,
                    "tabula.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
