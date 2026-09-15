package com.example.myappdemo

import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherCodeMapperTest {
    @Test
    fun mapsCommonWmoCodes() {
        assertEquals("晴", WeatherCodeMapper.description(0))
        assertEquals("雨", WeatherCodeMapper.description(63))
        assertEquals("雪", WeatherCodeMapper.description(75))
        assertEquals("雷暴伴冰雹", WeatherCodeMapper.description(99))
    }

    @Test
    fun mapsUnknownCodeSafely() {
        assertEquals("未知天气", WeatherCodeMapper.description(1000))
        assertEquals("?", WeatherCodeMapper.icon(1000, true))
    }
}
