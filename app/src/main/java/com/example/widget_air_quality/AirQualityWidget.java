package com.example.widget_air_quality;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AirQualityWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Create the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.air_quality_widget);

        // TODO: Replace with actual API call to get air quality data
        int aqi = 75; // Example AQI value
        String description = getAQIDescription(aqi);
        
        // Update the widget views
        views.setTextViewText(R.id.aqi_value, String.valueOf(aqi));
        views.setTextViewText(R.id.aqi_description, description);
        views.setTextViewText(R.id.last_updated, "Last updated: " + getCurrentTime());

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static String getAQIDescription(int aqi) {
        if (aqi <= 50) return "Good";
        if (aqi <= 100) return "Moderate";
        if (aqi <= 150) return "Unhealthy for Sensitive Groups";
        if (aqi <= 200) return "Unhealthy";
        if (aqi <= 300) return "Very Unhealthy";
        return "Hazardous";
    }

    private static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }
} 