package com.example.myappdemo

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import java.util.*

/**
 * 桌面小部件 - 显示一周排班情况
 * 
 * 该小部件用于在手机桌面上显示当前一周的排班信息，包括：
 * - 周一到周日的日期和班次
 * - 当天日期高亮显示
 * - 不同班次用不同颜色区分
 * - 点击可打开主应用
 */
class ShiftWidget : AppWidgetProvider() {

    /**
     * 排班周期数组：白班、夜班、休息交替循环
     * 周期为8天：白、夜、休、白、夜、休、休、休
     */
    private val shiftCycle = arrayOf("白", "夜", "休", "白", "夜", "休", "休", "休")

    /**
     * 基准日期：排班周期的起始日期
     * 设定为2026年4月25日，这一天为第一个白班
     */
    private val baseCalendar = Calendar.getInstance().apply {
        set(2026, Calendar.APRIL, 25, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }

    /**
     * 星期名称数组（周一到周日）
     */
    private val dayNames = arrayOf("一", "二", "三", "四", "五", "六", "日")

    /**
     * 当小部件需要更新时调用
     * 
     * @param context 上下文对象
     * @param appWidgetManager 小部件管理器
     * @param appWidgetIds 需要更新的小部件ID数组
     */
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // 遍历所有需要更新的小部件，逐一更新
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    /**
     * 当第一个小部件实例被添加时调用
     */
    override fun onEnabled(context: Context) {
        // 可在此处添加初始化逻辑
    }

    /**
     * 当最后一个小部件实例被删除时调用
     */
    override fun onDisabled(context: Context) {
        // 可在此处添加清理逻辑
    }

    /**
     * 更新单个小部件的显示内容
     * 
     * @param context 上下文对象
     * @param appWidgetManager 小部件管理器
     * @param appWidgetId 要更新的小部件ID
     */
    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        // 创建 RemoteViews 对象，加载小部件布局
        val views = RemoteViews(context.packageName, R.layout.widget_week_shift)

        // 获取今天的日期（重置时分秒为0，用于日期比较）
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        // 获取本周第一天（周一）的日期
        val firstDayOfWeek = getFirstDayOfWeek(today)

        // 定义一周七天对应的视图ID数组
        // 每个元素包含：星期名称、日期、班次三个TextView的ID
        val dayIds = arrayOf(
            intArrayOf(R.id.day1Name, R.id.day1Date, R.id.day1Shift),
            intArrayOf(R.id.day2Name, R.id.day2Date, R.id.day2Shift),
            intArrayOf(R.id.day3Name, R.id.day3Date, R.id.day3Shift),
            intArrayOf(R.id.day4Name, R.id.day4Date, R.id.day4Shift),
            intArrayOf(R.id.day5Name, R.id.day5Date, R.id.day5Shift),
            intArrayOf(R.id.day6Name, R.id.day6Date, R.id.day6Shift),
            intArrayOf(R.id.day7Name, R.id.day7Date, R.id.day7Shift)
        )

        // 用于构建日期范围字符串（如：4/29 - 5/5）
        val dateRangeBuilder = StringBuilder()

        // 遍历一周七天，设置每一天的显示内容
        for (i in 0..6) {
            // 计算当前日期（本周第一天 + i天）
            val currentDate = Calendar.getInstance().apply {
                timeInMillis = firstDayOfWeek.timeInMillis
                add(Calendar.DAY_OF_MONTH, i)
            }

            // 获取星期名称、日期字符串、班次信息
            val dayName = dayNames[i]
            val dateStr = "${currentDate.get(Calendar.MONTH) + 1}/${currentDate.get(Calendar.DAY_OF_MONTH)}"
            val shift = getShiftForDate(currentDate)

            // 设置星期名称、日期、班次的显示文本
            views.setTextViewText(dayIds[i][0], dayName)
            views.setTextViewText(dayIds[i][1], dateStr)
            views.setTextViewText(dayIds[i][2], shift)

            // 根据班次类型设置不同的颜色
            when (shift) {
                "白" -> views.setInt(dayIds[i][2], "setTextColor", ContextCompat.getColor(context, R.color.blue))
                "夜" -> views.setInt(dayIds[i][2], "setTextColor", ContextCompat.getColor(context, R.color.gray))
                "休" -> views.setInt(dayIds[i][2], "setTextColor", ContextCompat.getColor(context, R.color.pink))
            }

            // 如果是今天，高亮显示日期和班次
            if (isToday(currentDate, today)) {
                views.setInt(dayIds[i][0], "setTextColor", ContextCompat.getColor(context, R.color.today_highlight))
                views.setInt(dayIds[i][1], "setTextColor", ContextCompat.getColor(context, R.color.today_highlight))
                views.setInt(dayIds[i][2], "setTextColor", ContextCompat.getColor(context, R.color.today_highlight))
            }

            // 构建日期范围字符串（只取周一和周日）
            if (i == 0) {
                dateRangeBuilder.append("${currentDate.get(Calendar.MONTH) + 1}月${currentDate.get(Calendar.DAY_OF_MONTH)}日")
            } else if (i == 6) {
                dateRangeBuilder.append(" - ${currentDate.get(Calendar.MONTH) + 1}月${currentDate.get(Calendar.DAY_OF_MONTH)}日")
            }
        }

        // 设置日期范围显示
        views.setTextViewText(R.id.widgetDateRange, dateRangeBuilder.toString())

        // 设置点击标题打开主应用的功能
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.widgetTitle, pendingIntent)

        // 更新小部件显示
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    /**
     * 获取指定日期所在周的第一天（周一）
     * 
     * @param date 参考日期
     * @return 该周周一的 Calendar 对象
     */
    private fun getFirstDayOfWeek(date: Calendar): Calendar {
        // 获取日期是星期几（周日=1，周一=2，...，周六=7）
        val dayOfWeek = date.get(Calendar.DAY_OF_WEEK)
        // 计算偏移量：将日期调整到周一
        val offset = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2
        // 创建结果日期并调整到周一
        val result = Calendar.getInstance().apply {
            timeInMillis = date.timeInMillis
            add(Calendar.DAY_OF_MONTH, -offset)
        }
        return result
    }

    /**
     * 判断给定日期是否是今天
     * 
     * @param date 要判断的日期
     * @param today 今天的日期
     * @return 如果是今天返回true，否则返回false
     */
    private fun isToday(date: Calendar, today: Calendar): Boolean {
        return date.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                date.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                date.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)
    }

    /**
     * 根据日期计算对应的班次
     * 
     * @param date 要计算班次的日期
     * @return 班次字符串（白、夜、休）
     */
    private fun getShiftForDate(date: Calendar): String {
        // 计算与基准日期的毫秒差
        val diffMillis = date.timeInMillis - baseCalendar.timeInMillis
        // 转换为天数差
        val diffDays = (diffMillis / (24 * 60 * 60 * 1000)).toInt()
        // 计算周期索引（处理负数情况）
        val index = ((diffDays % shiftCycle.size) + shiftCycle.size) % shiftCycle.size
        // 返回对应的班次
        return shiftCycle[index]
    }
}