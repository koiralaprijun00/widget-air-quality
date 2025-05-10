package com.example.nepalweatherwidget.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nepalweatherwidget.databinding.ItemForecastBinding
import com.example.nepalweatherwidget.data.model.ForecastItem

class ForecastAdapter(
    private var forecasts: List<ForecastItem>
) : RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    fun submitList(newForecasts: List<ForecastItem>) {
        forecasts = newForecasts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val binding = ItemForecastBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ForecastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        holder.bind(forecasts[position])
    }

    override fun getItemCount(): Int = forecasts.size

    class ForecastViewHolder(
        private val binding: ItemForecastBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(forecast: ForecastItem) {
            binding.apply {
                tvDay.text = forecast.day
                tvTemperature.text = forecast.temperature
                tvDescription.text = forecast.description
                ivWeatherIcon.setImageResource(forecast.iconRes)
            }
        }
    }
} 