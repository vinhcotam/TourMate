package com.example.tourmate.model

data class DirectionsResponse(
    val routes: List<Route>
)

data class Route(
    val legs: List<Leg>
)

data class Leg(
    val distance: Distance,
    val duration: Duration,
    val steps: List<Step>
)

data class Step(
    val distance: Distance,
    val duration: Duration,
    val start_location: LocationClass,
    val end_location: LocationClass,
    val polyline: Polyline,
    val travel_mode: String
)

data class Distance(
    val text: String,
    val value: Int
)

data class Duration(
    val text: String,
    val value: Int
)

data class Location(
    val lat: Double,
    val lng: Double
)

data class Polyline(
    val points: String
)
