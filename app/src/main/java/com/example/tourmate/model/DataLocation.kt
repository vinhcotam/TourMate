package com.example.tourmate.model
import kotlin.math.ln
import kotlin.math.sqrt

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
    var max_hour: Double,
    var image_url: String,
    var description: String,
    var english_name: String,

)
