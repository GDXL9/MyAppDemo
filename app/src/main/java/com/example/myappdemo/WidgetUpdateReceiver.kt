package com.example.myappdemo

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import java.util.*

/**
 * 小组件定时更新广播接收器
 * 
 * 用于在每天凌晨0点自动更新所有桌面小组件，确保排班信息及时更新
 * 
 * 主要功能：
 * 1. 设置每天凌晨0点的定时更新任务
 * 2. 触发时更新所有类型的小组件（ShiftWidget 和 ShiftWidget1x4）
 * 3. 处理 Android 12+ 的精确闹钟权限检查
 */
class WidgetUpdateReceiver : BroadcastReceiver() {

    companion object {
        /**
         * 自定义广播动作：更新小组件
         * 用于标识定时任务触发时的广播意图
         */
        private const val ACTION_UPDATE_WIDGET = "com.example.myappdemo.action.UPDATE_WIDGET"
        
        /**
         * 请求码：用于标识定时任务的 PendingIntent
         */
        private const val REQUEST_CODE = 1001

        /**
         * 设置每天凌晨0点的定时更新任务
         * 
         * 在 Android 12+ 上需要先检查 SCHEDULE_EXACT_ALARM 权限
         * 如果没有权限，会弹出对话框引导用户开启
         * 
         * @param context 上下文对象
         */
        fun setDailyUpdate(context: Context) {
            // Android 12+ 需要检查精确闹钟权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (!alarmManager.canScheduleExactAlarms()) {
                    // 没有权限，显示引导对话框
                    showPermissionDialog(context)
                    return
                }
            }

            // 获取 AlarmManager 系统服务
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            // 创建定时触发的 Intent，指向本接收器
            val intent = Intent(context, WidgetUpdateReceiver::class.java)
            intent.action = ACTION_UPDATE_WIDGET
            
            // 创建 PendingIntent，用于定时触发广播
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // 设置明天凌晨0点的时间
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.DAY_OF_MONTH, 1)      // 明天
                set(Calendar.HOUR_OF_DAY, 0)       // 0点
                set(Calendar.MINUTE, 0)            // 0分
                set(Calendar.SECOND, 0)            // 0秒
                set(Calendar.MILLISECOND, 0)       // 0毫秒
            }

            // 设置精确闹钟
            // Android 6.0+ 使用 setExactAndAllowWhileIdle，支持低功耗模式
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,      // RTC_WAKEUP: 唤醒设备
                    calendar.timeInMillis,         // 触发时间
                    pendingIntent                  // 触发时执行的动作
                )
            } else {
                // 兼容 Android 6.0 以下版本
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }

        /**
         * 显示权限请求对话框
         * 
         * 引导用户开启"允许后台行为"权限，以便使用精确闹钟功能
         * 
         * @param context 上下文对象
         */
        private fun showPermissionDialog(context: Context) {
            androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("需要开启后台权限")
                .setMessage("为了让小组件在每天凌晨自动更新，需要开启'允许后台行为'权限。")
                .setPositiveButton("去开启") { _, _ ->
                    // 跳转到系统设置页面开启权限
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    context.startActivity(intent)
                }
                .setNegativeButton("取消", null)
                .show()
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
            
            // 取消定时任务
            alarmManager.cancel(pendingIntent)
        }
    }

    /**
     * 接收到广播时触发
     * 
     * @param context 上下文对象
     * @param intent 接收到的意图
     */
    override fun onReceive(context: Context, intent: Intent) {
        // 检查是否是我们自定义的更新动作
        if (intent.action == ACTION_UPDATE_WIDGET) {
            // 更新所有小组件
            updateAllWidgets(context)
            
            // 重新设置第二天的定时任务（实现每天循环）
            setDailyUpdate(context)
        }
    }

    /**
     * 更新所有类型的小组件
     * 
     * @param context 上下文对象
     */
    private fun updateAllWidgets(context: Context) {
        // 更新标准小组件（ShiftWidget）
        updateWidgets(context, ShiftWidget::class.java)
        // 更新1x4单行小组件（ShiftWidget1x4）
        updateWidgets(context, ShiftWidget1x4::class.java)
    }

    /**
     * 更新指定类型的小组件
     * 
     * 通过发送广播通知 AppWidgetManager 更新小组件
     * 
     * @param context 上下文对象
     * @param widgetClass 小组件类
     */
    private fun updateWidgets(context: Context, widgetClass: Class<out android.appwidget.AppWidgetProvider>) {
        // 获取 AppWidgetManager 实例
        val appWidgetManager = AppWidgetManager.getInstance(context)
        // 获取该类型所有已添加的小组件ID
        val componentName = android.content.ComponentName(context, widgetClass)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        
        // 如果有该类型的小组件，发送更新广播
        if (appWidgetIds.isNotEmpty()) {
            val intent = Intent(context, widgetClass)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            context.sendBroadcast(intent)
        }
    }
}