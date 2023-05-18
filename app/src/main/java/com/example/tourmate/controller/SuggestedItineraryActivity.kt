package com.example.tourmate.controller

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.example.tourmate.R
import com.example.tourmate.base.BaseActivity
import com.example.tourmate.databinding.ActivitySuggestedItineraryBinding
import com.example.tourmate.model.DirectionsResponse
import com.example.tourmate.model.ResultDistanceAndDuration
import com.example.tourmate.model.ViewSavedPlace
import com.example.tourmate.network.MapsApiService
import com.example.tourmate.utilities.Constants.Companion.DIRECTION_API_KEY
import com.example.tourmate.view.ResultDistanceAndDurationAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.maps.android.PolyUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class SuggestedItineraryActivity : BaseActivity(), OnMapReadyCallback {
    private val binding by lazy {
        ActivitySuggestedItineraryBinding.inflate(layoutInflater)
    }
    private var suggestList = ArrayList<ViewSavedPlace>()
    private var resultList = ArrayList<ResultDistanceAndDuration>()
    private lateinit var resultDistanceAndDurationAdapter: ResultDistanceAndDurationAdapter

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var progressDialog: MaterialDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        progressDialog = MaterialDialog(this).apply {
            title(text = "Loading")
            message(text = "Please wait...")
            cancelable(false)
            show()
        }
        suggestList = ArrayList()
        resultList = ArrayList()
        resultDistanceAndDurationAdapter = ResultDistanceAndDurationAdapter(resultList)
        binding.recycleViewResult.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recycleViewResult.adapter = resultDistanceAndDurationAdapter
        val jsonString = intent.getStringExtra("myList")
        val gson = Gson()
        val type = object : TypeToken<ArrayList<ViewSavedPlace>>() {}.type
        suggestList.clear()
        suggestList = gson.fromJson(jsonString, type)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.direction_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        var position = 0
        for (i in suggestList) {
            position++
            googleMap.addMarker(
                MarkerOptions().position(
                    LatLng(
                        i.latitude.toDouble(),
                        i.longtitude.toDouble()
                    )
                )
                    .title(i.english_name)
                    .snippet("Step $position")
            )
        }

        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    suggestList[0].latitude.toDouble(),
                    suggestList[0].longtitude.toDouble()
                ), 15f
            )
        )
        drawRoute(suggestList)
    }

    private fun drawRoute(suggestList: ArrayList<ViewSavedPlace>) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        for (i in 0 until suggestList.size - 1) {
            val start = suggestList[i]
            val end = suggestList[i + 1]
            val directionsService = retrofit.create(MapsApiService::class.java)
            val call = directionsService.getDirections(
                "${start.latitude},${start.longtitude}",
                "${end.latitude},${end.longtitude}",
                DIRECTION_API_KEY
            )
            call.enqueue(object : Callback<DirectionsResponse> {
                override fun onResponse(
                    call: Call<DirectionsResponse>,
                    response: Response<DirectionsResponse>
                ) {
                    Log.d("respon111e", response.toString())
                    if (response.isSuccessful) {
                        val directionsResponse = response.body()
                        if (directionsResponse?.routes?.isNotEmpty() == true) {
                            val route = directionsResponse.routes[0]
                            val distanceText = directionsResponse.routes[0].legs[0].distance.text
                            val distanceValue = directionsResponse.routes[0].legs[0].distance.value
                            val durationText = directionsResponse.routes[0].legs[0].duration.text
                            val result = ResultDistanceAndDuration(
                                start.english_name,
                                end.english_name,
                                durationText,
                                distanceText,
                                distanceValue
                            )
                            resultList.add(result)
                            val sortedList = ArrayList<ResultDistanceAndDuration>()
                            val resultMap = HashMap<String, ResultDistanceAndDuration>()
                            for (result in resultList) {
                                resultMap[result.start_name] = result
                            }

                            var currentStartName = "My Location"
                            while (resultMap.containsKey(currentStartName)) {
                                val currentResult = resultMap[currentStartName]
                                sortedList.add(currentResult!!)
                                currentStartName = currentResult.end_name
                            }
                            resultDistanceAndDurationAdapter =
                                ResultDistanceAndDurationAdapter(sortedList)
                            binding.recycleViewResult.layoutManager =
                                LinearLayoutManager(
                                    this@SuggestedItineraryActivity,
                                    LinearLayoutManager.HORIZONTAL,
                                    false
                                )
                            binding.recycleViewResult.adapter = resultDistanceAndDurationAdapter

                            val polylineOptions = PolylineOptions()
                                .color(getColor(R.color.light_blue))
                                .width(15f)
                            for (step in route.legs[0].steps) {
                                val points = PolyUtil.decode(step.polyline.points)
                                for (point in points) {
                                    polylineOptions.add(LatLng(point.latitude, point.longitude))
                                }
                            }

                            googleMap.addPolyline(polylineOptions)
                            progressDialog?.dismiss()

                        }
                    }
                }

                override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                    Toast.makeText(
                        this@SuggestedItineraryActivity,
                        t.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                    progressDialog?.dismiss()

                }

            })
        }
    }


    override fun onBackPressed() {
        suggestList.clear()
        finish()
    }
}