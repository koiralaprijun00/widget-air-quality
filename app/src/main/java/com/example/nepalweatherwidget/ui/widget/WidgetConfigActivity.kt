package com.example.nepalweatherwidget.ui.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.nepalweatherwidget.R
import com.example.nepalweatherwidget.core.result.Result
import com.example.nepalweatherwidget.databinding.ActivityWidgetConfigBinding
import com.example.nepalweatherwidget.domain.model.Location
import com.example.nepalweatherwidget.ui.widget.viewmodel.WidgetConfigViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WidgetConfigActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWidgetConfigBinding
    private val viewModel: WidgetConfigViewModel by viewModels()
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWidgetConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the result to CANCELED in case the user backs out
        setResult(RESULT_CANCELED)

        // Get the widget ID from the intent
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // Setup location spinner
        binding.locationSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            mutableListOf<String>()
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // Setup refresh interval spinner
        val intervals = resources.getStringArray(R.array.refresh_intervals)
        binding.refreshIntervalSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            intervals
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // Setup add location button
        binding.addLocationButton.setOnClickListener {
            showAddLocationDialog()
        }

        // Setup save button
        binding.saveButton.setOnClickListener {
            saveWidgetConfiguration()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUI(state)
                }
            }
        }
    }

    private fun updateUI(state: WidgetConfigViewModel.UiState) {
        when (state) {
            is WidgetConfigViewModel.UiState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.contentLayout.visibility = View.GONE
            }
            is WidgetConfigViewModel.UiState.Success -> {
                binding.progressBar.visibility = View.GONE
                binding.contentLayout.visibility = View.VISIBLE
                updateLocationSpinner(state.locations)
            }
            is WidgetConfigViewModel.UiState.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.contentLayout.visibility = View.VISIBLE
                Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateLocationSpinner(locations: List<Location>) {
        val adapter = binding.locationSpinner.adapter as ArrayAdapter<String>
        adapter.clear()
        adapter.addAll(locations.map { it.name })
        adapter.notifyDataSetChanged()
    }

    private fun showAddLocationDialog() {
        val dialog = AddLocationDialog()
        dialog.show(supportFragmentManager, "AddLocationDialog")
    }

    private fun saveWidgetConfiguration() {
        val selectedLocation = binding.locationSpinner.selectedItem as? String
        val refreshInterval = binding.refreshIntervalSpinner.selectedItem as? String

        if (selectedLocation == null || refreshInterval == null) {
            Toast.makeText(this, "Please select all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.saveWidgetConfiguration(
            widgetId = appWidgetId,
            locationName = selectedLocation,
            refreshInterval = refreshInterval
        )

        // Set the result and finish
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }
} 