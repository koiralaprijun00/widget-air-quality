package com.example.nepalweatherwidget.presentation.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nepalweatherwidget.databinding.ItemForecastBinding
import com.example.nepalweatherwidget.domain.model.ForecastItem

class ForecastAdapter(
    private val onItemClick: (ForecastItem) -> Unit
) : ListAdapter<ForecastItem, ForecastAdapter.ForecastViewHolder>(ForecastDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val binding = ItemForecastBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ForecastViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ForecastViewHolder(
        private val binding: ItemForecastBinding,
        private val onItemClick: (ForecastItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(item: ForecastItem) {
            binding.apply {
                textHour.text = item.hour
                textAqi.text = "${item.aqi} AQI"
                textAqiEmoji.text = item.aqiEmoji
                textTemp.text = "${item.temperature}°C"
                imageWeather.setImageResource(item.weatherIconRes)
            }
        }
    }

    private class ForecastDiffCallback : DiffUtil.ItemCallback<ForecastItem>() {
        override fun areItemsTheSame(oldItem: ForecastItem, newItem: ForecastItem): Boolean {
            return oldItem.hour == newItem.hour
        }

        override fun areContentsTheSame(oldItem: ForecastItem, newItem: ForecastItem): Boolean {
            return oldItem == newItem
        }
    }
} 