package com.example.myappdemo

import java.util.Calendar

/**
 * 全局班次配置，所有组件共用
 */
object ShiftConfig {
    // 8天一个周期
    val shiftCycle = arrayOf("白", "夜", "休", "白", "夜", "休", "休", "休")

    // 基准日期：2026-04-25 00:00:00 (第一个白班)
    val baseCalendar: Calendar = Calendar.getInstance().apply {
        set(2026, Calendar.APRIL, 25, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }

    /**
     * 根据日期计算班次
     */
    fun getShiftForDate(date: Calendar): String {
        val diffMillis = date.timeInMillis - baseCalendar.timeInMillis
        val diffDays = (diffMillis / (24 * 60 * 60 * 1000)).toInt()
        val index = ((diffDays % shiftCycle.size) + shiftCycle.size) % shiftCycle.size
        return shiftCycle[index]
    }
}