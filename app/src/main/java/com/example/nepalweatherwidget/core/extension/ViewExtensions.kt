package com.example.nepalweatherwidget.core.extension

import android.view.View
import android.widget.TextView

/**
 * Safely sets text on a TextView, using a default value if the text is null
 */
fun TextView.setTextSafe(text: String?) {
    this.text = text ?: "--"
}

/**
 * Safely sets visibility on a View, using VISIBLE or GONE
 */
fun View.setVisibilitySafe(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.GONE
}

/**
 * Safely sets temperature on a TextView, formatting it with °C and handling null values
 */
fun TextView.setTemperatureSafe(temp: Double?) {
    this.text = temp?.let { "${it.toInt()}°C" } ?: "--°C"
}

/**
 * Safely sets an integer value on a TextView, using a default value if null
 */
fun TextView.setIntSafe(value: Int?) {
    this.text = value?.toString() ?: "--"
}

/**
 * Safely sets a double value on a TextView with specified decimal places
 */
fun TextView.setDoubleSafe(value: Double?, decimalPlaces: Int = 1) {
    this.text = value?.let { 
        "%.${decimalPlaces}f".format(it)
    } ?: "--"
}

/**
 * Safely sets a percentage value on a TextView
 */
fun TextView.setPercentageSafe(value: Int?) {
    this.text = value?.let { "$it%" } ?: "--%"
}

/**
 * Safely sets a resource string on a TextView
 */
fun TextView.setTextResourceSafe(resId: Int?, defaultText: String = "--") {
    this.text = resId?.let { context.getString(it) } ?: defaultText
} 