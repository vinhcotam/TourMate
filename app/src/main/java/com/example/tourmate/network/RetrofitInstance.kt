package com.example.tourmate.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
//            private const val BASE_URL = "http://192.168.1.7/TourMateServer/"
//    private const val BASE_URL = "https://tourrrmateee.000webhostapp.com/TourMateServer/"

    private const val BASE_URL = "http://192.168.16.110/TourMateServer/"
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
