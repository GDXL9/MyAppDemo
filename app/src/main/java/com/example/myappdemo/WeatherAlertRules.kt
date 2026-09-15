package com.example.myappdemo

data class WeatherAlertCandidate(
    val weatherCode: Int,
    val severity: Int,
    val eventTimeMillis: Long,
    val precipitationProbability: Int
)

/** 特殊天气风险分级。0=无需提醒，1=注意，2=警告，3=严重。 */
object WeatherAlertRules {
    fun severity(weatherCode: Int, precipitationProbability: Int): Int = when (weatherCode) {
        96, 99 -> 3 // 雷暴伴冰雹
        95 -> 3     // 雷暴
        65, 66, 67, 75, 82, 86 -> 2 // 大雨、冻雨、大雪、强阵雨/雪
        61, 63, 71, 73, 80, 81, 85 ->
            if (precipitationProbability >= 70) 2 else 0
        else -> 0
    }

    fun title(severity: Int): String = when (severity) {
        3 -> "严重天气提醒"
        2 -> "特殊天气提醒"
        else -> "天气提示"
    }
}
