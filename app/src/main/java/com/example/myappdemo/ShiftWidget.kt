package com.example.myappdemo

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

class ShiftWidget : AppWidgetProvider() {
    override fun onEnabled(context: Context) {
        WidgetUpdateReceiver.setDailyUpdate(context)
        WeatherUpdateJobService.ensurePeriodic(context)
        WeatherUpdateJobService.scheduleImmediate(context)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        ShiftConfig.init(context)
        for (appWidgetId in appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, WidgetViewsFactory.createShiftWidgetViews(context))
        }
        WidgetUpdateReceiver.setDailyUpdate(context)
        WeatherUpdateJobService.ensurePeriodic(context)
        WeatherUpdateJobService.scheduleImmediate(context)
    }
}
