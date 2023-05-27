package com.example.tourmate.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tourmate.R
import com.example.tourmate.controller.interfaces.RecyclerFavoriteOnClickListener
import com.example.tourmate.databinding.ItemMyFavoriteBinding
import com.example.tourmate.model.ViewFavoriteLocation

class ViewFavoriteAdapter(
    private val context: Context,
    private var items: List<ViewFavoriteLocation>
) :
    RecyclerView.Adapter<ViewFavoriteAdapter.ViewHolder>() {
    private var mListener: RecyclerFavoriteOnClickListener? = null

    class ViewHolder(private val binding: ItemMyFavoriteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ViewFavoriteLocation, context: Context, listener: RecyclerFavoriteOnClickListener?) {
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
            binding.imageViewDelete.setOnClickListener{
                listener?.onDeleteItemClick(item)
            }
            binding.linearLayoutItem.startAnimation(AnimationUtils.loadAnimation(itemView.context, R.anim.scale))
        }
    }

    fun setOnItemClickListener(listener: RecyclerFavoriteOnClickListener?) {
        mListener = listener
    }
    fun setFilteredList(items: List<ViewFavoriteLocation>){
        this.items = items
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ItemMyFavoriteBinding.inflate(inflater, parent, false)
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