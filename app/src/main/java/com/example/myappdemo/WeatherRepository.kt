package com.example.myappdemo

import android.content.Context
import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.Executors
import kotlin.math.roundToInt

/** 获取并按城市缓存 Open-Meteo 当前天气与短时特殊天气预报。 */
object WeatherRepository {
    private const val PREFS_NAME = "weather_cache"
    private const val FRESH_MILLIS = 30 * 60 * 1000L
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    private data class WeatherResponse(
        val current: WeatherInfo,
        val dailyForecasts: List<DailyWeatherForecast>,
        val alertCandidate: WeatherAlertCandidate?
    )

    fun getCached(context: Context, city: WeatherCity): WeatherInfo? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.contains("${city.id}_time")) return null
        return WeatherInfo(
            cityName = city.name,
            temperature = prefs.getInt("${city.id}_temp", 0),
            weatherCode = prefs.getInt("${city.id}_code", -1),
            description = prefs.getString("${city.id}_description", "未知天气") ?: "未知天气",
            icon = prefs.getString("${city.id}_icon", "?") ?: "?",
            updatedAtMillis = prefs.getLong("${city.id}_time", 0L)
        )
    }

    fun getCachedDailyForecasts(context: Context, city: WeatherCity): Map<Long, DailyWeatherForecast> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("${city.id}_daily", null)
            ?.split(";")
            ?.mapNotNull { entry ->
                val values = entry.split(",")
                if (values.size != 4) return@mapNotNull null
                runCatching {
                    DailyWeatherForecast(
                        epochDay = values[0].toLong(),
                        weatherCode = values[1].toInt(),
                        minTemperature = values[2].toInt(),
                        maxTemperature = values[3].toInt()
                    )
                }.getOrNull()
            }
            ?.associateBy { it.epochDay }
            .orEmpty()
    }

    fun isFresh(info: WeatherInfo): Boolean =
        System.currentTimeMillis() - info.updatedAtMillis < FRESH_MILLIS

    fun load(
        context: Context,
        forceRefresh: Boolean = false,
        callback: (WeatherInfo?) -> Unit
    ) {
        val appContext = context.applicationContext
        val requestedCity = WeatherCities.findById(SettingsManager.getWeatherCityId(appContext))
        val cached = getCached(appContext, requestedCity)
        if (!forceRefresh && cached != null && isFresh(cached)) {
            callback(cached)
            return
        }
        executor.execute {
            val response = runCatching { fetch(requestedCity) }.getOrNull()
            val result = response?.current
            if (result != null && SettingsManager.getWeatherCityId(appContext) == requestedCity.id) {
                save(appContext, requestedCity, result, response.dailyForecasts)
                WeatherAlertNotifier.evaluateAndNotify(appContext, requestedCity, response.alertCandidate)
            }
            mainHandler.post { callback(result ?: cached) }
        }
    }

    /** 供 JobService 在后台线程中同步调用。 */
    fun refreshBlocking(context: Context): WeatherInfo? {
        val city = WeatherCities.findById(SettingsManager.getWeatherCityId(context))
        val response = runCatching { fetch(city) }.getOrNull() ?: return getCached(context, city)
        if (SettingsManager.getWeatherCityId(context) == city.id) {
            save(context, city, response.current, response.dailyForecasts)
            WeatherAlertNotifier.evaluateAndNotify(context, city, response.alertCandidate)
        }
        return response.current
    }

    private fun fetch(city: WeatherCity): WeatherResponse {
        val endpoint = "https://api.open-meteo.com/v1/forecast" +
            "?latitude=${city.latitude}&longitude=${city.longitude}" +
            "&current=temperature_2m,weather_code,is_day" +
            "&hourly=weather_code,precipitation_probability" +
            "&daily=weather_code,temperature_2m_max,temperature_2m_min" +
            "&past_days=6&forecast_days=8&temperature_unit=celsius&timezone=auto&timeformat=unixtime"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8_000
            readTimeout = 10_000
            setRequestProperty("Accept", "application/json")
        }
        try {
            if (connection.responseCode !in 200..299) {
                throw IllegalStateException("Open-Meteo HTTP ${connection.responseCode}")
            }
            val body = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            require(body.length <= 1_000_000) { "Weather response is too large" }
            val root = JSONObject(body)
            val current = root.getJSONObject("current")
            val temperature = current.getDouble("temperature_2m").roundToInt()
            val code = current.getInt("weather_code")
            val isDay = current.optInt("is_day", 1) == 1
            val info = WeatherInfo(
                cityName = city.name,
                temperature = temperature,
                weatherCode = code,
                description = WeatherCodeMapper.description(code),
                icon = WeatherCodeMapper.icon(code, isDay),
                updatedAtMillis = System.currentTimeMillis()
            )
            return WeatherResponse(info, parseDailyForecasts(root), parseAlertCandidate(root))
        } finally {
            connection.disconnect()
        }
    }

    private fun parseDailyForecasts(root: JSONObject): List<DailyWeatherForecast> {
        val daily = root.optJSONObject("daily") ?: return emptyList()
        val times = daily.optJSONArray("time") ?: return emptyList()
        val codes = daily.optJSONArray("weather_code") ?: return emptyList()
        val maxTemperatures = daily.optJSONArray("temperature_2m_max") ?: return emptyList()
        val minTemperatures = daily.optJSONArray("temperature_2m_min") ?: return emptyList()
        val utcOffset = root.optLong("utc_offset_seconds", 0L)
        val count = minOf(times.length(), codes.length(), maxTemperatures.length(), minTemperatures.length())
        return (0 until count).mapNotNull { index ->
            runCatching {
                val date = Instant.ofEpochSecond(times.getLong(index) + utcOffset)
                    .atZone(ZoneId.of("UTC"))
                    .toLocalDate()
                DailyWeatherForecast(
                    epochDay = date.toEpochDay(),
                    weatherCode = codes.getInt(index),
                    minTemperature = minTemperatures.getDouble(index).roundToInt(),
                    maxTemperature = maxTemperatures.getDouble(index).roundToInt()
                )
            }.getOrNull()
        }
    }

    private fun parseAlertCandidate(root: JSONObject): WeatherAlertCandidate? {
        val hourly = root.optJSONObject("hourly") ?: return null
        val times = hourly.optJSONArray("time") ?: return null
        val codes = hourly.optJSONArray("weather_code") ?: return null
        val probabilities = hourly.optJSONArray("precipitation_probability") ?: return null
        val nowSeconds = Instant.now().epochSecond
        val endSeconds = nowSeconds + 3 * 60 * 60
        var best: WeatherAlertCandidate? = null
        val count = minOf(times.length(), codes.length(), probabilities.length())
        for (index in 0 until count) {
            val eventSeconds = times.optLong(index, 0L)
            if (eventSeconds < nowSeconds || eventSeconds > endSeconds) continue
            val code = codes.optInt(index, -1)
            val probability = probabilities.optInt(index, 0).coerceIn(0, 100)
            val severity = WeatherAlertRules.severity(code, probability)
            if (severity == 0) continue
            val candidate = WeatherAlertCandidate(code, severity, eventSeconds * 1000, probability)
            if (best == null || candidate.severity > best.severity ||
                (candidate.severity == best.severity && candidate.eventTimeMillis < best.eventTimeMillis)
            ) {
                best = candidate
            }
        }
        return best
    }

    private fun save(
        context: Context,
        city: WeatherCity,
        info: WeatherInfo,
        dailyForecasts: List<DailyWeatherForecast>
    ) {
        val serializedDaily = dailyForecasts.joinToString(";") {
            "${it.epochDay},${it.weatherCode},${it.minTemperature},${it.maxTemperature}"
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt("${city.id}_temp", info.temperature)
            .putInt("${city.id}_code", info.weatherCode)
            .putString("${city.id}_description", info.description)
            .putString("${city.id}_icon", info.icon)
            .putLong("${city.id}_time", info.updatedAtMillis)
            .putString("${city.id}_daily", serializedDaily)
            .apply()
    }
}
