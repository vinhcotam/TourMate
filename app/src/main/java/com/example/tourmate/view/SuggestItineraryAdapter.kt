package com.example.tourmate.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tourmate.R
import com.example.tourmate.databinding.ItemSuggestItineraryBinding
import com.example.tourmate.model.ViewSavedPlace

class SuggestItineraryAdapter(
    private val context: Context,
    private var items: List<ViewSavedPlace>
): RecyclerView.Adapter<SuggestItineraryAdapter.ViewHolder>() {
    class ViewHolder(private val binding: ItemSuggestItineraryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ViewSavedPlace, context: Context) {
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
            binding.textViewMinHours.text = "Min hours : ${item.min_hour}"
            binding.textViewMaxHours.text = "Max hours : ${item.max_hour}"

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ItemSuggestItineraryBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, context)
    }

    override fun getItemCount(): Int {
        return items.size
    }
}