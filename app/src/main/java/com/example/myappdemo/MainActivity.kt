package com.example.myappdemo

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.myappdemo.databinding.ActivityMainBinding
import java.util.Calendar

/**
 * 主活动。
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: CalendarViewModel
    private lateinit var adapter: CalendarAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(CalendarViewModel::class.java)

        // 初始化 ShiftConfig（从 SharedPreferences 读取）
        ShiftConfig.init(this)

        refreshCalendar()

        // 更新小组件
        WidgetViewsFactory.updateAllWidgets(this)
        WidgetUpdateReceiver.setDailyUpdate(this)

        binding.prevMonthButton.setOnClickListener { changeMonth(-1) }
        binding.nextMonthButton.setOnClickListener { changeMonth(1) }
        binding.monthYearButton.setOnClickListener { showYearMonthPicker() }
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.btnRefreshWeather.setOnClickListener { refreshWeather(forceRefresh = true) }
        refreshWeather()
        WeatherUpdateJobService.ensurePeriodic(this)
    }

    override fun onResume() {
        super.onResume()
        // 从设置界面返回后刷新日历
        ShiftConfig.init(this)
        refreshCalendar()
        refreshWeather()
    }

    private fun refreshWeather(forceRefresh: Boolean = false) {
        val city = WeatherCities.findById(SettingsManager.getWeatherCityId(this))
        binding.weatherCity.text = city.name
        binding.weatherDetail.text = getString(R.string.weather_loading)
        binding.btnRefreshWeather.isEnabled = false
        WeatherRepository.load(this, forceRefresh) { weather ->
            binding.btnRefreshWeather.isEnabled = true
            if (weather == null) {
                binding.weatherIcon.text = getString(R.string.weather_default_icon)
                binding.weatherDetail.text = getString(R.string.weather_unavailable)
            } else {
                binding.weatherCity.text = weather.cityName
                binding.weatherIcon.text = weather.icon
                binding.weatherDetail.text = "${weather.temperature}°C · ${weather.description}"
                WidgetViewsFactory.updateAllWidgets(this)
            }
        }
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
        binding.monthYearButton.text = viewModel.getMonthTitle()

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
                getString(R.string.shift_day_time).take(1) -> getString(R.string.shift_day_time)
                getString(R.string.shift_night_time).take(1) -> getString(R.string.shift_night_time)
                else -> getString(R.string.shift_rest)
            }
            val toastText = getString(
                R.string.shift_format,
                year,
                month + 1,
                day,
                shift,
                msg
            )
            Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show()
        }
        binding.calendarGrid.adapter = adapter
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
            .setTitle(getString(R.string.year_month))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                val year = yearPicker.value
                val month = monthPicker.value - 1
                if (year != viewModel.currentYear || month != viewModel.currentMonth) {
                    viewModel.updateMonth(year, month)
                    refreshCalendar()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}
