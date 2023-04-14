package com.example.tourmate.controller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tourmate.databinding.ActivityDataLocationBinding
import com.example.tourmate.model.DataLocation
import com.example.tourmate.network.RetrofitInstance
import com.example.tourmate.view.DataLocationAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class DataLocationActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityDataLocationBinding.inflate(layoutInflater)
    }
    private var dataLocationList = ArrayList<DataLocation>()
    private lateinit var dataLocationAdapter: DataLocationAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val cityId: String = intent.getStringExtra("city_id").toString()
        dataLocationList = ArrayList()
        dataLocationAdapter = DataLocationAdapter(this, dataLocationList)
        binding.recycleViewDataLocation.layoutManager = LinearLayoutManager(this)
        binding.recycleViewDataLocation.adapter = dataLocationAdapter
        val api = RetrofitInstance.api
        api.getLocationsByCity(cityId).enqueue(object : Callback<List<DataLocation>> {
            override fun onResponse(
                call: Call<List<DataLocation>>,
                response: Response<List<DataLocation>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { dataLocationList.addAll(it) }
                    dataLocationAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<List<DataLocation>>, t: Throwable) {
                Toast.makeText(this@DataLocationActivity, t.toString(), Toast.LENGTH_LONG).show()
            }
        })
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
    }
    private fun filterListDataLocation(newText: String?) {
        if (!newText.isNullOrEmpty()) {
            val filteredList = ArrayList<DataLocation>()
            for (i in dataLocationList) {
                if (i.name.lowercase(Locale.ROOT).contains(newText)) {
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
        }
    }
}