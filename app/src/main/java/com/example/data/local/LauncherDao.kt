package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LauncherDao {

    @Query("SELECT * FROM downloaded_addons ORDER BY installedAt DESC")
    fun observeAddons(): Flow<List<DownloadedAddonEntity>>

    @Query("SELECT * FROM downloaded_addons WHERE id = :id LIMIT 1")
    suspend fun getAddonById(id: String): DownloadedAddonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAddon(addon: DownloadedAddonEntity)

    @Query("DELETE FROM downloaded_addons WHERE id = :id")
    suspend fun deleteAddonById(id: String)

    @Query("SELECT * FROM saved_skins ORDER BY isSelected DESC, updatedAt DESC")
    fun observeSkins(): Flow<List<SavedSkinEntity>>

    @Query("SELECT * FROM saved_skins")
    suspend fun getSkinsOnce(): List<SavedSkinEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSkin(skin: SavedSkinEntity)

    @Query("UPDATE saved_skins SET isSelected = 0")
    suspend fun clearSelectedSkins()

    @Query("UPDATE saved_skins SET isSelected = 1 WHERE id = :id")
    suspend fun markSkinSelected(id: String)

    @Query("DELETE FROM saved_skins WHERE id = :id")
    suspend fun deleteSkinById(id: String)
}
