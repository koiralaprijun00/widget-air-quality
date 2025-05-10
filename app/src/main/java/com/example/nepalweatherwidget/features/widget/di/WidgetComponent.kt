package com.example.nepalweatherwidget.features.widget.di

import android.content.Context
import com.example.nepalweatherwidget.core.di.scopes.WidgetScope
import com.example.nepalweatherwidget.features.widget.presentation.TraditionalWeatherWidgetProvider
import dagger.BindsInstance
import dagger.Component
import dagger.hilt.components.SingletonComponent

@WidgetScope
@Component(
    dependencies = [SingletonComponent::class],
    modules = [WidgetModule::class]
)
interface WidgetComponent {
    fun inject(provider: TraditionalWeatherWidgetProvider)
    
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance context: Context,
            singletonComponent: SingletonComponent
        ): WidgetComponent
    }
} 