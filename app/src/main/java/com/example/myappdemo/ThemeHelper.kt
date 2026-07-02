package com.example.myappdemo

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

/**
 * 夜间模式管理工具
 */
object ThemeHelper {

    /**
     * 应用保存的夜间模式设置
     */
    fun applyNightMode(context: Context) {
        val mode = SettingsManager.getNightMode(context)
        applyMode(mode)
    }

    /**
     * 应用指定夜间模式
     */
    fun applyMode(mode: Int) {
        val delegateMode = when (mode) {
            SettingsManager.NIGHT_MODE_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            SettingsManager.NIGHT_MODE_DARK  -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(delegateMode)
    }
}
