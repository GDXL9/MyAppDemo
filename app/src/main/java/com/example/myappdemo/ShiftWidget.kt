package com.example.myappdemo

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import java.util.*

class ShiftWidget : AppWidgetProvider() {

    private val shiftCycle = arrayOf("白", "夜", "休", "白", "夜", "休", "休", "休")

    private val baseCalendar = Calendar.getInstance().apply {
        set(2026, Calendar.APRIL, 25, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private val dayNames = arrayOf("一", "二", "三", "四", "五", "六", "日")

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
    }

    override fun onDisabled(context: Context) {
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_week_shift)

        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        val firstDayOfWeek = getFirstDayOfWeek(today)

        val dayIds = arrayOf(
            intArrayOf(R.id.day1Name, R.id.day1Date, R.id.day1Shift),
            intArrayOf(R.id.day2Name, R.id.day2Date, R.id.day2Shift),
            intArrayOf(R.id.day3Name, R.id.day3Date, R.id.day3Shift),
            intArrayOf(R.id.day4Name, R.id.day4Date, R.id.day4Shift),
            intArrayOf(R.id.day5Name, R.id.day5Date, R.id.day5Shift),
            intArrayOf(R.id.day6Name, R.id.day6Date, R.id.day6Shift),
            intArrayOf(R.id.day7Name, R.id.day7Date, R.id.day7Shift)
        )

        val dateRangeBuilder = StringBuilder()

        for (i in 0..6) {
            val currentDate = Calendar.getInstance().apply {
                timeInMillis = firstDayOfWeek.timeInMillis
                add(Calendar.DAY_OF_MONTH, i)
            }

            val dayName = dayNames[i]
            val dateStr = "${currentDate.get(Calendar.MONTH) + 1}/${currentDate.get(Calendar.DAY_OF_MONTH)}"
            val shift = getShiftForDate(currentDate)

            views.setTextViewText(dayIds[i][0], dayName)
            views.setTextViewText(dayIds[i][1], dateStr)
            views.setTextViewText(dayIds[i][2], shift)

            if (isToday(currentDate, today)) {
                views.setInt(dayIds[i][1], "setTextColor", ContextCompat.getColor(context, R.color.colorPrimary))
                views.setInt(dayIds[i][2], "setTextColor", ContextCompat.getColor(context, R.color.colorPrimary))
            }

            when (shift) {
                "白" -> views.setInt(dayIds[i][2], "setTextColor", ContextCompat.getColor(context, R.color.colorPrimary))
                "夜" -> views.setInt(dayIds[i][2], "setTextColor", ContextCompat.getColor(context, R.color.night_shift))
                "休" -> views.setInt(dayIds[i][2], "setTextColor", ContextCompat.getColor(context, R.color.rest_shift))
            }

            if (i == 0) {
                dateRangeBuilder.append("${currentDate.get(Calendar.MONTH) + 1}月${currentDate.get(Calendar.DAY_OF_MONTH)}日")
            } else if (i == 6) {
                dateRangeBuilder.append(" - ${currentDate.get(Calendar.MONTH) + 1}月${currentDate.get(Calendar.DAY_OF_MONTH)}日")
            }
        }

        views.setTextViewText(R.id.widgetDateRange, dateRangeBuilder.toString())

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.widgetTitle, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun getFirstDayOfWeek(date: Calendar): Calendar {
        val dayOfWeek = date.get(Calendar.DAY_OF_WEEK)
        val offset = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2
        val result = Calendar.getInstance().apply {
            timeInMillis = date.timeInMillis
            add(Calendar.DAY_OF_MONTH, -offset)
        }
        return result
    }

    private fun isToday(date: Calendar, today: Calendar): Boolean {
        return date.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                date.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                date.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)
    }

    private fun getShiftForDate(date: Calendar): String {
        val diffMillis = date.timeInMillis - baseCalendar.timeInMillis
        val diffDays = (diffMillis / (24 * 60 * 60 * 1000)).toInt()
        val index = ((diffDays % shiftCycle.size) + shiftCycle.size) % shiftCycle.size
        return shiftCycle[index]
    }
}