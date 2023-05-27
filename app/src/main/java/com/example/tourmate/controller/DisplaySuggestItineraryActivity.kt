package com.example.tourmate.controller

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tourmate.R
import com.example.tourmate.base.BaseActivity
import com.example.tourmate.databinding.ActivityDisplaySuggestItineraryBinding
import com.example.tourmate.model.DistanceClass
import com.example.tourmate.model.ViewSavedPlace
import com.example.tourmate.view.SuggestItineraryAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class DisplaySuggestItineraryActivity : BaseActivity() {
    private val binding by lazy {
        ActivityDisplaySuggestItineraryBinding.inflate(layoutInflater)
    }
    private lateinit var currentLocation: ViewSavedPlace
    private var currentLatitude = 0.0
    private var currentLongtitude = 0.0
    private var distanceList = ArrayList<DistanceClass>()
    private var suggestList = ArrayList<ViewSavedPlace>()
    private var findShortestList = ArrayList<ViewSavedPlace>()
    private var arrayListSuggestId = ArrayList<Int>()
    private var arrayListEditId = ArrayList<Int>()
    private var editList = ArrayList<ViewSavedPlace>()
    private lateinit var suggestItineraryAdapter: SuggestItineraryAdapter
    private var differences = ArrayList<Int>()
    private var commonList = ArrayList<Int>()
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

        suggestList = ArrayList()
        findShortestList = ArrayList()
        distanceList = ArrayList()
        arrayListSuggestId = ArrayList()
        arrayListEditId = ArrayList()
        editList = ArrayList()
        val jsonStringSuggestList = intent.getStringExtra("myList")
        val jsonStringDistanceList = intent.getStringExtra("distanceList")
        val gson = Gson()
        val typeSuggestList = object : TypeToken<ArrayList<ViewSavedPlace>>() {}.type
        val typeDistanceList = object : TypeToken<ArrayList<DistanceClass>>() {}.type
        suggestList.clear()
        suggestList = gson.fromJson(jsonStringSuggestList, typeSuggestList)
        distanceList = gson.fromJson(jsonStringDistanceList, typeDistanceList)
        editList = ArrayList(suggestList)
        val iterator = editList.iterator()
        while (iterator.hasNext()) {
            val i = iterator.next()
            if (i.location_id == 0) {
                currentLocation = i
                currentLatitude = i.latitude.toDouble()
                currentLongtitude = i.longtitude.toDouble()
                iterator.remove()
            }
        }

        for (i in editList) {
            arrayListSuggestId.add(i.location_id)
        }
        suggestItineraryAdapter = SuggestItineraryAdapter(this, editList)
        binding.recycleSuggestPlace.setHasFixedSize(true)
        binding.recycleSuggestPlace.layoutManager = LinearLayoutManager(this)
        binding.recycleSuggestPlace.adapter = suggestItineraryAdapter
        swipeItem()
    }

    fun onClickSuggest(view: View) {
        when (view) {
            binding.buttonRecalculation -> {
                if (differences.isNotEmpty()) {
                    greedyTravelingSalesman(differences[0], distanceList, commonList)
                    val editFindShortestList = ArrayList(findShortestList)
                    val iterator = editFindShortestList.iterator()
                    while (iterator.hasNext()) {
                        val i = iterator.next()
                        if (i.location_id == 0) {
                            currentLocation = i
                            currentLatitude = i.latitude.toDouble()
                            currentLongtitude = i.longtitude.toDouble()
                            iterator.remove()
                        }
                    }
                    suggestItineraryAdapter = SuggestItineraryAdapter(this, editFindShortestList)
                    binding.recycleSuggestPlace.setHasFixedSize(true)
                    binding.recycleSuggestPlace.layoutManager = LinearLayoutManager(this)
                    binding.recycleSuggestPlace.adapter = suggestItineraryAdapter
                }
            }

            binding.buttonViewMap -> {
                if (differences.isNotEmpty()) {
                    val currentLocation = ViewSavedPlace(
                        0,
                        "",
                        0,
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
                    findShortestList.add(0, currentLocation)
                    val intent = Intent(this, SuggestedItineraryActivity::class.java)
                    val gson = Gson()
                    val jsonStringFindShortestList = gson.toJson(findShortestList)
                    intent.putExtra("myList", jsonStringFindShortestList)
                    startActivity(intent)
                } else {
                    val intent = Intent(this, SuggestedItineraryActivity::class.java)
                    val gson = Gson()
                    val jsonStringFindShortestList = gson.toJson(suggestList)
                    val jsonStringDistanceList = gson.toJson(distanceList)
                    intent.putExtra("myList", jsonStringFindShortestList)
                    intent.putExtra("distanceList", jsonStringDistanceList)
                    startActivity(intent)
                }

            }
        }
    }

    private fun swipeItem() {
        val itemTouchHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(
                recyclerView: RecyclerView,
                source: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val sourcePosition = source.adapterPosition
                val targetPosition = target.adapterPosition
                if (sourcePosition < 0 || targetPosition < 0 ||
                    sourcePosition >= editList.size || targetPosition >= editList.size
                ) {
                    return false
                }
                Collections.swap(editList, sourcePosition, targetPosition)
                suggestItineraryAdapter.notifyItemMoved(sourcePosition, targetPosition)
                arrayListEditId.clear()
                for (i in editList) {
                    arrayListEditId.add(i.location_id)
                }
                if (arrayListSuggestId[0] == 0) {
                    arrayListSuggestId.removeAt(0)
                }
                findDifferences(arrayListSuggestId, arrayListEditId)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                TODO("Not yet implemented")
            }

        })
        itemTouchHelper.attachToRecyclerView(binding.recycleSuggestPlace)
    }

    fun findDifferences(list1: ArrayList<Int>, list2: ArrayList<Int>) {
        differences.clear()
        commonList.clear()

        for (i in list1.indices) {
            if (list1[i] != list2[i]) {
                differences.add(list2[i])
            }
        }

        val firstDifference = differences.firstOrNull()

        if (firstDifference != null) {
            val index = list2.indexOf(firstDifference)
            if (index != -1) {
                for (i in 0 until index) {
                    commonList.add(list2[i])
                }
            }
        }
    }

    private fun greedyTravelingSalesman(
        startId: Int,
        distanceList: ArrayList<DistanceClass>,
        common: ArrayList<Int>
    ) {
        val visited = mutableListOf<Int>()
        visited.add(0)
        if (common.isNotEmpty()) {
            for (i in common) {
                visited.add(i)
            }
        }
        visited.add(startId)
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
        val currentLocation = ViewSavedPlace(
            0,
            "",
            0,
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
        arrayListSuggestId.clear()
        arrayListEditId.clear()
        val uniqueElements = visited.distinct()
        val test = ArrayList<ViewSavedPlace>()
        for (i in uniqueElements) {
            for (j in suggestList) {
                if (i != 0) {
                    if (i == j.location_id) {
                        test.add(j)
                    }
                }
            }
            if (i != 0) {
                arrayListSuggestId.add(i)
                arrayListEditId.add(i)
            }


        }
        findShortestList = test.distinct() as ArrayList<ViewSavedPlace>
//        arrayListSuggestId.add(0,0)
    }

}

