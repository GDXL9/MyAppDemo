package com.example.myappdemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.myappdemo.databinding.ItemCalendarDayBinding

/**
 * 日历适配器。
 */
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
        val binding = if (convertView == null) {
            val itemBinding = ItemCalendarDayBinding.inflate(LayoutInflater.from(context), parent, false)
            itemBinding.root.tag = itemBinding
            itemBinding
        } else {
            convertView.tag as ItemCalendarDayBinding
        }

        val item = daysList[position]

        binding.dayText.text = item.day.toString()
        binding.shiftText.text = item.shift

        val bgRes = when (item.shift) {
            "白" -> R.drawable.bg_shift_day
            "夜" -> R.drawable.bg_shift_night
            else -> R.drawable.bg_shift_rest
        }
        binding.root.setBackgroundResource(bgRes)

        val textColor = if (item.isCurrentMonth) {
            context.getColor(android.R.color.black)
        } else {
            context.getColor(R.color.inactive_month_text)
        }
        binding.dayText.setTextColor(textColor)
        binding.shiftText.setTextColor(textColor)

        if (todayCheck(item)) {
            binding.root.setBackgroundResource(R.drawable.bg_today_highlight)
        }

        binding.root.setOnClickListener {
            onItemClick(item.year, item.month, item.day)
        }

        return binding.root
    }
}
