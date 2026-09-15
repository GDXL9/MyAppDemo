package com.example.myappdemo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherCitiesTest {
    @Test
    fun coversAllProvinceLevelRegions() {
        assertEquals(34, WeatherCities.PROVINCES.size)
        assertTrue(WeatherCities.PROVINCES.containsAll(listOf(
            "北京市", "河北省", "内蒙古自治区", "香港特别行政区", "澳门特别行政区", "台湾省"
        )))
    }

    @Test
    fun everyProvinceHasCitiesAndIdsAreUnique() {
        assertTrue(WeatherCities.PROVINCES.all { WeatherCities.citiesForProvince(it).isNotEmpty() })
        assertEquals(WeatherCities.ALL.size, WeatherCities.ALL.map { it.id }.distinct().size)
    }

    @Test
    fun coordinatesAreInChinaAreaAndLookupWorks() {
        assertTrue(WeatherCities.ALL.all {
            it.latitude in 3.0..54.0 && it.longitude in 73.0..136.0
        })
        assertEquals("广东省", WeatherCities.findById("shenzhen").province)
        assertEquals(WeatherCities.DEFAULT, WeatherCities.findById("not_found"))
    }
}
