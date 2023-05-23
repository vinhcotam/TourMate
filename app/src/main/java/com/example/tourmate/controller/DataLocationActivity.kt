package com.example.tourmate.controller

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.example.tourmate.R
import com.example.tourmate.base.BaseActivity
import com.example.tourmate.controller.interfaces.RecyclerLocation0nClickListener
import com.example.tourmate.databinding.ActivityDataLocationBinding
import com.example.tourmate.model.DataLocation
import com.example.tourmate.network.RetrofitInstance
import com.example.tourmate.view.DataLocationAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class DataLocationActivity : BaseActivity(), RecyclerLocation0nClickListener {
    private val binding by lazy {
        ActivityDataLocationBinding.inflate(layoutInflater)
    }
    private var progressDialog: MaterialDialog? = null
    private var dataLocationList = ArrayList<DataLocation>()
    private lateinit var dataLocationAdapter: DataLocationAdapter
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
        progressDialog = MaterialDialog(this).apply {
            title(text = "Loading")
            message(text = "Please wait...")
            cancelable(false)
            show()
        }
        val cityId: String = intent.getStringExtra("city_id").toString()
        dataLocationList = ArrayList()
        dataLocationAdapter = DataLocationAdapter(this, dataLocationList)
        dataLocationAdapter.setOnItemClickListener(this)
        binding.recycleViewDataLocation.layoutManager = LinearLayoutManager(this)
        binding.recycleViewDataLocation.adapter = dataLocationAdapter
        if (cityId != "0") {
            getData(cityId)
        } else {
            getAllLocation()
        }
        binding.searchViewLocation.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterListDataLocation(newText)
                return true
            }
        })
        binding.searchViewLocation.setOnCloseListener {
            if (dataLocationList.isEmpty()) {
                binding.textViewEmptyNotice.visibility = View.VISIBLE
                binding.recycleViewDataLocation.visibility = View.GONE
            } else {
                binding.textViewEmptyNotice.visibility = View.GONE
                binding.recycleViewDataLocation.visibility = View.VISIBLE

            }
            runOnUiThread {

                dataLocationAdapter.notifyDataSetChanged()
            }
            true
        }
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
                            progressDialog?.dismiss()                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<DataLocation>>, t: Throwable) {
                Toast.makeText(this@DataLocationActivity, t.toString(), Toast.LENGTH_LONG)
                    .show()
            }
        })
    }

    private fun getData(cityId: String?) {
        val api = RetrofitInstance.api
        if (cityId != null) {
            api.getLocationsByCity(cityId).enqueue(object : Callback<List<DataLocation>> {
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
                    Toast.makeText(this@DataLocationActivity, t.toString(), Toast.LENGTH_LONG)
                        .show()
                    progressDialog?.dismiss()

                }
            })
        }
    }

    private fun filterListDataLocation(newText: String?) {
        if (!newText.isNullOrEmpty()) {
            val filteredList = ArrayList<DataLocation>()
            for (i in dataLocationList) {
                if (i.english_name.lowercase(Locale.ROOT).contains(newText) || i.location.lowercase(Locale.ROOT).contains(newText)) {
                    filteredList.add(i)
                }
            }
            if (filteredList.isEmpty()) {
                binding.textViewEmptyNotice.visibility = View.VISIBLE
                binding.recycleViewDataLocation.visibility = View.GONE
            } else {
                binding.textViewEmptyNotice.visibility = View.GONE
                binding.recycleViewDataLocation.visibility = View.VISIBLE
                dataLocationAdapter.setFilteredList(filteredList)

            }
        } else {
            binding.textViewEmptyNotice.visibility = View.GONE
            binding.recycleViewDataLocation.visibility = View.VISIBLE
            dataLocationAdapter.setFilteredList(dataLocationList)

        }
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