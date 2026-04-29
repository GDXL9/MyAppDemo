package com.example.myappdemo

import android.app.AlertDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.widget.Button
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    // 排班周期：白、夜、休、白、夜、休、休、休
    private val shiftCycle = arrayOf("白", "夜", "休", "白", "夜", "休", "休", "休")

    // 基准日期：2026年4月1日 为第一个班次（白班）, 在这里可以更改第一个白班的开始日期
    // year: 年份
    // month: 月份，0-based (0=1月)，月份需要减去1
    // day: 日，1-based
    // hour: 小时，0-based (0=24:00)
    // minute: 分钟，0-based (0=00)
    // second: 秒，0-based (0=00)
    private val baseCalendar = Calendar.getInstance().apply {
        set(2026, Calendar.APRIL, 25, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private lateinit var monthYearButton: Button    // 月份和年份显示
    private lateinit var gridView: GridView  // 日历网格视图
    private lateinit var adapter: CalendarAdapter   // 日历适配器

    private var currentYear = 0    // 当前年份
    private var currentMonth = 0  // 当前月份，0-based (0=1月)

    // 初始化活动
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        monthYearButton = findViewById(R.id.monthYearButton)
        gridView = findViewById(R.id.calendarGrid)
        val prevButton: Button = findViewById(R.id.prevMonthButton)
        val nextButton: Button = findViewById(R.id.nextMonthButton)

        // 获取当前日期
        val now = Calendar.getInstance()
        currentYear = now.get(Calendar.YEAR)
        currentMonth = now.get(Calendar.MONTH)

        refreshCalendar()

        updateWidget()

        prevButton.setOnClickListener {
            currentMonth--
            if (currentMonth < 0) {
                currentMonth = 11
                currentYear--
            }
            refreshCalendar()
        }

        nextButton.setOnClickListener {
            currentMonth++
            if (currentMonth > 11) {
                currentMonth = 0
                currentYear++
            }
            refreshCalendar()
        }
        // 点击标题按钮弹出年月选择器
        monthYearButton.setOnClickListener {
            showYearMonthPicker()
        }
    }

    // 刷新当前月的日历
    private fun refreshCalendar() {
        // 更新标题
        val monthName = when (currentMonth) {
            0 -> "1月"
            1 -> "2月"
            2 -> "3月"
            3 -> "4月"
            4 -> "5月"
            5 -> "6月"
            6 -> "7月"
            7 -> "8月"
            8 -> "9月"
            9 -> "10月"
            10 -> "11月"
            else -> "12月"
        }
        monthYearButton.text = "$currentYear $monthName"   // 更新按钮文字

        // 生成当前月的日历数据（包含上月/下月占位）
        val daysList = generateCalendarDays(currentYear, currentMonth)
        adapter = CalendarAdapter(this, daysList) { year, month, day ->
            // 点击日期时显示完整班次信息
            val dateCal = Calendar.getInstance().apply {
                set(year, month, day, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val shift = getShiftForDate(dateCal)
            val message = when (shift) {
                "白" -> "（当日）8:00 ~ （当日）20:00"
                "夜" -> "（当日）20:00 ~ （次日）8:00"
                else -> "今天休息"
            }
            // 显示完整班次信息
            Toast.makeText(this, "${year}年${month+1}月${day}日:${shift}班 \n$message", Toast.LENGTH_SHORT).show()
        }
        gridView.adapter = adapter
    }

    // 弹出年月选择对话框
    private fun showYearMonthPicker() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_year_month, null)
        val yearPicker = dialogView.findViewById<android.widget.NumberPicker>(R.id.yearPicker)
        val monthPicker = dialogView.findViewById<android.widget.NumberPicker>(R.id.monthPicker)

        // 设置年份范围（可根据需要调整，如当前年份±10）
        val currentYearVal = Calendar.getInstance().get(Calendar.YEAR)
        yearPicker.minValue = currentYearVal - 10
        yearPicker.maxValue = currentYearVal + 10
        yearPicker.value = currentYear

        monthPicker.minValue = 1
        monthPicker.maxValue = 12
        monthPicker.value = currentMonth + 1   // 显示月份为 1~12

        AlertDialog.Builder(this)
            .setTitle("选择年月")
            .setView(dialogView)
            .setPositiveButton("确定") { _, _ ->
                val selectedYear = yearPicker.value
                val selectedMonth = monthPicker.value - 1   // 转为 0-based
                if (selectedYear != currentYear || selectedMonth != currentMonth) {
                    currentYear = selectedYear
                    currentMonth = selectedMonth
                    refreshCalendar()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 根据日期计算班次
     * @param date 日期
     * @return 班次（白、夜、休）
     */
    private fun getShiftForDate(date: Calendar): String {
        // 计算两个日期之间的天数差
        val diffMillis = date.timeInMillis - baseCalendar.timeInMillis
        val diffDays = (diffMillis / (24 * 60 * 60 * 1000)).toInt()
        // 取模得到周期内的索引（处理负数）
        val index = ((diffDays % shiftCycle.size) + shiftCycle.size) % shiftCycle.size
        return shiftCycle[index]
    }

    /**
     * 生成指定年月需要显示的日历数据（6行×7列 = 42个格子）
     * 包含上个月末尾几天、当月所有天、下个月开头几天
     */
    private fun generateCalendarDays(year: Int, month: Int): List<CalendarDay> {
        val calendar = Calendar.getInstance().apply {
            set(year, month, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // 当月第一天是星期几（周一=2，周日=1）-> 转换为周一为0的索引
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        // 偏移量：为了使周一排在第一列
        val offset = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2

        // 当月总天数
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // 上个月的最后几天
        val prevMonthCalendar = Calendar.getInstance().apply {
            set(year, month, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_MONTH, -1)
        }
        val prevMonthDays = prevMonthCalendar.get(Calendar.DAY_OF_MONTH)

        val result = mutableListOf<CalendarDay>()

        // 添加上个月的日期
        for (i in offset downTo 1) {
            val day = prevMonthDays - i + 1
            val prevYear = if (month == 0) year - 1 else year
            val prevMonth = if (month == 0) 11 else month - 1
            val dateCal = Calendar.getInstance().apply {
                set(prevYear, prevMonth, day, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            result.add(CalendarDay(prevYear, prevMonth, day, getShiftForDate(dateCal), false))
        }

        // 添加当月的日期
        for (day in 1..daysInMonth) {
            val dateCal = Calendar.getInstance().apply {
                set(year, month, day, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            result.add(CalendarDay(year, month, day, getShiftForDate(dateCal), true))
        }

        // 添加下个月的日期，凑满 42 个格子
        val remaining = 42 - result.size
        for (day in 1..remaining) {
            val nextYear = if (month == 11) year + 1 else year
            val nextMonth = if (month == 11) 0 else month + 1
            val dateCal = Calendar.getInstance().apply {
                set(nextYear, nextMonth, day, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            result.add(CalendarDay(nextYear, nextMonth, day, getShiftForDate(dateCal), false))
        }

        return result
    }

    // 日历格子数据类
    // year: 年份
    // month: 月份，0-based (0=1月)
    // day: 日，1-based
    // shift: 班次（白班、夜班、休息）
    // isCurrentMonth: 是否为当前月的日期
       data class CalendarDay(
        val year: Int,
        val month: Int,   // 0-based
        val day: Int,
        val shift: String,
        val isCurrentMonth: Boolean
    )

    /**
     * 更新桌面小组件
     * 当应用打开时自动调用，确保所有小组件显示最新的排班信息
     */
    private fun updateWidget() {
        updateWidget(ShiftWidget::class.java)
        updateWidget(ShiftWidget1x4::class.java)
    }

    /**
     * 更新指定类型的小组件
     * @param widgetClass 小组件类
     */
    private fun updateWidget(widgetClass: Class<out android.appwidget.AppWidgetProvider>) {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, widgetClass)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds.isNotEmpty()) {
            val intent = android.content.Intent(this, widgetClass)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            sendBroadcast(intent)
        }
    }
}