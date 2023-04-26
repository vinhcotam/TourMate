package com.example.tourmate.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    //        private const val BASE_URL = "http://192.168.1.5/TourMateServer/"
    private const val BASE_URL = "https://tourmateserver.000webhostapp.com/TourMateServer/"

//    private const val BASE_URL = "http://192.168.16.117/TourMateServer/"
//    private const val BASE_URL = "http://192.168.1.167/TourMateServer/"

    private val retrofit by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
