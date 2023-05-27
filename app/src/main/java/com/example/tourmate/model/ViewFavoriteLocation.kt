package com.example.tourmate.model

data class ViewFavoriteLocation(
    var favorite_id: Int,
    var uid: String,
    var location_id: Int,
    var location: String,
    var city_id: String,
    var vote_average: String,
    var vote_count: Int,
    var image_url: String,
    var english_name: String
)
