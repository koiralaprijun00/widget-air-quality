package com.example.nepalweatherwidget.core.di

import androidx.work.ListenableWorker

interface WorkerAssistedFactory<T : ListenableWorker> {
    fun create(workerParameters: androidx.work.WorkerParameters): T
} 