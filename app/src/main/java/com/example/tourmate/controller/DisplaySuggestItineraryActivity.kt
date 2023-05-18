package com.example.tourmate.controller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tourmate.R
import com.example.tourmate.databinding.ActivityDisplaySuggestItineraryBinding
import com.example.tourmate.model.DistanceClass
import com.example.tourmate.model.ViewSavedPlace
import com.example.tourmate.view.SuggestItineraryAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Collections

class DisplaySuggestItineraryActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityDisplaySuggestItineraryBinding.inflate(layoutInflater)
    }
    private lateinit var currentLocation: ViewSavedPlace
    private var currentLatitude = 0.0
    private var currentLongtitude = 0.0
    private var distanceList = ArrayList<DistanceClass>()
    private var suggestList = ArrayList<ViewSavedPlace>()
    private var findShortestList = ArrayList<ViewSavedPlace>()
    private lateinit var suggestItineraryAdapter: SuggestItineraryAdapter
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
        suggestList = ArrayList()
        findShortestList = ArrayList()
        distanceList = ArrayList()
        val jsonStringSuggestList = intent.getStringExtra("myList")
        val jsonStringDistanceList = intent.getStringExtra("distanceList")
        val gson = Gson()
        val typeSuggestList = object : TypeToken<ArrayList<ViewSavedPlace>>() {}.type
        val typeDistanceList = object: TypeToken<ArrayList<DistanceClass>>() {}.type
        suggestList.clear()
        suggestList = gson.fromJson(jsonStringSuggestList, typeSuggestList)
        distanceList = gson.fromJson(jsonStringDistanceList, typeDistanceList)
        val iterator = suggestList.iterator()
        while (iterator.hasNext()) {
            val i = iterator.next()
            if (i.location_id == 0) {
                currentLocation = i
                currentLatitude = i.latitude.toDouble()
                currentLongtitude = i.longtitude.toDouble()
                iterator.remove()
            }
        }

        Log.d("assdhfhj", currentLocation.toString())

        suggestItineraryAdapter = SuggestItineraryAdapter(this, suggestList)
        binding.recycleSuggestPlace.setHasFixedSize(true)
        binding.recycleSuggestPlace.layoutManager = LinearLayoutManager(this)
        binding.recycleSuggestPlace.adapter = suggestItineraryAdapter
        swipeItem()
    }
    fun onClickSuggest(view: View) {
        when (view) {
            binding.buttonRecalculation -> {

            }
            binding.buttonViewMap ->{

            }
        }
    }
    private fun swipeItem(){
        val itemTouchHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(
                recyclerView: RecyclerView,
                source: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val sourcePosition = source.adapterPosition
                val targetPosition = target.adapterPosition

                Collections.swap(suggestList, sourcePosition, targetPosition)
                suggestItineraryAdapter.notifyItemMoved(sourcePosition, targetPosition)
                Log.d("assdhfhj1", suggestList.toString())
                return true

            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                TODO("Not yet implemented")
            }

        })
        itemTouchHelper.attachToRecyclerView(binding.recycleSuggestPlace)
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
        val missingPoints = suggestList.map { it.location_id } - visited.toSet()
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
            for (j in suggestList) {
                if (i != 0) {
                    if (i == j.location_id) {
                        findShortestList.add(j)
                    }
                }
            }
        }
    }
}

