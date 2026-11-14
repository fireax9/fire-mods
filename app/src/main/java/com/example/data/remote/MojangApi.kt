package com.example.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface MojangApi {
    @GET("https://api.mojang.com/users/profiles/minecraft/{username}")
    suspend fun getUuidByUsername(
        @Path("username") username: String,
    ): MojangProfileResponse

    @GET("https://sessionserver.mojang.com/session/minecraft/profile/{uuid}")
    suspend fun getSessionProfile(
        @Path("uuid") uuid: String,
    ): MojangSessionProfile
}
