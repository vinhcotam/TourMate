package com.example.tourmate.controller

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.example.tourmate.R
import com.example.tourmate.base.BaseActivity
import com.example.tourmate.controller.interfaces.RecyclerSavedPlaceOnClickListener
import com.example.tourmate.databinding.ActivitySavedPlaceBinding
import com.example.tourmate.model.*
import com.example.tourmate.network.MapsApiService
import com.example.tourmate.network.RetrofitInstance
import com.example.tourmate.utilities.Constants.Companion.BING_MAP_API_KEY
import com.example.tourmate.view.ViewSavedPlaceAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class SavedPlaceActivity : BaseActivity(), RecyclerSavedPlaceOnClickListener {
    private val binding by lazy {
        ActivitySavedPlaceBinding.inflate(layoutInflater)
    }
    private var savedPlaceList = ArrayList<ViewSavedPlace>()
    private var findShortestList = ArrayList<ViewSavedPlace>()
    private lateinit var auth: FirebaseAuth
    private lateinit var viewSavedPlaceAdapter: ViewSavedPlaceAdapter
    private var distanceList = ArrayList<DistanceClass>()
    private var progressDialog: MaterialDialog? = null
    private var locationList = ArrayList<LocationClass>()
    private var currentLatitude: Double? = 0.0
    private var currentLongtitude: Double? = 0.0
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: LocationClass? = null
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
        findShortestList = ArrayList()
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
        swipeItem()
    }

    private fun swipeItem() {
        val simpleItemTouchCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val deleteSavedPlace = savedPlaceList[viewHolder.position]
                savedPlaceList.removeAt(viewHolder.adapterPosition)
                viewSavedPlaceAdapter.notifyItemRemoved(viewHolder.adapterPosition)
                Snackbar.make(
                    binding.recycleViewSavedPlace,
                    "Delete " + deleteSavedPlace.english_name,
                    Snackbar.LENGTH_LONG
                )
                    .setAction("Undo", View.OnClickListener {
                        savedPlaceList.add(position, deleteSavedPlace)
                        viewSavedPlaceAdapter.notifyItemInserted(position)
                    }).show()

            }
        }
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(binding.recycleViewSavedPlace)
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
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
            return
        }
        task.addOnSuccessListener {
            if (it != null) {
                currentLocation = LocationClass(0, it.latitude, it.longitude, "My Location")
                currentLatitude = it.latitude
                currentLongtitude = it.longitude
                locationList.clear()
                locationList.add(currentLocation as LocationClass)
                if (currentLatitude != 0.0 && currentLongtitude != 0.0) {
                    getDataDistance(savedPlaceList)
                }

            }
        }
    }

    private fun filterSavedPlaceList(newText: String?) {
        if (!newText.isNullOrEmpty()) {
            val filteredList = ArrayList<ViewSavedPlace>()
            for (i in savedPlaceList) {
                if (i.english_name.lowercase(Locale.ROOT).contains(newText) || i.location.lowercase(
                        Locale.ROOT
                    ).contains(newText)
                ) {
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
                            }
                            locationList.clear()
                            distanceList.clear()
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

                    override fun onFailure(
                        call: Call<List<ViewSavedPlace>>,
                        t: Throwable
                    ) {
                        Toast.makeText(this@SavedPlaceActivity, t.toString(), Toast.LENGTH_LONG)
                            .show()
                        progressDialog?.dismiss()

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

    fun insertDistance(startLocation: LocationClass, endLocation: LocationClass) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://dev.virtualearth.net")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val distanceMatrixService = retrofit.create(MapsApiService::class.java)
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
                    val distance =
                        distanceInMeters?.let {
                            DistanceClass(
                                startLocation.id, endLocation.id,
                                it
                            )
                        }
                    response.body().let {
                        if (distance != null) {
                            distanceList.add(distance)
                        }
                    }
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
                                    } else {
                                        Toast.makeText(
                                            this@SavedPlaceActivity,
                                            getString(R.string.fail),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }

                            override fun onFailure(
                                call: Call<ResponseBody>,
                                t: Throwable
                            ) {
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

    private fun getDataDistance(savedPlaceList: List<ViewSavedPlace>) {
        for (viewSavedPlace in savedPlaceList) {
            val location = LocationClass(
                id = viewSavedPlace.location_id,
                latitude = viewSavedPlace.latitude.toDouble(),
                longtitude = viewSavedPlace.longtitude.toDouble(),
                english_name = viewSavedPlace.english_name
            )
            locationList.add(location)
        }
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
                                response.body()?.let {
                                    if (locationList[i].id != locationList[j].id) {
                                        distanceList.addAll(it)
                                    }
                                }
                            }

                        }
                    }

                    override fun onFailure(
                        call: Call<List<DistanceClass>>,
                        t: Throwable
                    ) {
                        Toast.makeText(
                            this@SavedPlaceActivity,
                            t.toString(),
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                })
            }
        }

    }

    fun onClickSavedPlace(view: View) {
        when (view) {
            binding.btnStartItinerary -> {
                if (savedPlaceList.isNotEmpty()) {
                    progressDialog = MaterialDialog(this).apply {
                        title(text = "Loading")
                        message(text = "Please wait...")
                        cancelable(false)
                        show()
                    }
                    greedyTravelingSalesman(0, distanceList)
                    val intent = Intent(this, DisplaySuggestItineraryActivity::class.java)
                    val gson = Gson()
                    val jsonStringFindShortestList = gson.toJson(findShortestList)
                    val jsonStringDistanceList = gson.toJson(distanceList)
                    intent.putExtra("myList", jsonStringFindShortestList)
                    intent.putExtra("distanceList", jsonStringDistanceList)
                    savedPlaceList.clear()
                    findShortestList.clear()
                    distanceList.clear()
                    startActivity(intent)
                    finish()
                }

            }
        }
    }

    private fun greedyTravelingSalesman(startId: Int, distanceList: ArrayList<DistanceClass>) {
        val visited = mutableListOf(startId)
        var currentId = startId
        while (visited.size < distanceList.size + 1) {
            var nearestId: Int? = null
            var minDistance = Double.MAX_VALUE
            for (distance in distanceList) {
                if (!visited.contains(distance.location_end_id)) {
                    if (distance.location_start_id == currentId && distance.distance < minDistance) {
                        nearestId = distance.location_end_id
                        minDistance = distance.distance
                    } else if (distance.location_end_id == currentId && distance.distance < minDistance) {
                        nearestId = distance.location_start_id
                        minDistance = distance.distance
                    }
                }
            }
            if (nearestId != null) {
                visited.add(nearestId)
                currentId = nearestId
            } else {
                break
            }
        }

        if (visited.size == distanceList.size + 1) {
            visited.add(startId)
        }
        val missingPoints = savedPlaceList.map { it.location_id } - visited.toSet()
        visited.addAll(missingPoints)
        val currentLocation =
            ViewSavedPlace(
                0,
                "",
                0,
                "My Location",
                "a",
                "01",
                currentLatitude.toString(),
                currentLongtitude.toString(),
                "1",
                1,
                "",
                "My Location",
                1.0,
                2.0
            )
        findShortestList.add(currentLocation)
        for (i in visited) {
            for (j in savedPlaceList) {
                if (i != 0) {
                    if (i == j.location_id) {
                        findShortestList.add(j)
                    }
                }
            }
        }
        progressDialog?.dismiss()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()

        }
    }

}