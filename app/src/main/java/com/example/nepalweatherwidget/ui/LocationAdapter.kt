package com.example.nepalweatherwidget.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nepalweatherwidget.databinding.ItemLocationBinding

class LocationAdapter(
    private val onLocationClick: (String) -> Unit
) : RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {

    private var locations: List<String> = emptyList()

    fun submitList(newLocations: List<String>) {
        locations = newLocations
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding = ItemLocationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(locations[position])
    }

    override fun getItemCount(): Int = locations.size

    inner class LocationViewHolder(
        private val binding: ItemLocationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onLocationClick(locations[position])
                }
            }
        }

        fun bind(location: String) {
            binding.tvLocationName.text = location
        }
    }
} 