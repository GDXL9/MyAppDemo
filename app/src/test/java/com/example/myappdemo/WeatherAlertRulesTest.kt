package com.example.myappdemo

import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherAlertRulesTest {
    @Test
    fun thunderstormAndHailAreSevere() {
        assertEquals(3, WeatherAlertRules.severity(95, 20))
        assertEquals(3, WeatherAlertRules.severity(99, 90))
    }

    @Test
    fun dangerousPrecipitationIsWarning() {
        assertEquals(2, WeatherAlertRules.severity(65, 30))
        assertEquals(2, WeatherAlertRules.severity(67, 80))
        assertEquals(2, WeatherAlertRules.severity(86, 75))
    }

    @Test
    fun ordinaryRainNeedsHighProbability() {
        assertEquals(0, WeatherAlertRules.severity(61, 69))
        assertEquals(2, WeatherAlertRules.severity(61, 70))
    }

    @Test
    fun normalWeatherDoesNotAlert() {
        assertEquals(0, WeatherAlertRules.severity(0, 100))
        assertEquals(0, WeatherAlertRules.severity(3, 100))
        assertEquals(0, WeatherAlertRules.severity(45, 100))
    }
}
