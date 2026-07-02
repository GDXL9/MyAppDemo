package com.example.myappdemo

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import java.util.Calendar
import kotlin.math.log

class WidgetUpdateReceiver : BroadcastReceiver() {

    companion object {
        private const val ACTION_UPDATE_WIDGET = "com.example.myappdemo.action.UPDATE_WIDGET"

        /**
         * 设置每天凌晨0点定时更新。
         * 在 Android 12+ 上，如果没有精确闹钟权限，仅记录日志，不做强制要求（功能降级）。
         */
        fun setDailyUpdate(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    // 没有权限，不设置闹钟，但可通知用户或静默失败
                    return
                }
            }

            val intent = Intent(context, WidgetUpdateReceiver::class.java).apply {
                action = ACTION_UPDATE_WIDGET
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, 1001, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // 计算明天凌晨0点
            val triggerTime = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            // 设置定时更新，每天凌晨 0 点触发
            try {
                // Android 12+ (API 31) 要求用户明确授权“闹铃和提醒”才能设置精确闹钟。
                // 如果未授权，canScheduleExactAlarms() 返回 false，但此时不直接终止，
                // 而是让后续的 setExactAndAllowWhileIdle 抛出 SecurityException，
                // 由 catch 统一降级处理，避免因权限检查遗漏导致功能完全失效。
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Log.d("WidgetUpdate", "Android 12+ 支持精确闹钟")
                    if (!alarmManager.canScheduleExactAlarms()) {
                        // 权限不足，不设置精确闹钟，等待 catch 降级。
                        // 若希望引导用户授权，可在此处通过 PendingIntent 跳转到系统设置，
                        // 但 setDailyUpdate 通常在后台调用，不宜直接启动 Activity，
                        // 建议在应用主界面检测权限并提示用户。
                    }
                }

                // Android 6.0+ (API 23) 引入 Doze 模式，setExactAndAllowWhileIdle 在设备空闲时
                // 仍能准时触发闹钟，比 setExact 更可靠。Android 12+ 需要额外权限（见上）。
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Log.d("WidgetUpdate", "Android 6.0+ 支持精确闹钟")
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    Log.d("WidgetUpdate", "Android 5.0- 不支持精确闹钟")
                    // 旧版系统直接使用 setExact
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            } catch (e: SecurityException) {
                Log.d("WidgetUpdate", "Android 12+ 无精确闹钟权限")
                // Android 12+ 无精确闹钟权限时会抛出此异常，降级为非精确闹钟。
                // 非精确闹钟可能因系统省电策略延迟，但能保证功能基本可用。
                try {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    Log.w("WidgetUpdate", "无精确闹钟权限，已降级为普通闹钟")
                } catch (ex: Exception) {
                    Log.e("WidgetUpdate", "无法设置任何闹钟，小组件将不会自动更新", ex)
                }
            }
        }

        /**
         * 取消每天凌晨0点定时更新。
         */
        fun cancelDailyUpdate(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, WidgetUpdateReceiver::class.java).apply {
                action = ACTION_UPDATE_WIDGET
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, 1001, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    /**
     * 处理广播接收。
     */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_UPDATE_WIDGET) {
            // 重新加载配置（可能用户已修改设置）
            ShiftConfig.init(context)
            // 直接更新所有小组件（不再使用广播）
            WidgetViewsFactory.updateAllWidgets(context)

            // 注册下一次定时
            setDailyUpdate(context)
        }
    }
}