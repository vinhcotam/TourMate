package com.example.tourmate.network

import com.example.tourmate.model.NearbyLocation
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BingMapsApi {
    @GET("Locations/{query}")
    fun getLocation(
        @Path("query") query: String,
        @Query("includeNeighborhood") includeNeighborhood: Int = 0,
        @Query("o") format: String = "json",
        @Query("key") apiKey: String
    ): Response<JsonElement>
    @GET("interpreter")
    fun getNearby(@Query("data") data: String): Call<JsonObject>
}

