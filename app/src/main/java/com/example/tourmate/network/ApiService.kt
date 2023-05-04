package com.example.tourmate.network

import com.example.tourmate.model.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface ApiService {
    @GET("getDataLocation.php")
    fun getLocations(): Call<List<DataLocation>>

    @GET("getDataLocationByCityId.php")
    fun getLocationsByCity(@Query("city_id") cityId: String): Call<List<DataLocation>>

    @GET("getDataCity.php")
    fun getDataCity(): Call<List<DataCity>>

    @GET("getNearbyWithLocationId.php")
    fun getNearbyWithLocationId(@Query("location_id") locationId: String): Call<List<DataNearby>>

    @GET("getDetailLocationById.php")
    fun getDetailLocationById(@Query("id") id: Int): Call<List<DataLocation>>

    @GET("insertFavoriteList.php")
    fun insertFavoriteList(
        @Query("id") id: Int,
        @Query("uid") uid: String,
        @Query("location_id") locationId: Int
    ): Call<ResponseBody>

    @GET("getFavoriteListByUid.php")
    fun getFavoriteListByUid(@Query("uid") uid: String): Call<List<FavoriteList>>

    @GET("getFavoriteListByUidd.php")
    fun getFavoriteListByUidd(@Query("uid") uid: String): Call<List<ViewFavoriteLocation>>

    @GET("deleteFavoriteListByUid.php")
    fun deleteFavoriteListByUid(
        @Query("uid") uid: String,
        @Query("location_id") locationId: Int
    ): Call<ResponseBody>

    @GET("getSavedPlaceByUid.php")
    fun getSavedPlaceByUid(@Query("uid") uid: String): Call<List<SavedPlace>>
    @GET("getViewSavedLocationByUid.php")
    fun getViewSavedLocationByUid(@Query("uid") uid: String): Call<List<ViewSavedPlace>>
    @GET("insertSavedPlaceList.php")
    fun insertSavedPlaceList(
        @Query("id") id: Int,
        @Query("uid") uid: String,
        @Query("location_id") locationId: Int
    ): Call<ResponseBody>

    @GET("deleteSavedPlaceListByUid.php")
    fun deleteSavedPlaceListByUid(
        @Query("uid") uid: String,
        @Query("location_id") locationId: Int
    ): Call<ResponseBody>

    @GET("getFavoriteListByUidAndLocationId.php")
    fun getFavoriteListByUidAndLocationId(
        @Query("uid") uid: String,
        @Query("location_id") location_id: Int
    ): Call<List<FavoriteList>>
    @GET("getSavedPlaceListByUidAndLocationId.php")
    fun getSavedPlaceListByUidAndLocationId(
        @Query("uid") uid: String,
        @Query("location_id") location_id: Int
    ): Call<List<SavedPlace>>
    @GET("recommendRequest.php")
    fun recommendRequest(@Query("name") name: String): Call<Unit>
    @GET("receivedRecommendFromPython.php")
    fun getRecommend(): Call<List<String>>
    @GET("getDistanceByStartAndEndLocationId.php")
    fun getDistanceByStartAndEndLocationId(
        @Query("location_start_id") location_start_id: Int,
        @Query("location_end_id") location_end_id: Int
    ): Call<List<DistanceClass>>
    @GET("insertDistanceData.php")
    fun insertDistanceData(
        @Query("location_start_id") location_start_id: Int,
        @Query("location_end_id") location_end_id: Int,
        @Query("distance") distance: Double
    ): Call<ResponseBody>
}
