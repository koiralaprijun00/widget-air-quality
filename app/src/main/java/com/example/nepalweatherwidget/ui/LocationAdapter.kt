package com.example.nepalweatherwidget.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nepalweatherwidget.databinding.ItemLocationBinding
import com.example.nepalweatherwidget.domain.model.LocationItem

class LocationAdapter(
    private val onLocationClick: (String) -> Unit
) : ListAdapter<LocationItem, LocationAdapter.LocationViewHolder>(LocationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding = ItemLocationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LocationViewHolder(
        private val binding: ItemLocationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onLocationClick(getItem(position).name)
                }
            }
        }

        fun bind(item: LocationItem) {
            binding.apply {
                locationName.text = item.name
                locationSubtitle.text = item.subtitle
                aqiValue.text = item.aqi.toString()
                aqiLabel.setText(item.aqiStatusRes)
            }
        }
    }

    private class LocationDiffCallback : DiffUtil.ItemCallback<LocationItem>() {
        override fun areItemsTheSame(oldItem: LocationItem, newItem: LocationItem): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: LocationItem, newItem: LocationItem): Boolean {
            return oldItem == newItem
        }
    }
} 