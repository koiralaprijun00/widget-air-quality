package com.example.nepalweatherwidget.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nepalweatherwidget.R
import com.example.nepalweatherwidget.domain.model.ForecastItem

class ForecastAdapter(private val items: List<ForecastItem>) : RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {
    class ForecastViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textHour: TextView = view.findViewById(R.id.textHour)
        val textAqiEmoji: TextView = view.findViewById(R.id.textAqiEmoji)
        val textAqi: TextView = view.findViewById(R.id.textAqi)
        val imageWeather: ImageView = view.findViewById(R.id.imageWeather)
        val textTemp: TextView = view.findViewById(R.id.textTemp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_forecast, parent, false)
        return ForecastViewHolder(view)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val item = items[position]
        holder.textHour.text = item.hour
        holder.textAqiEmoji.text = item.aqiEmoji
        holder.textAqi.text = "${item.aqi} AQI"
        holder.imageWeather.setImageResource(item.weatherIconRes)
        holder.textTemp.text = "${item.temperature}°C"
    }

    override fun getItemCount(): Int = items.size
} 