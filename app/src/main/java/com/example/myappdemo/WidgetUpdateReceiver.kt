package com.example.myappdemo

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.*

/**
 * 小组件定时更新接收器
 * 
 * 用于在每天凌晨0点自动更新所有小组件
 */
class WidgetUpdateReceiver : BroadcastReceiver() {

    companion object {
        private const val ACTION_UPDATE_WIDGET = "com.example.myappdemo.action.UPDATE_WIDGET"
        private const val REQUEST_CODE = 1001

        /**
         * 设置凌晨0点的定时更新任务
         * 
         * @param context 上下文对象
         */
        fun setDailyUpdate(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            // 创建定时触发的 Intent
            val intent = Intent(context, WidgetUpdateReceiver::class.java)
            intent.action = ACTION_UPDATE_WIDGET
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // 设置明天凌晨0点的时间
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // 设置重复闹钟，每天凌晨0点触发
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }

        /**
         * 取消定时更新任务
         * 
         * @param context 上下文对象
         */
        fun cancelDailyUpdate(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            val intent = Intent(context, WidgetUpdateReceiver::class.java)
            intent.action = ACTION_UPDATE_WIDGET
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            alarmManager.cancel(pendingIntent)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_UPDATE_WIDGET -> {
                // 更新所有小组件
                updateAllWidgets(context)
                
                // 重新设置第二天的定时任务
                setDailyUpdate(context)
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                // 开机后重新设置定时任务
                setDailyUpdate(context)
            }
        }
    }

    /**
     * 更新所有类型的小组件
     */
    private fun updateAllWidgets(context: Context) {
        updateWidgets(context, ShiftWidget::class.java)
        updateWidgets(context, ShiftWidget1x4::class.java)
    }

    /**
     * 更新指定类型的小组件
     */
    private fun updateWidgets(context: Context, widgetClass: Class<out android.appwidget.AppWidgetProvider>) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = android.content.ComponentName(context, widgetClass)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        
        if (appWidgetIds.isNotEmpty()) {
            val intent = Intent(context, widgetClass)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            context.sendBroadcast(intent)
        }
    }
}