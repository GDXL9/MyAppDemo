package com.example.myappdemo

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myappdemo.databinding.ActivitySettingsBinding
import java.util.Calendar

/**
 * 班次设置界面
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    private var selectedYear = 2026
    private var selectedMonth = Calendar.APRIL
    private var selectedDay = 25
    private var selectedNightMode = SettingsManager.NIGHT_MODE_SYSTEM

    private val nightModeValues = intArrayOf(
        SettingsManager.NIGHT_MODE_SYSTEM,
        SettingsManager.NIGHT_MODE_LIGHT,
        SettingsManager.NIGHT_MODE_DARK
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadCurrentSettings()
        setupListeners()
        setupNightModeSpinner()
    }

    private fun loadCurrentSettings() {
        selectedYear = SettingsManager.getBaseYear(this)
        selectedMonth = SettingsManager.getBaseMonth(this)
        selectedDay = SettingsManager.getBaseDay(this)
        selectedNightMode = SettingsManager.getNightMode(this)

        updateBaseDateButton()
        binding.editShiftCycle.setText(SettingsManager.getShiftCycleString(this))
        updateCyclePreview()
    }

    private fun setupNightModeSpinner() {
        val nightModeLabels = resources.getStringArray(R.array.night_mode_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nightModeLabels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerNightMode.adapter = adapter

        val selectedIndex = nightModeValues.indexOf(selectedNightMode).coerceAtLeast(0)
        binding.spinnerNightMode.setSelection(selectedIndex)

        binding.spinnerNightMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedNightMode = nightModeValues[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

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

        binding.editShiftCycle.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                updateCyclePreview()
            }
        })

        binding.btnSave.setOnClickListener {
            saveSettings()
        }
    }

    private fun updateBaseDateButton() {
        binding.btnBaseDate.text = getString(
            R.string.date_format_full,
            selectedYear,
            selectedMonth + 1,
            selectedDay
        )
    }

    private fun updateCyclePreview() {
        val raw = binding.editShiftCycle.text.toString().trim()
        val count = if (raw.isEmpty()) 0 else raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.size
        binding.tvCyclePreview.text = if (count > 0) {
            getString(R.string.setting_cycle_preview_full, count)
        } else {
            getString(R.string.setting_cycle_invalid_error)
        }
    }

    private fun saveSettings() {
        val raw = binding.editShiftCycle.text.toString().trim()
        if (raw.isEmpty()) {
            Toast.makeText(this, getString(R.string.setting_cycle_empty_error), Toast.LENGTH_SHORT).show()
            return
        }

        val cycle = raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (cycle.isEmpty()) {
            Toast.makeText(this, getString(R.string.setting_cycle_invalid_error), Toast.LENGTH_SHORT).show()
            return
        }

        // 保存班次设置
        SettingsManager.saveShiftCycle(this, cycle)
        SettingsManager.saveBaseDate(this, selectedYear, selectedMonth, selectedDay)
        ShiftConfig.init(this)

        // 保存夜间模式
        val previousNightMode = SettingsManager.getNightMode(this)
        SettingsManager.saveNightMode(this, selectedNightMode)
        if (previousNightMode != selectedNightMode) {
            ThemeHelper.applyMode(selectedNightMode)
        }

        // 更新所有小组件
        WidgetViewsFactory.updateAllWidgets(this)

        Toast.makeText(this, getString(R.string.setting_save_success), Toast.LENGTH_SHORT).show()
        finish()
    }
}
