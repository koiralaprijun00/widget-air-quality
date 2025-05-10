package com.example.nepalweatherwidget.features.widget.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nepalweatherwidget.core.di.WorkerAssistedFactory
import com.example.nepalweatherwidget.features.widget.data.WidgetDataProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val widgetDataProvider: WidgetDataProvider
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Get widget ID from input data
            val widgetId = inputData.getInt(KEY_WIDGET_ID, -1)
            if (widgetId == -1) return@withContext Result.failure()
            
            // Update widget data
            widgetDataProvider.updateWidget(widgetId)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    @AssistedFactory
    interface Factory : WorkerAssistedFactory<WidgetUpdateWorker>

    companion object {
        const val KEY_WIDGET_ID = "widget_id"
    }
} 