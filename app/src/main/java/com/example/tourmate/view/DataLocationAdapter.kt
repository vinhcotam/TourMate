package com.example.tourmate.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tourmate.databinding.ItemDataLocationBinding
import com.example.tourmate.model.DataLocation

class DataLocationAdapter(private val context: Context, private var items: List<DataLocation>) :
    RecyclerView.Adapter<DataLocationAdapter.ViewHolder>() {
    class ViewHolder(private val binding: ItemDataLocationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DataLocation) {
            val name = "Địa điểm: "+item.name+""
            binding.textViewLocationName.text = name
            val location = "Địa chỉ: "+item.location
            binding.textViewLocation.text = location
            binding.ratingBarVoteAverage.rating = item.vote_average.toFloat()
            val voteCount = "("+item.vote_count+")"
            binding.textViewVoteCount.text = voteCount
        }
    }
    fun setFilteredList(items: List<DataLocation>){
        this.items = items
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ItemDataLocationBinding.inflate(inflater, parent, false)
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
