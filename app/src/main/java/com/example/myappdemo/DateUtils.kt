package com.example.myappdemo

import java.util.Calendar

/**
 * 日期计算与格式化工具
 */
object DateUtils {

    /**
     * 获取指定日期所在周的周一（以周一为一周第一天）
     */
    fun getFirstDayOfWeek(date: Calendar): Calendar {
        val dayOfWeek = date.get(Calendar.DAY_OF_WEEK)   // SUN=1 .. SAT=7
        val offset = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2
        return Calendar.getInstance().apply {
            timeInMillis = date.timeInMillis
            add(Calendar.DAY_OF_MONTH, -offset)
            // 归零时分秒，保证一致性
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    /**
     * 判断是否为今天
     */
    fun isToday(date: Calendar): Boolean {
        val today = Calendar.getInstance()
        return date.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                date.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                date.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)
    }

    /** 格式化为 "M/d" 短日期 */
    fun formatShortDate(date: Calendar): String =
        "${date.get(Calendar.MONTH) + 1}/${date.get(Calendar.DAY_OF_MONTH)}"

    /** 格式化为 "M月d日" 中文日期 */
    fun formatChineseDate(date: Calendar): String =
        "${date.get(Calendar.MONTH) + 1}月${date.get(Calendar.DAY_OF_MONTH)}日"

    /** 格式化周范围字符串，如 "4月29日 - 5月5日" */
    fun formatWeekRange(start: Calendar, end: Calendar): String =
        "${formatChineseDate(start)} - ${formatChineseDate(end)}"
}