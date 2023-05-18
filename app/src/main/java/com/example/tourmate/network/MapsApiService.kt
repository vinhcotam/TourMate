package com.example.tourmate.network

import com.example.tourmate.model.DirectionsResponse
import com.example.tourmate.model.DistanceMatrixResponse
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MapsApiService {

    @GET("interpreter")
    fun getNearby(@Query("data") data: String): Call<JsonObject>
    @GET("/maps/api/directions/json")
    fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") apiKey: String
    ): Call<DirectionsResponse>
    @GET("/REST/v1/Routes/DistanceMatrix")
    fun getDistanceMatrix(
        @Query("origins") origins: String,
        @Query("destinations") destinations: String,
        @Query("travelMode") travelMode: String,
        @Query("key") key: String
    ): Call<DistanceMatrixResponse>
}

