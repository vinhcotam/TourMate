package com.example.tourmate.controller

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.example.tourmate.R
import com.example.tourmate.databinding.ActivityNearbyBinding
import com.example.tourmate.model.NearbyLocation
import com.example.tourmate.network.BingMapsApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NearbyActivity : AppCompatActivity(), OnMapReadyCallback {
    private val binding by lazy {
        ActivityNearbyBinding.inflate(layoutInflater)
    }
    private var map: GoogleMap? = null
    private var currentLocation: LatLng? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var nearbyLocationList = ArrayList<NearbyLocation>()
    private var progressDialog: MaterialDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        nearbyLocationList = ArrayList()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(
            R.id.map_fragment
        ) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

    }


    override fun onMapReady(p0: GoogleMap) {
        map = p0
        val task = fusedLocationClient.lastLocation
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
            return
        }
        task.addOnSuccessListener {
            if (it != null) {
                currentLocation = LatLng(it.latitude, it.longitude)
                map?.addMarker(
                    MarkerOptions()
                        .position(currentLocation!!)
                        .title("My location")
                )
                map?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation!!, 15f))
            }
        }
    }

    fun onClickNearbySearch(view: View) {
        when (view) {
            binding.buttonSearchCafe -> {
                searchNearby(currentLocation!!.latitude, currentLocation!!.longitude, "cafe")
            }
            binding.buttonSearchAtm -> {
                searchNearby(currentLocation!!.latitude, currentLocation!!.longitude, "atm")
            }
            binding.buttonSearchRestaurant -> {
                searchNearby(currentLocation!!.latitude, currentLocation!!.longitude, "restaurant")
            }
            binding.buttonSearchPharmacy -> {
                searchNearby(currentLocation!!.latitude, currentLocation!!.longitude, "pharmacy")
            }
            binding.buttonSearchShop -> {
                searchNearby(currentLocation!!.latitude, currentLocation!!.longitude, "shop")
            }
        }
    }

    private fun searchNearby(latitude: Double, longtitude: Double, type: String) {
        progressDialog = MaterialDialog(this).apply {
            title(text = "Loading")
            message(text = "Please wait...")
            cancelable(false)
            show()
        }
        val retrofit = Retrofit.Builder()
            .baseUrl("https://overpass-api.de/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val nearbyApi = retrofit.create(BingMapsApi::class.java)
        var request: Call<JsonObject>? = if (type == "shop") {
            nearbyApi.getNearby("[out:json];node(around:1500,${latitude},${longtitude})[shop];out;")

        } else {
            nearbyApi.getNearby("[out:json];node(around:1500,${latitude},${longtitude})[amenity=${type}];out;")
        }
        request
            ?.enqueue(object : Callback<JsonObject> {
                override fun onResponse(
                    call: Call<JsonObject>,
                    response: Response<JsonObject>
                ) {
                    if (response.isSuccessful) {
                        val nodes = response.body()?.getAsJsonArray("elements")
                        Log.d("checkapi", response.toString())
                        if (nodes != null) {
                            for (i in 0 until nodes.size()) {
                                val node = nodes.get(i).asJsonObject
                                val lat = node.get("lat").asDouble
                                val lon = node.get("lon").asDouble
                                val nameJsonElement = node.getAsJsonObject("tags").get("name")
                                val name = if (nameJsonElement != null) {
                                    nameJsonElement.asString
                                } else {
                                    ""
                                }
                                val amenityJsonElement = node.getAsJsonObject("tags").get("amenity")
                                val amenity = if (amenityJsonElement != null) {
                                    amenityJsonElement.asString
                                } else {
                                    ""
                                }

                                if (name != "") {
                                    val nearby = NearbyLocation(lat, lon, name, amenity)
                                    nearbyLocationList.clear()
                                    nearbyLocationList.add(nearby)
                                }
//                                else {
//                                    if (amenity == "atm") {
//                                        val operator =
//                                            node.getAsJsonObject("tags").get("operator").asString
//                                        val nearby = NearbyLocation(lat, lon, operator, amenity)
//                                        nearbyLocationList.add(nearby)
//                                    }
//                                }
                                if (!nearbyLocationList.isNullOrEmpty()) {
                                    addMarker(nearbyLocationList, type)
                                }

                                Log.d("neahar", nearbyLocationList.toString())
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Log.d("neahar", t.toString())
                }
            })


    }

    private fun addMarker(nearbyLocationList: ArrayList<NearbyLocation>, type: String) {
        val iconMarker: Int = when (type) {
            "cafe" -> {
                R.drawable.ic_cafe
            }
            "restaurant" -> {
                R.drawable.ic_food
            }
            "pharmacy" -> {
                R.drawable.ic_pharmacy
            }
            "shop" -> {
                R.drawable.ic_shopping
            }
            else -> {
                R.drawable.ic_atm
            }
        }
        for (i in nearbyLocationList) {
            map?.addMarker(
                MarkerOptions()
                    .position(LatLng(i.lat, i.lon))
                    .title(i.name)
                    .snippet(type)
                    .icon(bitmapDescriptorFromVector(this, iconMarker))
            )
        }
        progressDialog?.dismiss()

    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ALPHA_8)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }
}