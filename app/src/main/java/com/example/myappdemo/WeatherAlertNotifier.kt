package com.example.myappdemo

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/** 特殊天气通知与去重。 */
object WeatherAlertNotifier {
    private const val CHANNEL_ID = "weather_alerts"
    private const val NOTIFICATION_ID = 3001
    private const val PREFS_NAME = "weather_alert_state"
    private const val COOLDOWN_MILLIS = 6 * 60 * 60 * 1000L

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.weather_alert_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.weather_alert_channel_description)
            }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    fun evaluateAndNotify(context: Context, city: WeatherCity, candidate: WeatherAlertCandidate?) {
        if (!SettingsManager.areWeatherAlertsEnabled(context)) return
        if (candidate == null || candidate.severity == 0) {
            clearActiveState(context, city.id)
            return
        }
        if (!hasPermission(context)) return

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastCity = prefs.getString("city", null)
        val lastCode = prefs.getInt("code", -1)
        val lastSeverity = prefs.getInt("severity", 0)
        val lastTime = prefs.getLong("notified_at", 0L)
        val now = System.currentTimeMillis()
        val shouldNotify = lastCity != city.id ||
            lastCode == -1 ||
            candidate.severity > lastSeverity ||
            now - lastTime >= COOLDOWN_MILLIS
        if (!shouldNotify) return

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val minutes = ((candidate.eventTimeMillis - now).coerceAtLeast(0L) / 60_000L).toInt()
        val timing = if (minutes < 60) "未来约${minutes.coerceAtLeast(1)}分钟" else "未来约${minutes / 60}小时"
        val description = WeatherCodeMapper.description(candidate.weatherCode)
        val content = "$timing，${city.name}可能出现$description，降水概率${candidate.precipitationProbability}%，请注意防范。"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(WeatherAlertRules.title(candidate.severity))
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            return
        }
        prefs.edit()
            .putString("city", city.id)
            .putInt("code", candidate.weatherCode)
            .putInt("severity", candidate.severity)
            .putLong("notified_at", now)
            .apply()
    }

    fun hasPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun clearActiveState(context: Context, cityId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getString("city", null) == cityId) prefs.edit().clear().apply()
    }
}
