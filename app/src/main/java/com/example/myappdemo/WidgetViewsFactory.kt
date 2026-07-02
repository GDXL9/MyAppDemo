package com.example.myappdemo

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import java.util.Calendar

/**
 * 统一构建小组件 RemoteViews，避免代码重复
 */
object WidgetViewsFactory {

    /**
     * 检测当前应用是否处于夜间模式（优先使用 AppCompatDelegate 设置，其次跟随系统）
     */
    private fun isNightMode(context: Context): Boolean {
        return when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_YES -> true
            AppCompatDelegate.MODE_NIGHT_NO -> false
            else -> {
                (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                        Configuration.UI_MODE_NIGHT_YES
            }
        }
    }

    /**
     * 根据当前主题获取默认文字颜色
     */
    private fun getWidgetTextColor(context: Context): Int {
        return if (isNightMode(context)) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
    }

    /**
     * 根据当前主题获取日期范围文字颜色
     */
    private fun getWidgetDateRangeColor(context: Context): Int {
        return if (isNightMode(context)) 0xFFB0BEC5.toInt() else 0xFF424242.toInt()
    }

    /**
     * 构建标准小组件（带日期范围标题）的 RemoteViews
     */
    fun createShiftWidgetViews(context: Context): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_week_shift)
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val firstDay = DateUtils.getFirstDayOfWeek(today)

        val dayIds = arrayOf(
            intArrayOf(R.id.day1Name, R.id.day1Date, R.id.day1Shift),
            intArrayOf(R.id.day2Name, R.id.day2Date, R.id.day2Shift),
            intArrayOf(R.id.day3Name, R.id.day3Date, R.id.day3Shift),
            intArrayOf(R.id.day4Name, R.id.day4Date, R.id.day4Shift),
            intArrayOf(R.id.day5Name, R.id.day5Date, R.id.day5Shift),
            intArrayOf(R.id.day6Name, R.id.day6Date, R.id.day6Shift),
            intArrayOf(R.id.day7Name, R.id.day7Date, R.id.day7Shift)
        )

        val dayNames = arrayOf("一", "二", "三", "四", "五", "六", "日")
        val defaultTextColor = getWidgetTextColor(context)

        for (i in 0..6) {
            val current = Calendar.getInstance().apply {
                timeInMillis = firstDay.timeInMillis
                add(Calendar.DAY_OF_MONTH, i)
            }
            val shift = ShiftConfig.getShiftForDate(current)
            val dateStr = DateUtils.formatShortDate(current)

            views.setTextViewText(dayIds[i][0], dayNames[i])
            views.setTextViewText(dayIds[i][1], dateStr)
            views.setTextViewText(dayIds[i][2], shift)

            // 默认颜色
            views.setInt(dayIds[i][0], "setTextColor", defaultTextColor)
            views.setInt(dayIds[i][1], "setTextColor", defaultTextColor)

            views.setInt(dayIds[i][2], "setTextColor", when (shift) {
                "白" -> ContextCompat.getColor(context, R.color.accent_blue)
                "夜" -> ContextCompat.getColor(context, R.color.accent_gray)
                else -> ContextCompat.getColor(context, R.color.accent_pink)
            })

            // 今天高亮
            if (DateUtils.isToday(current)) {
                val highlight = ContextCompat.getColor(context, R.color.highlight_today)
                views.setInt(dayIds[i][0], "setTextColor", highlight)
                views.setInt(dayIds[i][1], "setTextColor", highlight)
                views.setInt(dayIds[i][2], "setTextColor", highlight)
            }
        }

        // 日期范围标题
        val start = Calendar.getInstance().apply { timeInMillis = firstDay.timeInMillis }
        val end = Calendar.getInstance().apply {
            timeInMillis = firstDay.timeInMillis
            add(Calendar.DAY_OF_MONTH, 6)
        }
        views.setTextViewText(R.id.widgetDateRange, DateUtils.formatWeekRange(start, end))
        views.setInt(R.id.widgetDateRange, "setTextColor", getWidgetDateRangeColor(context))

        // 标题文字颜色
        views.setInt(R.id.widgetTitle, "setTextColor", defaultTextColor)

        // 点击标题打开应用
        val intent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.widgetTitle, pi)

        return views
    }

    /**
     * 构建1x4精简小组件的 RemoteViews
     */
    fun createShift1x4WidgetViews(context: Context): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_shift_1x4)
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val firstDay = DateUtils.getFirstDayOfWeek(today)

        val dayIds = arrayOf(
            intArrayOf(R.id.day1Name, R.id.day1Date, R.id.day1Shift),
            intArrayOf(R.id.day2Name, R.id.day2Date, R.id.day2Shift),
            intArrayOf(R.id.day3Name, R.id.day3Date, R.id.day3Shift),
            intArrayOf(R.id.day4Name, R.id.day4Date, R.id.day4Shift),
            intArrayOf(R.id.day5Name, R.id.day5Date, R.id.day5Shift),
            intArrayOf(R.id.day6Name, R.id.day6Date, R.id.day6Shift),
            intArrayOf(R.id.day7Name, R.id.day7Date, R.id.day7Shift)
        )
        val dayNames = arrayOf("一", "二", "三", "四", "五", "六", "日")
        val defaultTextColor = getWidgetTextColor(context)

        for (i in 0..6) {
            val current = Calendar.getInstance().apply {
                timeInMillis = firstDay.timeInMillis
                add(Calendar.DAY_OF_MONTH, i)
            }
            val shift = ShiftConfig.getShiftForDate(current)
            val dateStr = DateUtils.formatShortDate(current)

            views.setTextViewText(dayIds[i][0], dayNames[i])
            views.setTextViewText(dayIds[i][1], dateStr)
            views.setTextViewText(dayIds[i][2], shift)

            views.setInt(dayIds[i][0], "setTextColor", defaultTextColor)
            views.setInt(dayIds[i][1], "setTextColor", defaultTextColor)
            views.setInt(dayIds[i][2], "setTextColor", when (shift) {
                "白" -> ContextCompat.getColor(context, R.color.accent_blue)
                "夜" -> ContextCompat.getColor(context, R.color.accent_gray)
                else -> ContextCompat.getColor(context, R.color.accent_pink)
            })

            if (DateUtils.isToday(current)) {
                val highlight = ContextCompat.getColor(context, R.color.highlight_today)
                views.setInt(dayIds[i][0], "setTextColor", highlight)
                views.setInt(dayIds[i][1], "setTextColor", highlight)
                views.setInt(dayIds[i][2], "setTextColor", highlight)
            }
        }

        // 为每个格子添加点击事件
        val intent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val clickIds = arrayOf(R.id.day1, R.id.day2, R.id.day3, R.id.day4, R.id.day5, R.id.day6, R.id.day7)
        clickIds.forEach { views.setOnClickPendingIntent(it, pi) }

        return views
    }

    /**
     * 直接更新所有已添加的两种小组件（无需发送广播）
     */
    fun updateAllWidgets(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        // 标准小组件
        val component1 = android.content.ComponentName(context, ShiftWidget::class.java)
        val ids1 = manager.getAppWidgetIds(component1)
        ids1.forEach { id ->
            manager.updateAppWidget(id, createShiftWidgetViews(context))
        }
        // 1x4小组件
        val component2 = android.content.ComponentName(context, ShiftWidget1x4::class.java)
        val ids2 = manager.getAppWidgetIds(component2)
        ids2.forEach { id ->
            manager.updateAppWidget(id, createShift1x4WidgetViews(context))
        }
    }
}
