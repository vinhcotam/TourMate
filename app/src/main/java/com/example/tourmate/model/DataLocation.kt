package com.example.tourmate.model

data class DataLocation(
    var id: Int,
    var name: String,
    var location: String,
    var latitude: String,
    var longtitude: String,
    var city_id: String,
    var vote_average: String,
    var vote_count: Int,
    var min_hour: Double,
    var max_hour: Double
)