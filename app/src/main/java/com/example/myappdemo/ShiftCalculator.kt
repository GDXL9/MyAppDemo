package com.example.myappdemo

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** 与 Android 无关的排班计算器，使用自然日计算，避免夏令时造成毫秒差偏移。 */
object ShiftCalculator {
    fun getShiftForDate(date: LocalDate, baseDate: LocalDate, cycle: List<String>): String {
        if (cycle.isEmpty()) return "休"
        val diffDays = ChronoUnit.DAYS.between(baseDate, date)
        val index = Math.floorMod(diffDays, cycle.size.toLong()).toInt()
        return cycle[index]
    }
}
