package com.example.tourmate.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tourmate.databinding.ItemDurationDistanceBinding
import com.example.tourmate.model.ResultDistanceAndDuration

class ResultDistanceAndDurationAdapter(private var items: List<ResultDistanceAndDuration>) :
    RecyclerView.Adapter<ResultDistanceAndDurationAdapter.ViewHolder>() {
    class ViewHolder(private val binding: ItemDurationDistanceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ResultDistanceAndDuration) {
            binding.textViewStartEndLocation.text = "${item.start_name } to ${item.end_name }"
            binding.textViewDistance.text = "Distance: ${item.distance}"
            binding.textViewDuration.text = "Duration:  ${item.duration}"
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDurationDistanceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return items.size
    }
}