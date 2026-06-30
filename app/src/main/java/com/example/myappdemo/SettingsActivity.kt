package com.example.myappdemo

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myappdemo.databinding.ActivitySettingsBinding
import java.util.Calendar

/**
 * 班次设置界面
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    // 当前选择的基准日期（0-based month）
    private var selectedYear = 2026
    private var selectedMonth = Calendar.APRIL   // 0-based
    private var selectedDay = 25

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 读取当前设置
        loadCurrentSettings()
        setupListeners()
    }

    private fun loadCurrentSettings() {
        selectedYear = SettingsManager.getBaseYear(this)
        selectedMonth = SettingsManager.getBaseMonth(this)
        selectedDay = SettingsManager.getBaseDay(this)

        updateBaseDateButton()
        binding.editShiftCycle.setText(SettingsManager.getShiftCycleString(this))
        updateCyclePreview()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        // 基准日期选择器
        binding.btnBaseDate.setOnClickListener {
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    selectedYear = year
                    selectedMonth = month
                    selectedDay = dayOfMonth
                    updateBaseDateButton()
                },
                selectedYear,
                selectedMonth,
                selectedDay
            )
            datePicker.show()
        }

        // 班次周期文本变化时更新预览
        binding.editShiftCycle.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                updateCyclePreview()
            }
        })

        // 保存
        binding.btnSave.setOnClickListener {
            saveSettings()
        }
    }

    private fun updateBaseDateButton() {
        binding.btnBaseDate.text = "${selectedYear}年${selectedMonth + 1}月${selectedDay}日"
    }

    private fun updateCyclePreview() {
        val raw = binding.editShiftCycle.text.toString().trim()
        val count = if (raw.isEmpty()) 0 else raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.size
        binding.tvCyclePreview.text = if (count > 0) "当前周期：${count}天一循环" else "请输入有效的班次周期"
    }

    private fun saveSettings() {
        val raw = binding.editShiftCycle.text.toString().trim()
        if (raw.isEmpty()) {
            Toast.makeText(this, "班次周期不能为空", Toast.LENGTH_SHORT).show()
            return
        }

        val cycle = raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (cycle.isEmpty()) {
            Toast.makeText(this, "请输入有效的班次周期", Toast.LENGTH_SHORT).show()
            return
        }

        // 保存到 SharedPreferences
        SettingsManager.saveShiftCycle(this, cycle)
        SettingsManager.saveBaseDate(this, selectedYear, selectedMonth, selectedDay)

        // 重新初始化 ShiftConfig
        ShiftConfig.init(this)

        // 更新所有小组件
        WidgetViewsFactory.updateAllWidgets(this)

        Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show()
        finish()
    }
}
