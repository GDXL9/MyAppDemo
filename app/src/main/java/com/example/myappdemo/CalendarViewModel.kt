package com.example.myappdemo

import androidx.lifecycle.ViewModel
import java.util.Calendar

/**
 * 日历视图模型。
 */
class CalendarViewModel : ViewModel() {
    var currentYear: Int = 0
        private set
    var currentMonth: Int = 0  // 0-based
        private set

    init {
        val now = Calendar.getInstance()
        currentYear = now.get(Calendar.YEAR)
        currentMonth = now.get(Calendar.MONTH)
    }

    fun updateMonth(year: Int, month: Int) {
        currentYear = year
        currentMonth = month
    }

    fun generateCalendarDays(): List<CalendarDay> {
        val calendar = Calendar.getInstance().apply {
            set(currentYear, currentMonth, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // SUN=1
        val offset = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val prevMonthCal = Calendar.getInstance().apply {
            set(currentYear, currentMonth, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_MONTH, -1)
        }
        val prevMonthDays = prevMonthCal.get(Calendar.DAY_OF_MONTH)

        val result = mutableListOf<CalendarDay>()

        // 上个月末尾
        for (i in offset downTo 1) {
            val day = prevMonthDays - i + 1
            val prevYear = if (currentMonth == 0) currentYear - 1 else currentYear
            val prevMonth = if (currentMonth == 0) 11 else currentMonth - 1
            val dateCal = Calendar.getInstance().apply {
                set(prevYear, prevMonth, day, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            result.add(CalendarDay(prevYear, prevMonth, day, ShiftConfig.getShiftForDate(dateCal), false))
        }

        // 当月
        for (day in 1..daysInMonth) {
            val dateCal = Calendar.getInstance().apply {
                set(currentYear, currentMonth, day, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            result.add(CalendarDay(currentYear, currentMonth, day, ShiftConfig.getShiftForDate(dateCal), true))
        }

        // 下个月开头
        val remaining = 42 - result.size
        for (day in 1..remaining) {
            val nextYear = if (currentMonth == 11) currentYear + 1 else currentYear
            val nextMonth = if (currentMonth == 11) 0 else currentMonth + 1
            val dateCal = Calendar.getInstance().apply {
                set(nextYear, nextMonth, day, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            result.add(CalendarDay(nextYear, nextMonth, day, ShiftConfig.getShiftForDate(dateCal), false))
        }

        return result
    }

    fun getMonthTitle(): String {
        val monthName = "${currentMonth + 1}月"
        return "${currentYear}年$monthName"
    }
}