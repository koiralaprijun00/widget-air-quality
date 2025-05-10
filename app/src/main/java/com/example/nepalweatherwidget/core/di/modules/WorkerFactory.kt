package com.example.nepalweatherwidget.core.di

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters

interface WorkerAssistedFactory<T : ListenableWorker> {
    fun create(appContext: Context, workerParameters: WorkerParameters): T
} 