package com.example.tourmate.controller

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.tourmate.R
import com.example.tourmate.base.BaseActivity
import com.example.tourmate.controller.interfaces.RecyclerLocation0nClickListener
import com.example.tourmate.databinding.ActivityDetailLocationBinding
import com.example.tourmate.model.*
import com.example.tourmate.network.RetrofitInstance
import com.example.tourmate.view.DataLocationAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import kotlin.random.Random


class DetailLocationActivity : BaseActivity(), RecyclerLocation0nClickListener {
    private val binding by lazy {
        ActivityDetailLocationBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var locationId: String
    private var favoriteList = ArrayList<FavoriteList>()
    private var savedPlaceList = ArrayList<SavedPlace>()
    private var locationList = ArrayList<DataLocation>()
    private var recommendList = ArrayList<DataLocation>()
    private var listStringName = ArrayList<String>()
    private lateinit var dataLocationAdapter: DataLocationAdapter
    private lateinit var location: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        auth = FirebaseAuth.getInstance()
        favoriteList = ArrayList()
        savedPlaceList = ArrayList()
        locationList = ArrayList()
        recommendList = ArrayList()
        listStringName = ArrayList()
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
        locationId = intent.getStringExtra("location_id").toString()
        dataLocationAdapter = DataLocationAdapter(this, recommendList)
        dataLocationAdapter.setOnItemClickListener(this)
        binding.recycleViewRecommend.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recycleViewRecommend.adapter = dataLocationAdapter
        getData(locationId)
        getFavoriteListByUidAndLocationId(auth.uid, locationId)
        getSavedPlaceListByUidAndLocationId(auth.uid, locationId)
    }


    private fun requestRecommend(location: String) {
        val api = RetrofitInstance.api
        api.recommendRequest(location).enqueue(object : Callback<Unit> {
            override fun onResponse(
                call: Call<Unit>,
                response: Response<Unit>
            ) {
                if (response.isSuccessful) {
                    val stringResponse = response.toString()
                    val regex = Regex("url=(.*)")
                    val matchResult = regex.find(stringResponse)
                    val url = matchResult?.groupValues?.get(1)
                    val newStr = url?.removeSuffix("}")
                    receivedRecommend(newStr)
                }
            }
            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Toast.makeText(this@DetailLocationActivity, t.toString(), Toast.LENGTH_LONG).show()
            }

        })
    }

    private fun receivedRecommend(newStr: String?) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://192.168.16.105:5001/recommend?url=$newStr")

