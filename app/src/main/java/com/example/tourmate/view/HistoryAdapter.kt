package com.example.tourmate.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tourmate.R
import com.example.tourmate.controller.interfaces.RecyclerHistoryOnClickListener
import com.example.tourmate.databinding.ItemHistoryBinding
import com.example.tourmate.model.DataLocation
import com.example.tourmate.model.History

class HistoryAdapter(
    private val context: Context,
    private var items: List<History>,
    dataLocationList: ArrayList<DataLocation>
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {
    private var mListener: RecyclerHistoryOnClickListener? = null
    var locationList = dataLocationList

    class ViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: History,
            context: Context,
            listener: RecyclerHistoryOnClickListener?,
            locationList: ArrayList<DataLocation>
        ) {
            binding.textViewDateHistory.text = "Date: ${item.date}"
//            binding.textViewItineraryHistory.text = "Itinerary: ${item.itinerary}"
            val result = item.itinerary.trim().split("-")
            val trimmedArray = result.map { it.trim() }.toTypedArray()
            val itineraryList = locationList.filter { trimmedArray.contains(it.english_name) }
            val reversedList = itineraryList.reversed()
            val dataLocationAdapter = DataLocationAdapter(context, reversedList)
            binding.recycleItinerary.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.recycleItinerary.adapter = dataLocationAdapter
            binding.imageViewDelete.setOnClickListener {
                listener?.onDeleteItemClick(item)
            }
            binding.linearLayoutItem.startAnimation(
                AnimationUtils.loadAnimation(
                    itemView.context,
                    R.anim.scale
                )
            )
        }
    }

    fun setFilteredList(items: List<History>){
        this.items = items
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ItemHistoryBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, context, mListener, locationList)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setOnItemClickListener(listener: RecyclerHistoryOnClickListener?) {
        mListener = listener
    }
}