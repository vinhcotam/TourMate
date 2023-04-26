package com.example.tourmate.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tourmate.R
import com.example.tourmate.controller.interfaces.RecyclerFavoriteOnClickListener
import com.example.tourmate.controller.interfaces.RecyclerSavedPlaceOnClickListener
import com.example.tourmate.databinding.ItemSavedPlaceBinding
import com.example.tourmate.model.ViewSavedPlace

class ViewSavedPlaceAdapter(
    private val context: Context,
    private var items: List<ViewSavedPlace>
) : RecyclerView.Adapter<ViewSavedPlaceAdapter.ViewHolder>() {
    private var mListener: RecyclerSavedPlaceOnClickListener? = null

    class ViewHolder(private val binding: ItemSavedPlaceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: ViewSavedPlace,
            context: Context,
            listener: RecyclerSavedPlaceOnClickListener?
        ) {
            val name = item.english_name
            val voteCount = "(" + item.vote_count + ")"
            val location = item.location
            binding.textViewLocationName.text = name
            binding.textViewLocation.text = location
            binding.ratingBarVoteAverage.rating = item.vote_average.toFloat()
            binding.textViewVoteCount.text = voteCount
            Glide.with(context)
                .load(item.image_url)
                .centerCrop()
                .placeholder(R.drawable.logo)
                .into(binding.imgLocation)
            binding.imgLocation.setOnClickListener {
                listener?.onItemClick(item)
            }
            binding.imageViewDelete.setOnClickListener {
                listener?.onDeleteItemClick(item)
            }
        }
    }

    fun setOnItemClickListener(listener: RecyclerSavedPlaceOnClickListener?) {
        mListener = listener
    }

    fun setFilteredList(items: List<ViewSavedPlace>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ItemSavedPlaceBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, context, mListener)
    }

    override fun getItemCount(): Int {
        return items.size
    }
}