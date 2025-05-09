package com.example.nepalweatherwidget.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nepalweatherwidget.R
import com.example.nepalweatherwidget.domain.model.LocationItem

class LocationAdapter(private val items: List<LocationItem>) : RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {
    class LocationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textLocationName: TextView = view.findViewById(R.id.textLocationName)
        val textLocationSub: TextView = view.findViewById(R.id.textLocationSub)
        val textAqi: TextView = view.findViewById(R.id.textAqi)
        val imageWeather: ImageView = view.findViewById(R.id.imageWeather)
        val textTemp: TextView = view.findViewById(R.id.textTemp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_location, parent, false)
        return LocationViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val item = items[position]
        holder.textLocationName.text = item.locationName
        holder.textLocationSub.text = item.locationSub
        holder.textAqi.text = "${item.aqi} AQI"
        holder.imageWeather.setImageResource(item.weatherIconRes)
        holder.textTemp.text = "${item.temperature}°C"
    }

    override fun getItemCount(): Int = items.size
} 