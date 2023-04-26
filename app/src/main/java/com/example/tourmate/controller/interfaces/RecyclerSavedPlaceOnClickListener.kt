package com.example.tourmate.controller.interfaces

import com.example.tourmate.model.ViewSavedPlace

interface RecyclerSavedPlaceOnClickListener {
    fun onItemClick(viewSavedPlace: ViewSavedPlace)
    fun onDeleteItemClick(viewSavedPlace: ViewSavedPlace)
}