package com.example.myappdemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class CalendarAdapter(
    private val context: android.content.Context,
    private val daysList: List<CalendarDay>,
    private val todayCheck: (CalendarDay) -> Boolean,
    private val onItemClick: (year: Int, month: Int, day: Int) -> Unit
) : BaseAdapter() {

    override fun getCount() = daysList.size
    override fun getItem(position: Int) = daysList[position]
    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_calendar_day, parent, false)

        val dayText = view.findViewById<TextView>(R.id.dayText)
        val shiftText = view.findViewById<TextView>(R.id.shiftText)
        val item = daysList[position]

        dayText.text = item.day.toString()
        shiftText.text = item.shift

        val bgRes = when (item.shift) {
            "白" -> R.drawable.bg_shift_day
            "夜" -> R.drawable.bg_shift_night
            else -> R.drawable.bg_shift_rest
        }
        view.setBackgroundResource(bgRes)

        val textColor = if (item.isCurrentMonth) {
            context.getColor(android.R.color.black)
        } else {
            context.getColor(R.color.inactive_month_text)
        }
        dayText.setTextColor(textColor)
        shiftText.setTextColor(textColor)

        if (todayCheck(item)) {
            view.setBackgroundResource(R.drawable.bg_today_highlight)
        }

        view.setOnClickListener {
            onItemClick(item.year, item.month, item.day)
        }
        return view
    }
}