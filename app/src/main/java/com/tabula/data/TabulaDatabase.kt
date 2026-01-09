package com.tabula.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [PhotoEntity::class, TrashPhotoEntity::class], version = 2, exportSchema = false)
abstract class TabulaDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
    abstract fun trashPhotoDao(): TrashPhotoDao

    companion object {
        @Volatile
        private var INSTANCE: TabulaDatabase? = null

        fun getInstance(context: Context): TabulaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TabulaDatabase::class.java,
                    "tabula.db"
                ).addMigrations(MIGRATION_1_2).build().also { INSTANCE = it }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `trash_photos` (`id` INTEGER NOT NULL, `uri` TEXT NOT NULL, `dateAdded` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                )
            }
        }
    }
}
