package com.example.myappdemo

import android.content.Context
import java.util.Calendar

/**
 * 班次配置，从 SettingsManager 动态读取，所有组件共用
 */
object ShiftConfig {

    // 默认值（作为 fallback，仅在未初始化时使用）
    private val DEFAULT_CYCLE = arrayOf("白", "夜", "休", "白", "夜", "休", "休", "休")
    private val DEFAULT_BASE_CALENDAR: Calendar = Calendar.getInstance().apply {
        set(2026, Calendar.APRIL, 25, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }

    // 当前生效配置
    private var shiftCycle: Array<String> = DEFAULT_CYCLE
    private var baseCalendar: Calendar = DEFAULT_BASE_CALENDAR

    /**
     * 初始化配置（应用启动或设置变更时调用）
     */
    fun init(context: Context) {
        val cycle = SettingsManager.getShiftCycle(context)
        if (cycle.isNotEmpty()) {
            shiftCycle = cycle.toTypedArray()
        }
        baseCalendar = SettingsManager.getBaseCalendar(context)
    }

    fun getCycle(): Array<String> = shiftCycle

    fun getBaseCalendar(): Calendar = baseCalendar

    /**
     * 根据日期计算班次
     */
    fun getShiftForDate(date: Calendar): String {
        if (shiftCycle.isEmpty()) return "休"
        val diffMillis = date.timeInMillis - baseCalendar.timeInMillis
        val diffDays = (diffMillis / (24 * 60 * 60 * 1000)).toInt()
        val index = ((diffDays % shiftCycle.size) + shiftCycle.size) % shiftCycle.size
        return shiftCycle[index]
    }
}
