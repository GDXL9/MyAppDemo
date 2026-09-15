package com.example.myappdemo

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.time.ZonedDateTime

/** 负责跨日、开机、时间或时区变化后的 Widget 刷新与重新调度。 */
class WidgetUpdateReceiver : BroadcastReceiver() {
    companion object {
        private const val ACTION_UPDATE_WIDGET = "com.example.myappdemo.action.UPDATE_WIDGET"
        private const val REQUEST_CODE = 1001

        /**
         * 使用一次性非精确闹钟在下一个本地零点后触发，不再申请精确闹钟特殊权限。
         * 每次触发都会根据当前时区重新计算下一次，避免固定 24 小时带来的漂移。
         */
        fun setDailyUpdate(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val now = ZonedDateTime.now()
            val triggerTime = now.toLocalDate().plusDays(1)
                .atStartOfDay(now.zone)
                .plusMinutes(2)
                .toInstant()
                .toEpochMilli()
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                createPendingIntent(context)
            )
        }

        fun cancelDailyUpdate(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(createPendingIntent(context))
        }

        private fun createPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, WidgetUpdateReceiver::class.java).apply {
                action = ACTION_UPDATE_WIDGET
            }
            return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_UPDATE_WIDGET,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                ShiftConfig.init(context)
                WidgetViewsFactory.updateAllWidgets(context)
                setDailyUpdate(context)
                WeatherUpdateJobService.ensurePeriodic(context)
                WeatherUpdateJobService.scheduleImmediate(context)
            }
        }
    }
}
