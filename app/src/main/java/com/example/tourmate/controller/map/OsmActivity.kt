package com.example.tourmate.controller.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tourmate.R
import com.example.tourmate.base.BaseActivity
import com.example.tourmate.databinding.ActivityOsmBinding
import com.example.tourmate.model.DistanceClass
import com.example.tourmate.model.ErrorResponse
import com.example.tourmate.model.ResultDistanceAndDuration
import com.example.tourmate.model.ViewSavedPlace
import com.example.tourmate.network.RetrofitInstance
import com.example.tourmate.view.ResultDistanceAndDurationAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import org.osgeo.proj4j.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.sql.Date
import java.time.LocalDate
import kotlin.math.roundToLong
import kotlin.random.Random

class OsmActivity : BaseActivity() {
    private val binding by lazy {
        ActivityOsmBinding.inflate(layoutInflater)
    }
    private var distanceList = ArrayList<DistanceClass>()
    private var suggestList = ArrayList<ViewSavedPlace>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var auth: FirebaseAuth
    private var resultList = ArrayList<ResultDistanceAndDuration>()
    private lateinit var resultDistanceAndDurationAdapter: ResultDistanceAndDurationAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, getPreferences(MODE_PRIVATE))
        setContentView(binding.root)
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
        binding.mapView.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapView.setMultiTouchControls(true)
        binding.mapView.controller.setZoom(15.0)
        auth = FirebaseAuth.getInstance()
        suggestList = ArrayList()
        distanceList = ArrayList()
        resultList = ArrayList()
        resultDistanceAndDurationAdapter = ResultDistanceAndDurationAdapter(resultList)
        binding.recycleViewResult.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recycleViewResult.adapter = resultDistanceAndDurationAdapter
        val jsonStringSuggestList = intent.getStringExtra("myList")
        val jsonStringDistanceList = intent.getStringExtra("distanceList")
        val gson = Gson()
        val typeSuggestList = object : TypeToken<ArrayList<ViewSavedPlace>>() {}.type
        val typeDistanceList = object : TypeToken<ArrayList<DistanceClass>>() {}.type
        suggestList = gson.fromJson(jsonStringSuggestList, typeSuggestList)
        distanceList = gson.fromJson(jsonStringDistanceList, typeDistanceList)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            updateLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_LOCATION
            )
        }
    }

    private fun updateLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val currentLocation = GeoPoint(location.latitude, location.longitude)
                        val currentCoordinate =
                            convertCoordinates(currentLocation.latitude, currentLocation.longitude)
                        val currentMarker = Marker(binding.mapView)
                        currentMarker.position = currentCoordinate
                        currentMarker.title = "Your Location"
                        binding.mapView.overlays.add(currentMarker)
                        val locations = suggestList.map { savedPlace ->
                            GeoPoint(
                                savedPlace.latitude.toDouble(),
                                savedPlace.longtitude.toDouble()
                            )
                        }
                        GlobalScope.launch(Dispatchers.Main) {
                            val waypoints = calculateWaypoints(locations)
                            drawRouteAndMarkers(waypoints)
                        }
                    }
                }
        }
    }

    private fun calculateWaypoints(
        locations: List<GeoPoint>
    ): List<GeoPoint> {
        val waypoints = mutableListOf<GeoPoint>()
        for (location in locations) {
            waypoints.add(location)
        }
        return waypoints
    }

    private fun drawRouteAndMarkers(waypoints: List<GeoPoint>) {
        for (i in suggestList) {
            val coordinate = convertCoordinates(i.latitude.toDouble(), i.longtitude.toDouble())

            val marker = Marker(binding.mapView)
//            marker.position = coordinate

            marker.position = GeoPoint(i.latitude.toDouble(), i.longtitude.toDouble())
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.title = i.english_name
            binding.mapView.overlays.add(marker)
            Log.d("marerrrr", marker.title.toString())
        }

        val polyline = Polyline()
        polyline.color = Color.BLUE
        polyline.width = 5.0f
        polyline.setPoints(waypoints)
        binding.mapView.overlays.add(polyline)

        GlobalScope.launch(Dispatchers.Main) {
            for (i in 0 until suggestList.size - 1) {
                val startLocation = GeoPoint(
                    suggestList[i].latitude.toDouble(),
                    suggestList[i].longtitude.toDouble()
                )
                val endLocation = GeoPoint(
                    suggestList[i + 1].latitude.toDouble(),
                    suggestList[i + 1].longtitude.toDouble()
                )
                val time = getTimeBetweenLocations(startLocation, endLocation)
                val distance = getDistanceBetweenLocations(startLocation, endLocation)
                val result = ResultDistanceAndDuration(
                    start_name = suggestList[i].english_name,
                    end_name = suggestList[i + 1].english_name,
                    duration = time,
                    distance = distance,
                    distanceValue = 0,
                    durationValue = 0
                )
                resultList.add(result)
            }

            resultDistanceAndDurationAdapter.notifyDataSetChanged()

            binding.mapView.zoomToBoundingBox(polyline.bounds, true, 100)
            binding.mapView.invalidate()

        }
    }

    private suspend fun getTimeBetweenLocations(
        startLocation: GeoPoint,
        endLocation: GeoPoint
    ): String {
        val startLat = startLocation.latitude
        val startLon = startLocation.longitude
        val endLat = endLocation.latitude
        val endLon = endLocation.longitude

        val url =
            "https://router.project-osrm.org/route/v1/driving/$startLon,$startLat;$endLon,$endLat?overview=false"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                val responseData = response.body?.string()
                if (response.isSuccessful && responseData != null) {
                    val json = JSONObject(responseData)
                    val routes = json.getJSONArray("routes")
                    if (routes.length() > 0) {
                        val route = routes.getJSONObject(0)
                        val legs = route.getJSONArray("legs")
                        if (legs.length() > 0) {
                            val leg = legs.getJSONObject(0)
                            val durationValue = leg.getDouble("duration")
                            val durationInMinutes = (durationValue / 60).roundToLong()
                            return@withContext "$durationInMinutes minutes"
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return@withContext ""
        }
    }

    private suspend fun getDistanceBetweenLocations(
        startLocation: GeoPoint,
        endLocation: GeoPoint
    ): String {
        val startLat = startLocation.latitude
        val startLon = startLocation.longitude
        val endLat = endLocation.latitude
        val endLon = endLocation.longitude

        val url =
            "https://router.project-osrm.org/route/v1/driving/$startLon,$startLat;$endLon,$endLat?overview=false"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                val responseData = response.body?.string()
                if (response.isSuccessful && responseData != null) {
                    val json = JSONObject(responseData)
                    val routes = json.getJSONArray("routes")
                    if (routes.length() > 0) {
                        val route = routes.getJSONObject(0)
                        val legs = route.getJSONArray("legs")
                        if (legs.length() > 0) {
                            val leg = legs.getJSONObject(0)
                            val distanceValue = leg.getDouble("distance")
                            val distanceInKm = (distanceValue / 1000).roundToLong()

                            return@withContext "$distanceInKm km"
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return@withContext ""
        }
    }


    private fun convertCoordinates(lat: Double, lon: Double): GeoPoint {
        val crsFactory = CRSFactory()
        val fromCRS: CoordinateReferenceSystem = crsFactory.createFromName("EPSG:4326")
        val toCRS: CoordinateReferenceSystem = crsFactory.createFromName("EPSG:3857")

        val transformFactory = CoordinateTransformFactory()
        val forwardTransform = transformFactory.createTransform(fromCRS, toCRS)
        val inverseTransform = transformFactory.createTransform(toCRS, fromCRS)
        val sourceCoord = ProjCoordinate(lon, lat)
        val targetCoord = ProjCoordinate()

        forwardTransform.transform(sourceCoord, targetCoord)

        val clampedLon = targetCoord.y.coerceIn(-180.0, 180.0)
        val clampedLat = targetCoord.x.coerceIn(-85.05112877980658, 85.05112877980658)

        return GeoPoint(clampedLat, clampedLon)

    }




    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateLocation()
            }
        }
    }


    fun osmButton(view: View) {
        when (view) {
            binding.buttonSavePlan -> {
                insertHistory()
                binding.buttonSavePlan.isEnabled = false
            }
            binding.locationButton -> {
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            val currentLocation = GeoPoint(location.latitude, location.longitude)
                            binding.mapView.controller.setCenter(currentLocation)
                            binding.mapView.controller.setZoom(15.0)
                        }
                    }
            }
        }
    }

    private fun insertHistory() {
        val randomNumber = Random.nextInt(0, 10000)
        val api = RetrofitInstance.api
        val currentDate = Date.valueOf(LocalDate.now().toString())
        val englishNameBuilder = StringBuilder()
        for (savedPlace in suggestList) {
            if (savedPlace.location_id != 0) {
                englishNameBuilder.append(savedPlace.english_name).append("-")
            }
        }

        if (englishNameBuilder.isNotEmpty()) {
            englishNameBuilder.delete(englishNameBuilder.length - 1, englishNameBuilder.length)
        }

        val concatenatedEnglishName = englishNameBuilder.trim().toString()
        val call = api.insertHistory(
            id = randomNumber,
            uid = auth.uid ?: "",
            itinerary = concatenatedEnglishName,
            date = currentDate.toString()
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
                            this@OsmActivity,
                            getString(R.string.success),
                            Toast.LENGTH_LONG
                        ).show()
                        insertDetailHistory(randomNumber, suggestList)
                        deleteSavePlace()
                    } else {
                        Toast.makeText(
                            this@OsmActivity,
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
                        this@OsmActivity,
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                if (t is IOException) {
                    Toast.makeText(
                        this@OsmActivity,
                        getString(R.string.cant_connect_to_server),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this@OsmActivity,
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
                            this@OsmActivity,
                            errorMessage,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    if (t is IOException) {
                        Toast.makeText(
                            this@OsmActivity,
                            getString(R.string.cant_connect_to_server),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this@OsmActivity,
                            getString(R.string.unknown_error),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            })
        }

    }

    fun deleteSavePlace() {
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
                            this@OsmActivity,
                            getString(R.string.success),
                            Toast.LENGTH_LONG
                        ).show()

                    } else {
                        Toast.makeText(
                            this@OsmActivity,
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
                        this@OsmActivity,
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                if (t is IOException) {
                    Toast.makeText(
                        this@OsmActivity,
                        getString(R.string.cant_connect_to_server),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this@OsmActivity,
                        getString(R.string.unknown_error),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    companion object {
        private const val PERMISSIONS_REQUEST_LOCATION = 123
    }
}
