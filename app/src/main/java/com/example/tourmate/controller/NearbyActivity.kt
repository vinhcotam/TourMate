package com.example.tourmate.controller

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.example.tourmate.R
import com.example.tourmate.base.BaseActivity
import com.example.tourmate.databinding.ActivityNearbyBinding
import com.example.tourmate.model.NearbyLocation
import com.example.tourmate.network.MapsApiService
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

class NearbyActivity : BaseActivity(), OnMapReadyCallback {
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
        binding.navigationView.setNavigationItemSelectedListener(this)
        binding.navigationView.menu.findItem(R.id.nearby).isChecked = true
        nearbyLocationList = ArrayList()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(
            R.id.map_fragment
        ) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

    }


    override fun onMapReady(p0: GoogleMap) {
        map = p0
        map!!.isMyLocationEnabled = true
        map!!.setPadding(300, 300, 0, 0)
        map!!.uiSettings.isZoomControlsEnabled = true
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
            binding.chipCafe -> {
                searchNearby(currentLocation!!.latitude, currentLocation!!.longitude, "cafe")
            }
            binding.chipAtm -> {
                searchNearby(currentLocation!!.latitude, currentLocation!!.longitude, "atm")
            }
            binding.chipRestaurant -> {
                searchNearby(currentLocation!!.latitude, currentLocation!!.longitude, "restaurant")
            }
            binding.chipPharmacy -> {
                searchNearby(currentLocation!!.latitude, currentLocation!!.longitude, "pharmacy")
            }
            binding.chipShopping -> {
                searchNearby(currentLocation!!.latitude, currentLocation!!.longitude, "shop")
            }
            binding.chipFuel -> {
                searchNearby(currentLocation!!.latitude, currentLocation!!.longitude, "fuel")
            }
        }
    }

    private fun searchNearby(latitude: Double, longtitude: Double, type: String) {
        nearbyLocationList.clear()
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

        val nearbyApi = retrofit.create(MapsApiService::class.java)
        val request: Call<JsonObject> = if (type == "shop") {
            nearbyApi.getNearby("[out:json];node(around:1000,${latitude},${longtitude})[shop];out;")

        } else {
            nearbyApi.getNearby("[out:json];node(around:1000,${latitude},${longtitude})[amenity=${type}];out;")
        }
        request
            .enqueue(object : Callback<JsonObject> {
                override fun onResponse(
                    call: Call<JsonObject>,
                    response: Response<JsonObject>
                ) {
                    if (response.isSuccessful) {
                        val nodes = response.body()?.getAsJsonArray("elements")
                        Log.d("checkapi", response.toString())
                        if (nodes?.size()!! > 0) {
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
                                    nearbyLocationList.add(nearby)
                                } else {
                                    val operatorJsonElement =
                                        node.getAsJsonObject("tags").get("operator")
                                    if (operatorJsonElement != null) {
                                        val operator = operatorJsonElement.asString
                                        val nearby = NearbyLocation(lat, lon, operator, amenity)
                                        nearbyLocationList.add(nearby)
                                    }
                                }
                                map?.clear()

                                addMarker(nearbyLocationList, type)
                            }
                        } else {
                            Toast.makeText(
                                this@NearbyActivity,
                                getString(R.string.no_data_found),
                                Toast.LENGTH_LONG
                            ).show()
                            progressDialog?.dismiss()
                        }
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Log.d("fail", t.toString())
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
            "fuel" -> {
                R.drawable.ic_fuel
            }
            else -> {
                R.drawable.ic_atm
            }
        }

        map?.addMarker(
            MarkerOptions()
                .position(LatLng(currentLocation!!.latitude, currentLocation!!.longitude))
        )
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation!!, 15f))
        val iconColor = ContextCompat.getColor(this, R.color.fire_brick)
        for (i in nearbyLocationList) {
            map?.addMarker(
                MarkerOptions()
                    .position(LatLng(i.lat, i.lon))
                    .title(i.name)
                    .snippet(type)
                    .icon(bitmapDescriptorFromVector(this, iconMarker, iconColor))
            )
        }

        progressDialog?.dismiss()

    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int, color: Int): BitmapDescriptor? {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
        vectorDrawable.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()

        }
    }

}