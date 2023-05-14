package com.example.tourmate.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tourmate.R

class ImagePagerAdapter(private val context: Context, private val imageUrls: List<Int>) :
    RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {
    private val actualItemCount = imageUrls.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.image_slide, parent, false)
        return ImageViewHolder(view)
    }

    override fun getItemCount(): Int {
        return Int.MAX_VALUE
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = imageUrls[position % actualItemCount]
        Glide.with(context)
            .load(imageUrl)
            .centerCrop()
            .into(holder.imageView)
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }
}