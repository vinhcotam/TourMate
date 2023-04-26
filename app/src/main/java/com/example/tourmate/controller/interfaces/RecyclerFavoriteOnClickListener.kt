package com.example.tourmate.controller.interfaces

import com.example.tourmate.model.ViewFavoriteLocation

interface RecyclerFavoriteOnClickListener {
    fun onItemClick(viewFavoriteLocation: ViewFavoriteLocation)
    fun onDeleteItemClick(viewFavoriteLocation: ViewFavoriteLocation)
}