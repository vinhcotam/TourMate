package com.example.tourmate.network

import com.example.tourmate.model.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface ApiService {
    @GET("getDataLocation.php")
    fun getLocations(): Call<List<DataLocation>>

    @GET("getDataLocationByCityId.php")
    fun getLocationsByCity(@Query("city_id") cityId: String): Call<List<DataLocation>>

    @GET("getDataCity.php")
    fun getDataCity(): Call<List<DataCity>>

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
    fun recommendRequest(@Query("english_name") english_name: String): Call<Unit>

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

    @GET("getTop10Location.php")
    fun getTop10Locations(): Call<List<DataLocation>>

    @GET("insertHistoryByUid.php")
    fun insertHistory(
        @Query("id") id: Int,
        @Query("uid") uid: String,
        @Query("itinerary") itinerary: String,
        @Query("date") date: String
    ): Call<ResponseBody>
    @GET("getHistoryByUid.php")
    fun getHistoryByUid(
        @Query("uid") uid: String
    ): Call<List<History>>
    @GET("deleteHistoryByUid.php")
    fun deleteHistory(
        @Query("uid") uid: String,
        @Query("id") id: Int
    ): Call<ResponseBody>

    @GET("deleteSavePlace.php")
    fun deleteSavedPlace(
        @Query("uid") uid: String
    ): Call<ResponseBody>

    @GET("insertDetailHistory.php")
    fun insertDetailHistory(
        @Query("id") id: Int,
        @Query("uid") uid: String,
        @Query("history_id") history_id: Int,
        @Query("location_id") location_id: Int
    ): Call<ResponseBody>
    @GET("deleteDetailHistory.php")
    fun deleteDetailHistory(
        @Query("uid") uid: String,
        @Query("history_id") history_id: Int
    ): Call<ResponseBody>
}
