package com.example.tourmate.model

import com.google.gson.annotations.SerializedName

data class DistanceMatrixResponse(
    val resourceSets: List<ResourceSet>
)

data class ResourceSet(
    val resources: List<Resource>
)

data class Resource(
    val results: List<Result>
)

data class Result(
    @SerializedName("travelDistance")
    val distance: Double
)
