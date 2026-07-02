package com.example.myappdemo

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

/**
 * 应用入口，初始化全局配置
 */
class ShiftCalendarApp : Application() {

    private var configReceiver: BroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()
        // 应用保存的夜间模式
        ThemeHelper.applyNightMode(this)

        // 动态注册配置变化广播，监听系统夜间模式切换
        registerConfigReceiver()
    }

    /**
     * 动态注册 ACTION_CONFIGURATION_CHANGED 广播。
     * 此广播从 Android 8.0 起无法通过静态 manifest 注册，必须在代码中动态注册。
     */
    private fun registerConfigReceiver() {
        configReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED) {
                    ShiftConfig.init(context)
                    WidgetViewsFactory.updateAllWidgets(context)
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED)
        registerReceiver(configReceiver, filter)
    }

    override fun onTerminate() {
        super.onTerminate()
        configReceiver?.let { unregisterReceiver(it) }
    }
}
