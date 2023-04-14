package com.example.tourmate.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tourmate.controller.RecyclerCity0nClickListener
import com.example.tourmate.databinding.ItemDataCityBinding
import com.example.tourmate.model.DataCity

class DataCityAdapter(private var items: List<DataCity>) :
    RecyclerView.Adapter<DataCityAdapter.ViewHolder>() {

    private var mListener: RecyclerCity0nClickListener? = null

    class ViewHolder(private val binding: ItemDataCityBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DataCity, listener: RecyclerCity0nClickListener?) {
            binding.buttonCity.text = item.city_name
            binding.buttonCity.setOnClickListener {
                listener?.onItemClick(item)
            }
        }


    }

    fun setOnItemClickListener(listener: RecyclerCity0nClickListener?) {
        mListener = listener
    }

    fun setFilteredList(items: List<DataCity>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDataCityBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, mListener)
    }

    override fun getItemCount(): Int {
        return items.size
    }
}