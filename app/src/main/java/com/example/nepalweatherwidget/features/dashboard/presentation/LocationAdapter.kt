package com.example.nepalweatherwidget.features.dashboard.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nepalweatherwidget.databinding.ItemLocationBinding
import com.example.nepalweatherwidget.features.dashboard.domain.model.LocationItem

class LocationAdapter(
    private val onLocationClick: (LocationItem) -> Unit
) : ListAdapter<LocationItem, LocationAdapter.LocationViewHolder>(LocationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding = ItemLocationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LocationViewHolder(binding, onLocationClick)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LocationViewHolder(
        private val binding: ItemLocationBinding,
        private val onLocationClick: (LocationItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onLocationClick(getItem(position))
                }
            }
        }

        fun bind(location: LocationItem) {
            binding.apply {
                tvLocationName.text = location.locationName
                tvLocationTemp.text = "${location.temperature}°C"
                tvLocationWeather.text = location.locationSub
            }
        }
    }

    private class LocationDiffCallback : DiffUtil.ItemCallback<LocationItem>() {
        override fun areItemsTheSame(oldItem: LocationItem, newItem: LocationItem): Boolean {
            return oldItem.locationName == newItem.locationName
        }

        override fun areContentsTheSame(oldItem: LocationItem, newItem: LocationItem): Boolean {
            return oldItem == newItem
        }
    }
} 