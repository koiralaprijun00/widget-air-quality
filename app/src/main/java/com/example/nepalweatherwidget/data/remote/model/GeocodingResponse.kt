package com.example.nepalweatherwidget.data.remote.model

import com.google.gson.annotations.SerializedName

data class GeocodingResponse(
    @SerializedName("name")
    val name: String,
    @SerializedName("local_names")
    val localNames: Map<String, String>? = null,
    @SerializedName("lat")
    val latitude: Double,
    @SerializedName("lon")
    val longitude: Double,
    @SerializedName("country")
    val country: String,
    @SerializedName("state")
    val state: String? = null,
    @SerializedName("zip")
    val zip: String? = null
) 