package com.example.tourmate.controller

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.example.tourmate.R
import com.example.tourmate.controller.interfaces.RecyclerSavedPlaceOnClickListener
import com.example.tourmate.databinding.ActivitySavedPlaceBinding
import com.example.tourmate.model.*
import com.example.tourmate.network.DistanceMatrixService
import com.example.tourmate.network.RetrofitInstance
import com.example.tourmate.utilities.Constants.Companion.BING_MAP_API_KEY
import com.example.tourmate.view.ViewSavedPlaceAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class SavedPlaceActivity : AppCompatActivity(), RecyclerSavedPlaceOnClickListener,
    NavigationView.OnNavigationItemSelectedListener {
    private val binding by lazy {
        ActivitySavedPlaceBinding.inflate(layoutInflater)
    }
    private var savedPlaceList = ArrayList<ViewSavedPlace>()
    private lateinit var auth: FirebaseAuth
    private lateinit var viewSavedPlaceAdapter: ViewSavedPlaceAdapter
    private var distanceList = ArrayList<DistanceClass>()
    private var progressDialog: MaterialDialog? = null
    private var locationList = ArrayList<Location>()
    private var currentLatitude: Double? = 0.0
    private var currentLongtitude: Double? = 0.0
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
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
        binding.navigationView.menu.findItem(R.id.saved_place).isChecked = true
        savedPlaceList = ArrayList()
        locationList = ArrayList()
        progressDialog = MaterialDialog(this).apply {
            title(text = "Loading")
            message(text = "Please wait...")
            cancelable(false)
            show()
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        viewSavedPlaceAdapter = ViewSavedPlaceAdapter(this, savedPlaceList)
        viewSavedPlaceAdapter.setOnItemClickListener(this)
        binding.recycleViewSavedPlace.layoutManager = LinearLayoutManager(this)
        binding.recycleViewSavedPlace.adapter = viewSavedPlaceAdapter
        getData(auth.uid)
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterSavedPlaceList(newText)
                return true
            }
        })
        binding.searchView.setOnCloseListener {
            if (savedPlaceList.isEmpty()) {
                binding.textViewEmptyNotice.visibility = View.VISIBLE
                binding.recycleViewSavedPlace.visibility = View.GONE
            } else {
                binding.textViewEmptyNotice.visibility = View.GONE
                binding.recycleViewSavedPlace.visibility = View.VISIBLE

            }
            runOnUiThread {

                viewSavedPlaceAdapter.notifyDataSetChanged()
            }
            true
        }
    }

    private fun fetchLocation() {
        val task = fusedLocationClient.lastLocation
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }
        task.addOnSuccessListener {
            if (it != null) {
                currentLocation = Location(0, it.latitude, it.longitude)
                currentLatitude=it.latitude
                currentLongtitude = it.longitude
                locationList.add(currentLocation as Location)
                getDataDistance(savedPlaceList)

            }
        }
    }
    private fun filterSavedPlaceList(newText: String?) {
        if (!newText.isNullOrEmpty()) {
            val filteredList = ArrayList<ViewSavedPlace>()
            for (i in savedPlaceList) {
                if (i.english_name.lowercase(Locale.ROOT).contains(newText)) {
                    filteredList.add(i)
                }
            }
            if (filteredList.isEmpty()) {
                binding.textViewEmptyNotice.visibility = View.VISIBLE
                binding.recycleViewSavedPlace.visibility = View.GONE
            } else {
                binding.textViewEmptyNotice.visibility = View.GONE
                binding.recycleViewSavedPlace.visibility = View.VISIBLE
                viewSavedPlaceAdapter.setFilteredList(filteredList)

            }
        } else {
            binding.textViewEmptyNotice.visibility = View.GONE
            binding.recycleViewSavedPlace.visibility = View.VISIBLE
            viewSavedPlaceAdapter.setFilteredList(savedPlaceList)

        }
    }

    private fun getData(uid: String?) {
        val api = RetrofitInstance.api
        if (uid != null) {
            api.getViewSavedLocationByUid(uid)
                .enqueue(object : Callback<List<ViewSavedPlace>> {
                    override fun onResponse(
                        call: Call<List<ViewSavedPlace>>,
                        response: Response<List<ViewSavedPlace>>
                    ) {
                        if (response.isSuccessful) {
                            savedPlaceList.clear()
                            response.body()?.let {
                                savedPlaceList.addAll(it)
                                savedPlaceList.sortBy { it.english_name }
                            }
                            viewSavedPlaceAdapter.notifyDataSetChanged()
                            fetchLocation()
                            if (savedPlaceList.isEmpty()) {
                                binding.textViewEmptyNotice.visibility = View.VISIBLE
                            } else {
                                binding.textViewEmptyNotice.visibility = View.GONE
                            }
                            progressDialog?.dismiss()

                        }
                    }

                    override fun onFailure(call: Call<List<ViewSavedPlace>>, t: Throwable) {
                        Toast.makeText(this@SavedPlaceActivity, t.toString(), Toast.LENGTH_LONG)
                            .show()
                    }

                })
        }
    }

    override fun onItemClick(viewSavedPlace: ViewSavedPlace) {
        val intent = Intent(this, DetailLocationActivity::class.java)
        intent.putExtra("location_id", viewSavedPlace.location_id.toString())
        startActivity(intent)
    }

    private fun deleteFavoriteLocation(viewSavedPlace: ViewSavedPlace) {
        val api = RetrofitInstance.api
        val call = api.deleteSavedPlaceListByUid(
            uid = auth.uid ?: "",
            locationId = viewSavedPlace.location_id
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
                            this@SavedPlaceActivity,
                            getString(R.string.success),
                            Toast.LENGTH_LONG
                        ).show()
                        getData(auth.uid)

                    } else {
                        Toast.makeText(
                            this@SavedPlaceActivity,
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
                        this@SavedPlaceActivity,
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                if (t is IOException) {
                    Toast.makeText(
                        this@SavedPlaceActivity,
                        getString(R.string.cant_connect_to_server),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this@SavedPlaceActivity,
                        getString(R.string.unknown_error),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
    }

    override fun onDeleteItemClick(viewSavedPlace: ViewSavedPlace) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.notice))
        builder.setMessage(getString(R.string.delete_favorite_location))
        builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
            deleteFavoriteLocation(viewSavedPlace)
        }
        builder.setNegativeButton(getString(R.string.no)) { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.favorite -> {
                val intent = Intent(this, MyFavoriteActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.saved_place -> {
                val intent = Intent(this, SavedPlaceActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.log_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            else -> false
        }
    }

    fun insertDistance(startLocation: Location, endLocation: Location) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://dev.virtualearth.net")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val distanceMatrixService = retrofit.create(DistanceMatrixService::class.java)
        Log.d("hecasjfnasdf", startLocation.toString())
        Log.d("hecasjfnasdf", endLocation.toString())
        val origin = "${startLocation.latitude},${startLocation.longtitude}"
        val destination = "${endLocation.latitude},${endLocation.longtitude}"
        val travelMode = "driving"
        val apiKey = BING_MAP_API_KEY

        distanceMatrixService.getDistanceMatrix(
            origins = origin,
            destinations = destination,
            travelMode = travelMode,
            key = apiKey
        ).enqueue(object : Callback<DistanceMatrixResponse> {
            override fun onResponse(
                call: Call<DistanceMatrixResponse>,
                response: Response<DistanceMatrixResponse>
            ) {
                if (response.isSuccessful) {
                    val distanceInMeters =
                        response.body()?.resourceSets?.get(0)?.resources?.get(0)?.results?.get(
                            0
                        )?.distance
                    Log.d(
                        "checkkkkk",
                        "distance " + startLocation.id + "to " + endLocation.id + " " + distanceInMeters.toString()
                    )
                    if (startLocation.id != 0 && endLocation.id != 0) {
                        val api = RetrofitInstance.api
                        val call = distanceInMeters?.let {
                            api.insertDistanceData(
                                location_start_id = startLocation.id,
                                location_end_id = endLocation.id,
                                distance = it
                            )
                        }
                        call?.enqueue(object : Callback<ResponseBody> {
                            override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>
                            ) {
                                if (response.isSuccessful) {
                                    val responseData = response.body()?.string()
                                    if (responseData != null && responseData == "success") {
                                        Log.d("checkkkaaaa", response.toString())
                                    } else {
                                        Toast.makeText(
                                            this@SavedPlaceActivity,
                                            getString(R.string.fail),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    Log.d("checkkkaaaa", responseData.toString())
                                }
                            }
                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            }
                        })
                    }
                } else {
                    Log.e("checkkkkk", "HTTP error code ${response.code()}")
                }

            }

            override fun onFailure(call: Call<DistanceMatrixResponse>, t: Throwable) {
                Log.e("checkkkkk", "Failed to get distance matrix: ${t.message}")
            }
        })
    }
//    fun insertDistance(startLocation: ViewSavedPlace, endLocation: ViewSavedPlace) {
//        val retrofit = Retrofit.Builder()
//            .baseUrl("https://dev.virtualearth.net")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//        val distanceMatrixService = retrofit.create(DistanceMatrixService::class.java)
//
//        val origin = "${startLocation.latitude},${startLocation.longtitude}"
//        val destination = "${endLocation.latitude},${endLocation.longtitude}"
//        val travelMode = "driving"
//        val apiKey = BING_MAP_API_KEY
//
//        distanceMatrixService.getDistanceMatrix(
//            origins = origin,
//            destinations = destination,
//            travelMode = travelMode,
//            key = apiKey
//        ).enqueue(object : Callback<DistanceMatrixResponse> {
//            override fun onResponse(
//                call: Call<DistanceMatrixResponse>,
//                response: Response<DistanceMatrixResponse>
//            ) {
//                if (response.isSuccessful) {
//                    val distanceInMeters =
//                        response.body()?.resourceSets?.get(0)?.resources?.get(0)?.results?.get(
//                            0
//                        )?.distance
//                    Log.d(
//                        "checkkkkk",
//                        "distance " + startLocation.location_id + "to " + endLocation.location_id + " " + distanceInMeters.toString()
//                    )
//                    val api = RetrofitInstance.api
//                    val call = distanceInMeters?.let {
//                        api.insertDistanceData(
//                            location_start_id = startLocation.location_id,
//                            location_end_id = endLocation.location_id,
//                            distance = it
//                        )
//                    }
//                    call?.enqueue(object : Callback<ResponseBody> {
//                        override fun onResponse(
//                            call: Call<ResponseBody>,
//                            response: Response<ResponseBody>
//                        ) {
//                            if (response.isSuccessful) {
//                                val responseData = response.body()?.string()
//                                if (responseData != null && responseData == "success") {
//                                    Log.d("checkkk", response.toString())
//                                } else {
//                                    Toast.makeText(
//                                        this@SavedPlaceActivity,
//                                        getString(R.string.fail),
//                                        Toast.LENGTH_LONG
//                                    ).show()
//                                }
//                                Log.d("checkkk", responseData.toString())
//                            } else {
//                                val error = Gson().fromJson(
//                                    response.errorBody()?.string(),
//                                    ErrorResponse::class.java
//                                )
//                                val errorMessage =
//                                    error?.message ?: getString(R.string.unknown_error)
////                                Toast.makeText(
////                                    this@SavedPlaceActivity,
////                                    errorMessage,
////                                    Toast.LENGTH_LONG
////                                ).show()
//                            }
//                        }
//
//                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
////                            Toast.makeText(
////                                this@SavedPlaceActivity,
////                                t.toString(),
////                                Toast.LENGTH_LONG
////                            ).show()
//                        }
//
//                    })
//                } else {
//                    Log.e("checkkkkk", "HTTP error code ${response.code()}")
//                }
//
//            }
//
//            override fun onFailure(call: Call<DistanceMatrixResponse>, t: Throwable) {
//                Log.e("checkkkkk", "Failed to get distance matrix: ${t.message}")
//            }
//        })
//    }

    private fun getDataDistance(savedPlaceList: List<ViewSavedPlace>) {
        for (viewSavedPlace in savedPlaceList) {
            val location = Location(
                id = viewSavedPlace.location_id,
                latitude = viewSavedPlace.latitude.toDouble(),
                longtitude = viewSavedPlace.longtitude.toDouble()
            )
            locationList.add(location)
        }
        if (distanceList.isNotEmpty()){
            for (i in distanceList){
                if (i.location_start_id !=0 || i.location_end_id !=0){
                    distanceList.clear()
                }
            }
        }
        Log.d("currentlocation", locationList.toString())
        for (i in 0 until locationList.size - 1) {
            for (j in i + 1 until locationList.size) {
                val api = RetrofitInstance.api
                api.getDistanceByStartAndEndLocationId(
                    locationList[i].id,
                    locationList[j].id
                ).enqueue(object : Callback<List<DistanceClass>> {
                    override fun onResponse(
                        call: Call<List<DistanceClass>>,
                        response: Response<List<DistanceClass>>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body()?.isEmpty() == true) {
                                insertDistance(locationList[i], locationList[j])
                            } else {
                                response.body()?.let { distanceList.addAll(it) }
                            }
                            Log.d("checkkkkk", response.body().toString())

                        }
                    }

                    override fun onFailure(call: Call<List<DistanceClass>>, t: Throwable) {
                        Toast.makeText(this@SavedPlaceActivity, t.toString(), Toast.LENGTH_LONG)
                            .show()
                    }
                })
            }
        }
//        for (i in 0 until savedPlaceList.size - 1) {
//            for (j in i + 1 until savedPlaceList.size) {
//                val api = RetrofitInstance.api
//                api.getDistanceByStartAndEndLocationId(
//                    savedPlaceList[i].location_id,
//                    savedPlaceList[j].location_id
//                ).enqueue(object : Callback<List<DistanceClass>> {
//                    override fun onResponse(
//                        call: Call<List<DistanceClass>>,
//                        response: Response<List<DistanceClass>>
//                    ) {
//                        if (response.isSuccessful) {
//                            if (response.body()?.isEmpty() == true) {
//                                insertDistance(savedPlaceList[i], savedPlaceList[j])
//                            } else {
//                                response.body()?.let { distanceList.addAll(it) }
//                            }
//                            Log.d("checkkkkk", response.body().toString())
//
//                        }
//                    }
//
//                    override fun onFailure(call: Call<List<DistanceClass>>, t: Throwable) {
//                        Toast.makeText(this@SavedPlaceActivity, t.toString(), Toast.LENGTH_LONG)
//                            .show()
//                    }
//                })
//            }
//        }


//        for (i in 0 until savedPlaceList.size - 1) {
//            for (j in i + 1 until savedPlaceList.size) {
//                val coroutineScope = CoroutineScope(Dispatchers.Main)
//                coroutineScope.launch(Dispatchers.IO) {
//                    val origins = "${savedPlaceList[i].latitude},${savedPlaceList[i].longtitude}"
//                    val destinations =
//                        "${savedPlaceList[j].latitude},${savedPlaceList[j].longtitude}"
//                    val apiKey = DISTANCE_MATRIX_API_KEY
//                    val urlString =
//                        "https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=$origins&destinations=$destinations&key=$apiKey"
//
//                    val result = URL(urlString).readText()
//                    val jsonObj = JSONObject(result)
//                    Log.d("checkkkkk", urlString)
//                    val rows = jsonObj.getJSONArray("rows")
//                    val elements = rows.getJSONObject(0).getJSONArray("elements")
//                    val distance =
//                        elements.getJSONObject(0).getJSONObject("distance").getString("text")
//                    Log.d("checkkkkk", distance.toString())
//                    Log.d("checkkkkk", "distance " + i + "to " + j + " " + distance.toString())
//
//                }
//            }
//        }


    }

}