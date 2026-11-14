package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ModrinthSearchResponse(
    val hits: List<ModrinthHit> = emptyList(),
    val offset: Int = 0,
    val limit: Int = 20,
    @Json(name = "total_hits") val totalHits: Int = 0,
)

@JsonClass(generateAdapter = true)
data class ModrinthHit(
    @Json(name = "project_id") val projectId: String,
    val slug: String = "",
    val title: String = "",
    val description: String = "",
    val categories: List<String> = emptyList(),
    @Json(name = "project_type") val projectType: String = "mod",
    val downloads: Long = 0L,
    val follows: Int = 0,
    @Json(name = "icon_url") val iconUrl: String? = null,
    val author: String = "",
    val versions: List<String> = emptyList(),
    @Json(name = "latest_version") val latestVersion: String? = null,
    val gallery: List<String> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class ModrinthVersion(
    val id: String,
    @Json(name = "project_id") val projectId: String,
    val name: String = "",
    @Json(name = "version_number") val versionNumber: String = "",
    @Json(name = "game_versions") val gameVersions: List<String> = emptyList(),
    val loaders: List<String> = emptyList(),
    val files: List<ModrinthFile> = emptyList(),
    @Json(name = "date_published") val datePublished: String = "",
)

@JsonClass(generateAdapter = true)
data class ModrinthFile(
    val url: String,
    val filename: String,
    val primary: Boolean = true,
    val size: Long = 0L,
)

@JsonClass(generateAdapter = true)
data class MojangProfileResponse(
    val id: String,
    val name: String,
)

@JsonClass(generateAdapter = true)
data class MojangSessionProfile(
    val id: String,
    val name: String,
    val properties: List<MojangProperty> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class MojangProperty(
    val name: String,
    val value: String,
)
