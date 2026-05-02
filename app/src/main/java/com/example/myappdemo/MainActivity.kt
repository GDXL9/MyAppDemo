package com.example.myappdemo

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.GridView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var monthYearButton: Button
    private lateinit var gridView: GridView
    private lateinit var viewModel: CalendarViewModel
    private lateinit var adapter: CalendarAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(CalendarViewModel::class.java)

        monthYearButton = findViewById(R.id.monthYearButton)
        gridView = findViewById(R.id.calendarGrid)
        val prevButton: Button = findViewById(R.id.prevMonthButton)
        val nextButton: Button = findViewById(R.id.nextMonthButton)

        refreshCalendar()

        // 更新小组件
        WidgetViewsFactory.updateAllWidgets(this)
        WidgetUpdateReceiver.setDailyUpdate(this)

        prevButton.setOnClickListener { changeMonth(-1) }
        nextButton.setOnClickListener { changeMonth(1) }
        monthYearButton.setOnClickListener { showYearMonthPicker() }
    }

    private fun changeMonth(delta: Int) {
        var newMonth = viewModel.currentMonth + delta
        var newYear = viewModel.currentYear
        if (newMonth < 0) {
            newMonth = 11
            newYear--
        } else if (newMonth > 11) {
            newMonth = 0
            newYear++
        }
        viewModel.updateMonth(newYear, newMonth)
        refreshCalendar()
    }

    private fun refreshCalendar() {
        monthYearButton.text = viewModel.getMonthTitle()

        val daysList = viewModel.generateCalendarDays()

        val today = Calendar.getInstance()
        val todayYear = today.get(Calendar.YEAR)
        val todayMonth = today.get(Calendar.MONTH)
        val todayDay = today.get(Calendar.DAY_OF_MONTH)

        adapter = CalendarAdapter(
            this,
            daysList,
            todayCheck = { item ->
                item.year == todayYear && item.month == todayMonth && item.day == todayDay
            }
        ) { year, month, day ->
            val shift = ShiftConfig.getShiftForDate(
                Calendar.getInstance().apply { set(year, month, day, 0, 0, 0) }
            )
            val msg = when (shift) {
                "白" -> "（当日）8:00 ~ 20:00"
                "夜" -> "（当日）20:00 ~ 次日8:00"
                else -> "今天休息"
            }
            Toast.makeText(this, "${year}年${month+1}月${day}日:${shift}班\n$msg", Toast.LENGTH_SHORT).show()
        }
        gridView.adapter = adapter
    }

    private fun showYearMonthPicker() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_year_month, null)
        val yearPicker = dialogView.findViewById<android.widget.NumberPicker>(R.id.yearPicker)
        val monthPicker = dialogView.findViewById<android.widget.NumberPicker>(R.id.monthPicker)

        val now = Calendar.getInstance()
        yearPicker.minValue = now.get(Calendar.YEAR) - 10
        yearPicker.maxValue = now.get(Calendar.YEAR) + 10
        yearPicker.value = viewModel.currentYear

        monthPicker.minValue = 1
        monthPicker.maxValue = 12
        monthPicker.value = viewModel.currentMonth + 1

        AlertDialog.Builder(this)
            .setTitle("选择年月")
            .setView(dialogView)
            .setPositiveButton("确定") { _, _ ->
                val year = yearPicker.value
                val month = monthPicker.value - 1
                if (year != viewModel.currentYear || month != viewModel.currentMonth) {
                    viewModel.updateMonth(year, month)
                    refreshCalendar()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
}