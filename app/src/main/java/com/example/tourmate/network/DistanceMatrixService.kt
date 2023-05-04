package com.example.tourmate.network

import com.example.tourmate.model.DistanceMatrixResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface DistanceMatrixService {
    @GET("/REST/v1/Routes/DistanceMatrix")
    fun getDistanceMatrix(
        @Query("origins") origins: String,
        @Query("destinations") destinations: String,
        @Query("travelMode") travelMode: String,
        @Query("key") key: String
    ): Call<DistanceMatrixResponse>
}
