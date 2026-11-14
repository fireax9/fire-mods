package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_addons")
data class DownloadedAddonEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val title: String,
    val category: String,       // MOD, MODPACK, RESOURCE_PACK, SHADER
    val edition: String,        // JAVA, BEDROCK
    val source: String,         // MODRINTH, MCPEDL, LOCAL
    val versionName: String,
    val fileName: String,
    val filePath: String,
    val iconUrl: String = "",
    val loaderOrFormat: String = "",
    val sizeBytes: Long = 0L,
    val installedAt: Long = System.currentTimeMillis(),
    val status: String = "READY", // DOWNLOADING, READY, FAILED
    val progress: Int = 100,
)

@Entity(tableName = "saved_skins")
data class SavedSkinEntity(
    @PrimaryKey val id: String,
    val name: String,
    val modelType: String,      // CLASSIC, SLIM
    val source: String,         // BUILTIN, MOJANG, IMPORTED, EDITED
    val filePath: String,
    val isSelected: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
)
