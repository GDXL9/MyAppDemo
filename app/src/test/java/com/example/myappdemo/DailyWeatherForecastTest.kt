package com.example.myappdemo

import org.junit.Assert.assertEquals
import org.junit.Test

class DailyWeatherForecastTest {
    @Test
    fun widgetTextContainsIconAndHighLowTemperature() {
        val forecast = DailyWeatherForecast(
            epochDay = 20_000,
            weatherCode = 61,
            minTemperature = 18,
            maxTemperature = 27
        )
        assertEquals("🌧 雨\n27°/18°", forecast.widgetText())
    }
}