//            .url("http://192.168.1.5:5001/recommend?url=$newStr")
            .build()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.d("HTTP", e.message.toString())
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                Log.d("HTTP", response.toString())

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        Log.d("HTTP", responseBody)
                    }

                    val results: List<String> =
                        Gson().fromJson(responseBody, Array<String>::class.java).toList()
                    recommendList.addAll(locationList.filter { it.name in results })
                    runOnUiThread {
                        dataLocationAdapter.notifyDataSetChanged()
                        binding.textViewTopRecommend.visibility = View.VISIBLE
                    }
                } else {
                    Log.e("HTTP", "Request failed with code: ${response.code}")
                }
            }

        })

    }

    private fun getSavedPlaceListByUidAndLocationId(uid: String?, locationId: String) {
        val api = RetrofitInstance.api
        if (uid != null) {
            api.getSavedPlaceListByUidAndLocationId(uid, locationId.toInt())
                .enqueue(object : Callback<List<SavedPlace>> {
                    override fun onResponse(
                        call: Call<List<SavedPlace>>,
                        response: Response<List<SavedPlace>>
                    ) {
                        if (response.isSuccessful) {
                            response.body()?.let {
                                savedPlaceList.clear()
                                savedPlaceList.addAll(it)
                            }
                        }
                    }

                    override fun onFailure(call: Call<List<SavedPlace>>, t: Throwable) {
                        Toast.makeText(this@DetailLocationActivity, t.toString(), Toast.LENGTH_LONG)
                            .show()
                    }

                })
        }

    }

    private fun getFavoriteListByUidAndLocationId(uid: String?, locationId: String) {
        val api = RetrofitInstance.api
        if (uid != null) {
            api.getFavoriteListByUidAndLocationId(uid, locationId.toInt())
                .enqueue(object : Callback<List<FavoriteList>> {
                    override fun onResponse(
                        call: Call<List<FavoriteList>>,
                        response: Response<List<FavoriteList>>
                    ) {
                        if (response.isSuccessful) {
                            response.body()?.let {
                                favoriteList.clear()
                                favoriteList.addAll(it)
                            }
                        }
                    }
                    override fun onFailure(call: Call<List<FavoriteList>>, t: Throwable) {
                        Toast.makeText(this@DetailLocationActivity, t.toString(), Toast.LENGTH_LONG)
                            .show()
                    }
                })
        }

    }

    private fun getData(locationId: String?) {
        val api = RetrofitInstance.api
        if (locationId != null) {
            api.getDetailLocationById(locationId.toInt())
                .enqueue(object : Callback<List<DataLocation>> {
                    override fun onResponse(
                        call: Call<List<DataLocation>>,
                        response: Response<List<DataLocation>>
                    ) {
                        if (response.isSuccessful) {
                            response.body()?.let {
                                Glide.with(this@DetailLocationActivity)
                                    .load(it[0].image_url)
                                    .centerCrop()
                                    .placeholder(R.drawable.logo)
                                    .into(binding.imgLocation)
                                binding.textViewLocationName.text = it[0].english_name
                                binding.textViewLocation.text = it[0].location
                                binding.ratingBarVoteAverage.rating = it[0].vote_average.toFloat()
                                val voteCount = "(" + it[0].vote_count + ")"
                                binding.textViewVoteCount.text = voteCount
                                binding.textViewDescriptionLocation.text = it[0].description
                                location = it[0].name

                            }
                        }
                    }

                    override fun onFailure(call: Call<List<DataLocation>>, t: Throwable) {
                        Toast.makeText(this@DetailLocationActivity, t.toString(), Toast.LENGTH_LONG)
                            .show()
                    }
                })
        }
        api.getLocations().enqueue(object : Callback<List<DataLocation>> {
            override fun onResponse(
                call: Call<List<DataLocation>>,
                response: Response<List<DataLocation>>
            ) {
                if (response.isSuccessful) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            locationList.addAll(it)
                        }
                        requestRecommend(location)
                    }
                }
            }

            override fun onFailure(call: Call<List<DataLocation>>, t: Throwable) {
                Toast.makeText(this@DetailLocationActivity, t.toString(), Toast.LENGTH_LONG)
                    .show()
            }

        })

    }

    fun onButtonClick(view: View) {
        when (view) {
            binding.buttonChoose -> {
                checkExistsSavedPlace()
            }
            binding.buttonAddFavorite -> {
                checkExistsFavorite()
            }
        }
    }

    private fun checkExistsSavedPlace() {
        var canAdd = true
        for (i in savedPlaceList) {
            canAdd = locationId != i.location_id.toString()
        }
        if (canAdd) {
            insertSavedPlaceList()
            getSavedPlaceListByUidAndLocationId(auth.uid, locationId)
        } else {
            Toast.makeText(this, getString(R.string.location_exists), Toast.LENGTH_LONG).show()
        }
    }

    private fun insertSavedPlaceList() {
        val randomNumber = Random.nextInt(0, 10000)
        val api = RetrofitInstance.api
        val call = api.insertSavedPlaceList(
            id = randomNumber,
            uid = auth.uid ?: "",
            locationId = locationId.toInt()
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
                            this@DetailLocationActivity,
                            getString(R.string.success),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this@DetailLocationActivity,
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
                        this@DetailLocationActivity,
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                if (t is IOException) {
                    Toast.makeText(
                        this@DetailLocationActivity,
                        getString(R.string.cant_connect_to_server),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this@DetailLocationActivity,
                        getString(R.string.unknown_error),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        })
    }

    private fun checkExistsFavorite() {
        var canAdd = true

        for (i in favoriteList) {
            if (locationId == i.location_id.toString()) {
                canAdd = false
            }
        }
        if (canAdd) {
            insertFavoriteList()
            getFavoriteListByUidAndLocationId(auth.uid, locationId)
        } else {
            Toast.makeText(this, getString(R.string.location_exists), Toast.LENGTH_LONG).show()
        }
    }

    private fun insertFavoriteList() {
        val randomNumber = Random.nextInt(0, 10000)
        val api = RetrofitInstance.api
        val call = api.insertFavoriteList(
            id = randomNumber,
            uid = auth.uid ?: "",
            locationId = locationId.toInt()
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
                            this@DetailLocationActivity,
                            getString(R.string.success),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this@DetailLocationActivity,
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
                        this@DetailLocationActivity,
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                if (t is IOException) {
                    Toast.makeText(
                        this@DetailLocationActivity,
                        getString(R.string.cant_connect_to_server),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this@DetailLocationActivity,
                        getString(R.string.unknown_error),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        })
    }
    override fun onItemClick(dataLocation: DataLocation) {
        val intent = Intent(this, DetailLocationActivity::class.java)
        intent.putExtra("location_id", dataLocation.id.toString())
        startActivity(intent)
    }
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()

        }
    }
}