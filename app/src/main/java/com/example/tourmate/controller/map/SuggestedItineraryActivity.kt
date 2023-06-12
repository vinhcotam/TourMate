package com.example.tourmate.controller.map

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tourmate.R
import com.example.tourmate.base.BaseActivity
import com.example.tourmate.databinding.ActivitySuggestedItineraryBinding
import com.example.tourmate.model.DirectionsResponse
import com.example.tourmate.model.ErrorResponse
import com.example.tourmate.model.ResultDistanceAndDuration
import com.example.tourmate.model.ViewSavedPlace
import com.example.tourmate.network.MapsApiService
import com.example.tourmate.network.RetrofitInstance
import com.example.tourmate.utilities.Constants.Companion.DIRECTION_API_KEY
import com.example.tourmate.view.ResultDistanceAndDurationAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.maps.android.PolyUtil
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.sql.Date.valueOf
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.random.Random
import java.time.LocalDate

class SuggestedItineraryActivity : BaseActivity(), OnMapReadyCallback {
    private val binding by lazy {
        ActivitySuggestedItineraryBinding.inflate(layoutInflater)
    }
    private var suggestList = ArrayList<ViewSavedPlace>()
    private var resultList = ArrayList<ResultDistanceAndDuration>()
    private lateinit var resultDistanceAndDurationAdapter: ResultDistanceAndDurationAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //    private var progressDialog: MaterialDialog? = null
    private lateinit var jsonString: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
//        progressDialog = MaterialDialog(this).apply {
//            title(text = "Loading")
//            message(text = "Please wait...")
//            cancelable(false)
//            show()
//        }
        setSupportActionBar(binding.toolbar)
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navigationView.setNavigationItemSelectedListener(this)
        auth = FirebaseAuth.getInstance()
        suggestList = ArrayList()
        resultList = ArrayList()
        resultDistanceAndDurationAdapter = ResultDistanceAndDurationAdapter(resultList)
        binding.recycleViewResult.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recycleViewResult.adapter = resultDistanceAndDurationAdapter
        jsonString = intent.getStringExtra("myList").toString()
        val gson = Gson()
        val type = object : TypeToken<ArrayList<ViewSavedPlace>>() {}.type
        suggestList = gson.fromJson(jsonString, type)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.direction_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        var position = 0
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        googleMap.isMyLocationEnabled = true
        googleMap.setPadding(300, 300, 0, 0)
        googleMap.uiSettings.isZoomControlsEnabled = true
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

//                    .icon(i.image_url as BitmapDescriptor)

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
//        progressDialog?.dismiss()
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
                            val durationValue = directionsResponse.routes[0].legs[0].duration.value
                            val result = ResultDistanceAndDuration(
                                start.english_name,
                                end.english_name,
                                durationText,
                                distanceText,
                                distanceValue,
                                durationValue
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

                            Log.d("tottttt", sortedList.toString())
                            resultDistanceAndDurationAdapter =
                                ResultDistanceAndDurationAdapter(sortedList)
                            binding.recycleViewResult.layoutManager =
                                LinearLayoutManager(
                                    this@SuggestedItineraryActivity,
                                    LinearLayoutManager.HORIZONTAL,
                                    false
                                )
//                            val randomColor = randomColor()
                            binding.recycleViewResult.adapter = resultDistanceAndDurationAdapter
                            val polylineOptions = PolylineOptions()
                                .color((getColor(R.color.dark_blue)))
                                .width(15f)
                            for (step in route.legs[0].steps) {
                                val points = PolyUtil.decode(step.polyline.points)
                                for (point in points) {
                                    polylineOptions.add(LatLng(point.latitude, point.longitude))
                                }
                            }
                            googleMap.addPolyline(polylineOptions)
//                            progressDialog?.dismiss()

                        }
                    }
                }

                override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                    Toast.makeText(
                        this@SuggestedItineraryActivity,
                        t.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
//                    progressDialog?.dismiss()

                }

            })
        }

    }

    private fun randomColor(): Int {
        val random = Random
        val r = random.nextInt(156) + 100
        val g = random.nextInt(156) + 100
        val b = random.nextInt(156) + 100
        return (255 shl 24) or (r shl 16) or (g shl 8) or b
    }

    override fun onBackPressed() {
        suggestList.clear()
        finish()
    }

    fun savePlan(view: View) {
        when (view) {
            binding.buttonSavePlan -> {
                insertHistory()
                binding.buttonSavePlan.isEnabled = false
            }
        }
    }

    private fun insertHistory() {
        val randomNumber = Random.nextInt(0, 10000)
        val api = RetrofitInstance.api
        val currentDate = valueOf(LocalDate.now().toString())
        val englishNameBuilder = StringBuilder()
        for (savedPlace in suggestList) {
            if(savedPlace.location_id!=0){
                englishNameBuilder.append(savedPlace.english_name).append("-")
            }
        }

        if (englishNameBuilder.isNotEmpty()) {
            englishNameBuilder.delete(englishNameBuilder.length - 1, englishNameBuilder.length)
        }

        val concatenatedEnglishName = englishNameBuilder.toString()
        Log.d("Adsadfgg", concatenatedEnglishName)
        val call = api.insertHistory(
            id = randomNumber,
            uid = auth.uid ?: "",
            itinerary = englishNameBuilder.toString(),
            date = currentDate.toString()
        )
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    val responseData = response.body()?.string()
                    Log.d("resssss", response.toString())
                    if (responseData != null && responseData == "success") {
                        Toast.makeText(
                            this@SuggestedItineraryActivity,
                            getString(R.string.success),
                            Toast.LENGTH_LONG
                        ).show()
                        insertDetailHistory(randomNumber, suggestList)

                        deleteSavePlace()
                    } else {
                        Toast.makeText(
                            this@SuggestedItineraryActivity,
                            getString(R.string.fail),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    val error = Gson().fromJson(
                        response.errorBody()?.string(),
                        ErrorResponse::class.java
                    )
                    val errorMessage = error?.message ?: getString(R.string.unknown_error)
                    Toast.makeText(
                        this@SuggestedItineraryActivity,
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                if (t is IOException) {
                    Toast.makeText(
                        this@SuggestedItineraryActivity,
                        getString(R.string.cant_connect_to_server),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this@SuggestedItineraryActivity,
                        getString(R.string.unknown_error),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        })
    }
    private fun insertDetailHistory(randomNumber: Int, suggestList: ArrayList<ViewSavedPlace>) {
        Log.d("Dafgh", randomNumber.toString())

        for (i in 1 until  suggestList.size){
            var randomNumberDetail = Random.nextInt(0, 10000)
            Log.d("Dafgh", suggestList[i].location_id.toString())

            val api = RetrofitInstance.api
            val call = api.insertDetailHistory(
                id = randomNumberDetail,
                uid = auth.uid ?: "",
                history_id = randomNumber,
                location_id = suggestList[i].location_id
            )
            call.enqueue(object: Callback<ResponseBody>{
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        Log.d("Dafgh", response.toString())
                        val responseData = response.body()?.string()
                        if (responseData != null && responseData == "success") {
                            Log.d("Dafgh", getString(R.string.success))

//                            Toast.makeText(
//                                this@OsmActivity,
//                                getString(R.string.success),
//                                Toast.LENGTH_LONG
//                            ).show()
                        } else {
                            Log.d("Dafgh", getString(R.string.fail))
//
//                            Toast.makeText(
//                                this@OsmActivity,
//                                getString(R.string.fail),
//                                Toast.LENGTH_LONG
//                            ).show()
                        }
                    } else {
                        val error = Gson().fromJson(
                            response.errorBody()?.string(),
                            ErrorResponse::class.java
                        )
                        val errorMessage = error?.message ?: getString(R.string.unknown_error)
                        Toast.makeText(
                            this@SuggestedItineraryActivity,
                            errorMessage,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    if (t is IOException) {
                        Toast.makeText(
                            this@SuggestedItineraryActivity,
                            getString(R.string.cant_connect_to_server),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this@SuggestedItineraryActivity,
                            getString(R.string.unknown_error),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            })
        }

    }

    fun deleteSavePlace(){
        val api = RetrofitInstance.api
        val call = api.deleteSavedPlace(
            uid = auth.uid ?: ""
        )
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    val responseData = response.body()?.string()
                    if (responseData != null && responseData == "success") {
                        Toast.makeText(
                            this@SuggestedItineraryActivity,
                            getString(R.string.success),
                            Toast.LENGTH_LONG
                        ).show()

                    } else {
                        Toast.makeText(
                            this@SuggestedItineraryActivity,
                            getString(R.string.fail),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    val error = Gson().fromJson(
                        response.errorBody()?.string(),
                        ErrorResponse::class.java
                    )
                    val errorMessage = error?.message ?: getString(R.string.unknown_error)
                    Toast.makeText(
                        this@SuggestedItineraryActivity,
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                if (t is IOException) {
                    Toast.makeText(
                        this@SuggestedItineraryActivity,
                        getString(R.string.cant_connect_to_server),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this@SuggestedItineraryActivity,
                        getString(R.string.unknown_error),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
    }
}