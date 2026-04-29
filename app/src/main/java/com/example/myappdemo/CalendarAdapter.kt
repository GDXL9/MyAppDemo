package com.example.myappdemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.myappdemo.MainActivity.CalendarDay
import java.util.Calendar

class CalendarAdapter(
    private val context: android.content.Context,
    private val daysList: List<CalendarDay>,
    private val onItemClick: (year: Int, month: Int, day: Int) -> Unit
) : BaseAdapter() {

    // 获取数据项数量
    override fun getCount(): Int = daysList.size

    // 获取指定位置的数据项
    override fun getItem(position: Int): Any = daysList[position]

    // 获取指定位置的数据项的唯一标识符
    override fun getItemId(position: Int): Long = position.toLong()

    // 获取指定位置的数据项的视图
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_calendar_day, parent, false)

        val dayText = view.findViewById<TextView>(R.id.dayText)
        val shiftText = view.findViewById<TextView>(R.id.shiftText)
        val item = daysList[position]

        dayText.text = item.day.toString()
        shiftText.text = item.shift

        // 根据班次选择对应的圆角背景资源
        val bgRes = when (item.shift) {
            "白" -> R.drawable.bg_shift_day
            "夜" -> R.drawable.bg_shift_night
            else -> R.drawable.bg_shift_rest
        }
        view.setBackgroundResource(bgRes)

        // 非当前月的日期文字颜色变淡
        if (!item.isCurrentMonth) {
            dayText.setTextColor(context.getColor(R.color.inactive_month_text))
            shiftText.setTextColor(context.getColor(R.color.inactive_month_text))
        } else {
            dayText.setTextColor(context.getColor(android.R.color.black))
            shiftText.setTextColor(context.getColor(android.R.color.black))
        }

        // 高亮今天
        val today = Calendar.getInstance()
        val currentYear = today.get(Calendar.YEAR)
        val currentMonth = today.get(Calendar.MONTH)
        val currentDay = today.get(Calendar.DAY_OF_MONTH)
        if (item.year == currentYear && item.month == currentMonth && item.day == currentDay) {
            view.setBackgroundResource(R.drawable.bg_today_highlight)
        }

        // 点击日期时触发点击事件
        view.setOnClickListener {
            onItemClick(item.year, item.month, item.day)
        }

        return view
    }
}