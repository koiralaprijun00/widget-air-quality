package com.example.nepalweatherwidget

import android.app.Application
import androidx.work.Configuration

class NepalWeatherApp : Application(), Configuration.Provider {
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .build()
} 