package com.example.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ModrinthApi {
    @GET("v2/search")
    suspend fun searchProjects(
        @Query("query") query: String,
        @Query("facets") facets: String,
        @Query("index") index: String = "downloads",
        @Query("limit") limit: Int = 25,
        @Query("offset") offset: Int = 0,
    ): ModrinthSearchResponse

    @GET("v2/project/{id}/version")
    suspend fun getProjectVersions(
        @Path("id") projectId: String,
    ): List<ModrinthVersion>
}
