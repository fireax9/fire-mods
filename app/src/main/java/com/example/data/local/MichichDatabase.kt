package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DownloadedAddonEntity::class,
        SavedSkinEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class MichichDatabase : RoomDatabase() {
    abstract fun launcherDao(): LauncherDao

    companion object {
        @Volatile
        private var INSTANCE: MichichDatabase? = null

        fun getInstance(context: Context): MichichDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MichichDatabase::class.java,
                    "michich_mods_hub.db",
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
