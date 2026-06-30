package com.example.myappdemo

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar

/**
 * 班次设置持久化管理（SharedPreferences）
 */
object SettingsManager {

    private const val PREFS_NAME = "shift_settings"

    // Keys
    private const val KEY_SHIFT_CYCLE = "shift_cycle"
    private const val KEY_BASE_YEAR = "base_year"
    private const val KEY_BASE_MONTH = "base_month"
    private const val KEY_BASE_DAY = "base_day"

    private const val DEFAULT_CYCLE = "白,夜,休,白,夜,休,休,休"
    private const val DEFAULT_YEAR = 2026
    private const val DEFAULT_MONTH = Calendar.APRIL   // 0-based
    private const val DEFAULT_DAY = 25

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // --- 班次周期 ---

    fun getShiftCycle(context: Context): List<String> {
        val raw = getPrefs(context).getString(KEY_SHIFT_CYCLE, DEFAULT_CYCLE) ?: DEFAULT_CYCLE
        return raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    fun getShiftCycleString(context: Context): String {
        return getPrefs(context).getString(KEY_SHIFT_CYCLE, DEFAULT_CYCLE) ?: DEFAULT_CYCLE
    }

    fun saveShiftCycle(context: Context, cycle: List<String>) {
        getPrefs(context).edit().putString(KEY_SHIFT_CYCLE, cycle.joinToString(",")).apply()
    }

    // --- 基准日期 ---

    fun getBaseCalendar(context: Context): Calendar {
        val prefs = getPrefs(context)
        val year = prefs.getInt(KEY_BASE_YEAR, DEFAULT_YEAR)
        val month = prefs.getInt(KEY_BASE_MONTH, DEFAULT_MONTH)
        val day = prefs.getInt(KEY_BASE_DAY, DEFAULT_DAY)
        return Calendar.getInstance().apply {
            set(year, month, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    fun saveBaseDate(context: Context, year: Int, month: Int, day: Int) {
        getPrefs(context).edit()
            .putInt(KEY_BASE_YEAR, year)
            .putInt(KEY_BASE_MONTH, month)
            .putInt(KEY_BASE_DAY, day)
            .apply()
    }

    /**
     * 获取基准日期各字段，用于设置界面回显
     */
    fun getBaseYear(context: Context): Int =
        getPrefs(context).getInt(KEY_BASE_YEAR, DEFAULT_YEAR)

    fun getBaseMonth(context: Context): Int =
        getPrefs(context).getInt(KEY_BASE_MONTH, DEFAULT_MONTH)   // 0-based

    fun getBaseDay(context: Context): Int =
        getPrefs(context).getInt(KEY_BASE_DAY, DEFAULT_DAY)
}
