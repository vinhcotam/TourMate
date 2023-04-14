package com.example.tourmate.network

import com.example.tourmate.model.DataCity
import com.example.tourmate.model.DataLocation
import retrofit2.Call

import retrofit2.http.GET
import retrofit2.http.Query


interface ApiService {
    @GET("getDataLocation.php")
    fun getLocations(): Call<List<DataLocation>>

    @GET("getDataLocationByCityId.php")
    fun getLocationsByCity(@Query("city_id") cityId: String): Call<List<DataLocation>>
    @GET("getDataCity.php")
    fun getDataCity():Call<List<DataCity>>
}
