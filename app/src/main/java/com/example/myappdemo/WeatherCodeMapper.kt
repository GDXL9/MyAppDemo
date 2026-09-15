package com.example.myappdemo

/** Open-Meteo 使用的 WMO 天气代码展示映射。 */
object WeatherCodeMapper {
    fun description(code: Int): String = when (code) {
        0 -> "晴"
        1 -> "大部晴朗"
        2 -> "局部多云"
        3 -> "阴"
        45, 48 -> "雾"
        51, 53, 55 -> "毛毛雨"
        56, 57 -> "冻毛毛雨"
        61, 63, 65 -> "雨"
        66, 67 -> "冻雨"
        71, 73, 75 -> "雪"
        77 -> "米雪"
        80, 81, 82 -> "阵雨"
        85, 86 -> "阵雪"
        95 -> "雷暴"
        96, 99 -> "雷暴伴冰雹"
        else -> "未知天气"
    }

    fun icon(code: Int, isDay: Boolean): String = when (code) {
        0 -> if (isDay) "☀" else "☾"
        1 -> if (isDay) "🌤" else "☾"
        2 -> "⛅"
        3 -> "☁"
        45, 48 -> "🌫"
        51, 53, 55, 56, 57 -> "🌦"
        61, 63, 65, 66, 67, 80, 81, 82 -> "🌧"
        71, 73, 75, 77, 85, 86 -> "🌨"
        95, 96, 99 -> "⛈"
        else -> "?"
    }
}
