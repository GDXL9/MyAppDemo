package com.example.myappdemo

import android.app.Application

/**
 * 应用入口，初始化全局配置
 */
class ShiftCalendarApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // 应用保存的夜间模式
        ThemeHelper.applyNightMode(this)
    }
}
