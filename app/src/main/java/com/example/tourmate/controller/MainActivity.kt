package com.example.tourmate.controller


import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.example.tourmate.R
import com.example.tourmate.base.BaseActivity
import com.example.tourmate.controller.interfaces.RecyclerCity0nClickListener
import com.example.tourmate.controller.interfaces.RecyclerLocation0nClickListener
import com.example.tourmate.databinding.ActivityMainBinding
import com.example.tourmate.model.DataCity
import com.example.tourmate.model.DataLocation
import com.example.tourmate.model.LocationClass
import com.example.tourmate.network.RetrofitInstance
import com.example.tourmate.view.DataCityAdapter
import com.example.tourmate.view.DataLocationAdapter
import com.example.tourmate.view.ImagePagerAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : BaseActivity(), RecyclerCity0nClickListener,
    RecyclerLocation0nClickListener {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val imageUrls = listOf(
        R.drawable.welcome,
        R.drawable.slide2,
        R.drawable.slide3,
        R.drawable.halong,
        R.drawable.slide4,
        R.drawable.slide5,
        R.drawable.slide6
    )
    private var dataCityList = ArrayList<DataCity>()
    private var dataTop10LocationList = ArrayList<DataLocation>()
    private lateinit var dataCityAdapter: DataCityAdapter
    private lateinit var dataTop10LocationAdapter: DataLocationAdapter
    private var progressDialog: MaterialDialog? = null
    private var myJob: Job? = null
    private var dataLocationList = ArrayList<DataLocation>()
    private lateinit var dataLocationAdapter: DataLocationAdapter
    private var currentCityId = "0"
    private var currentLocation: LocationClass? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
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
        binding.navigationView.menu.findItem(R.id.home).isChecked = true
        val adapter = ImagePagerAdapter(this, imageUrls)
        binding.viewPager2.adapter = adapter
        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    val currentItem = binding.viewPager2.currentItem
                    val nextItem = if (currentItem == adapter.itemCount - 1) 0 else currentItem + 1
                    binding.viewPager2.currentItem = nextItem
                }
            }
        }, 5000, 5000)
        dataCityList = ArrayList()
        dataTop10LocationList = ArrayList()
        dataLocationList = ArrayList()
        dataCityAdapter = DataCityAdapter(dataCityList)
        dataCityAdapter.setOnItemClickListener(this)
        dataTop10LocationAdapter = DataLocationAdapter(this, dataTop10LocationList)
        dataTop10LocationAdapter.setOnItemClickListener(this)
        dataLocationAdapter = DataLocationAdapter(this, dataLocationList)
        dataLocationAdapter.setOnItemClickListener(this)
        progressDialog = MaterialDialog(this).apply {
            title(text = "Loading")
            message(text = "Please wait...")
            cancelable(false)
            show()
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        binding.recycleViewDataLocation.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recycleViewDataLocation.adapter = dataCityAdapter
        binding.recycleTop10.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recycleTop10.adapter = dataTop10LocationAdapter
        binding.recycleLocation.adapter = dataLocationAdapter
        binding.recycleLocation.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        myJob = GlobalScope.launch {
            while (true) {
                getTop10Location()
                delay(3000)
            }
        }
        getData()
        getAllLocation()
        fetchLocation()
        getLocationOnClick(currentCityId)
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterListDataLocation(newText)
                return true
            }
        })
        binding.searchView.setOnCloseListener {
            if (dataCityList.isEmpty()) {
                binding.textViewEmptyNotice.visibility = View.VISIBLE
                binding.recycleViewDataLocation.visibility = View.GONE
                binding.recycleLocation.visibility = View.GONE
                binding.textViewMore.visibility = View.GONE
            } else {
                binding.textViewEmptyNotice.visibility = View.GONE
                binding.recycleViewDataLocation.visibility = View.VISIBLE
                binding.recycleLocation.visibility = View.VISIBLE
                binding.textViewMore.visibility = View.VISIBLE

            }
            runOnUiThread {

                dataCityAdapter.notifyDataSetChanged()
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
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
            return
        }
        task.addOnSuccessListener {
            if (it != null) {
                currentLocation = LocationClass(0, it.latitude, it.longitude, "My Locaton")
            }
        }
    }

    private fun getTop10Location() {
        val api = RetrofitInstance.api
        api.getTop10Locations().enqueue(object : Callback<List<DataLocation>> {
            override fun onResponse(
                call: Call<List<DataLocation>>,
                response: Response<List<DataLocation>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { it ->
                        dataTop10LocationList.clear()
                        dataTop10LocationList.addAll(it)
                    }
                    dataTop10LocationAdapter.notifyDataSetChanged()
                    binding.textViewMore.visibility = View.VISIBLE

                }
            }
            override fun onFailure(call: Call<List<DataLocation>>, t: Throwable) {
                Toast.makeText(binding.root.context, t.toString(), Toast.LENGTH_LONG).show()
            }

        })
    }

    private fun getData() {
        val api = RetrofitInstance.api
        api.getDataCity().enqueue(object : Callback<List<DataCity>> {
            override fun onResponse(
                call: Call<List<DataCity>>,
                response: Response<List<DataCity>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { it ->
                        dataCityList.clear()
                        val all = DataCity("0", "All", "All")
                        dataCityList.add(all)
                        dataCityList.addAll(it)
                    }
                    dataCityAdapter.notifyDataSetChanged()
                    progressDialog?.dismiss()
                }
            }

            override fun onFailure(call: Call<List<DataCity>>, t: Throwable) {
                Toast.makeText(binding.root.context, t.toString(), Toast.LENGTH_LONG).show()
                progressDialog?.dismiss()
            }

        })
    }

    private fun filterListDataLocation(newText: String?) {
        if (!newText.isNullOrEmpty()) {
            val filteredList = ArrayList<DataLocation>()
            for (i in dataLocationList) {
                if (i.english_name.lowercase(Locale.ROOT).contains(newText) || i.location.lowercase(
                        Locale.ROOT
                    ).contains(newText)
                ) {
                    filteredList.add(i)
                }
            }
            if (filteredList.isEmpty()) {
                binding.textViewEmptyNotice.visibility = View.VISIBLE
                binding.recycleViewDataLocation.visibility = View.GONE
                binding.recycleLocation.visibility = View.GONE
                binding.textViewMore.visibility = View.GONE
            } else {
                binding.textViewEmptyNotice.visibility = View.GONE
                binding.recycleViewDataLocation.visibility = View.VISIBLE
                binding.recycleLocation.visibility = View.VISIBLE
                binding.textViewMore.visibility = View.VISIBLE
                dataLocationAdapter.setFilteredList(filteredList)

            }
        } else {
            binding.textViewEmptyNotice.visibility = View.GONE
            binding.recycleViewDataLocation.visibility = View.VISIBLE
            binding.recycleLocation.visibility = View.VISIBLE
            binding.textViewMore.visibility = View.VISIBLE
            dataLocationAdapter.setFilteredList(dataLocationList)
        }
    }

    private fun getLocationOnClick(city_id: String) {
        val api = RetrofitInstance.api
        api.getLocationsByCity(city_id).enqueue(object : Callback<List<DataLocation>> {
            override fun onResponse(
                call: Call<List<DataLocation>>,
                response: Response<List<DataLocation>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        dataLocationList.clear()
                        dataLocationList.addAll(it)
                        dataLocationList.sortBy { it.english_name }
                    }
                    dataLocationAdapter.notifyDataSetChanged()
                    progressDialog?.dismiss()

                }
            }

            override fun onFailure(call: Call<List<DataLocation>>, t: Throwable) {
                Toast.makeText(this@MainActivity, t.toString(), Toast.LENGTH_LONG)
                    .show()
                progressDialog?.dismiss()

            }
        })
    }

    override fun onItemClick(dataCity: DataCity) {
        progressDialog = MaterialDialog(this).apply {
            title(text = "Loading")
            message(text = "Please wait...")
            cancelable(false)
            show()
        }
        if (dataCity.city_id != "0") {
            getLocationOnClick(dataCity.city_id)
        } else {
            getAllLocation()
        }
        currentCityId = dataCity.city_id
    }

    private fun getAllLocation() {
        val api = RetrofitInstance.api
        api.getLocations().enqueue(object : Callback<List<DataLocation>> {
            override fun onResponse(
                call: Call<List<DataLocation>>,
                response: Response<List<DataLocation>>
            ) {
                if (response.isSuccessful) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            dataLocationList.clear()
                            dataLocationList.addAll(it)
                            dataLocationAdapter.notifyDataSetChanged()
                            progressDialog?.dismiss()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<DataLocation>>, t: Throwable) {
                Toast.makeText(this@MainActivity, t.toString(), Toast.LENGTH_LONG)
                    .show()
            }

        })
    }


    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()

        }
    }

    override fun onStop() {
        super.onStop()
        myJob?.cancel()
    }

    override fun onItemClick(dataLocation: DataLocation) {
        val intent = Intent(this, DetailLocationActivity::class.java)
        intent.putExtra("location_id", dataLocation.id.toString())
        startActivity(intent)
    }

    fun onButtonMainActivityClick(view: View) {
        when (view) {
            binding.textViewMore -> {
                textViewMoreOnClick(currentCityId)
            }
        }
    }
    private fun textViewMoreOnClick(city_id: String) {
        val intent = Intent(this, DataLocationActivity::class.java)
        intent.putExtra("city_id", city_id)
        startActivity(intent)
    }
}
