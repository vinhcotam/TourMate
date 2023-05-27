package com.example.tourmate.model

import java.io.Serializable
import kotlin.properties.Delegates

data class ViewSavedPlace(
    var saved_place_id: Int,
    var uid: String,
    var location_id: Int,
    var location: String,
    var city_id: String,
    var latitude: String,
    var longtitude: String,
    var vote_average: String,
    var vote_count: Int,
    var image_url: String,
    var english_name: String,
    var min_hour: Double,
    var max_hour: Double
)




