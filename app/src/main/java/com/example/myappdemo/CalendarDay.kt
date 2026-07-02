package com.example.myappdemo

/**
 * 日历格子数据类
 * @param year 年份
 * @param month 月份，0-based (0=1月)
 * @param day 日，1-based
 * @param shift 班次（白、夜、休）
 * @param isCurrentMonth 是否属于当前显示月份
 */
data class CalendarDay(
    val year: Int,
    val month: Int,   // 0-based
    val day: Int,
    val shift: String,
    val isCurrentMonth: Boolean
)