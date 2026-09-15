package com.example.myappdemo

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ShiftCalculatorTest {
    private val cycle = listOf("白", "夜", "休", "休")
    private val baseDate = LocalDate.of(2026, 4, 25)

    @Test
    fun baseDateUsesFirstShift() {
        assertEquals("白", ShiftCalculator.getShiftForDate(baseDate, baseDate, cycle))
    }

    @Test
    fun datesAfterBaseDateCycleForward() {
        assertEquals("夜", ShiftCalculator.getShiftForDate(baseDate.plusDays(1), baseDate, cycle))
        assertEquals("白", ShiftCalculator.getShiftForDate(baseDate.plusDays(4), baseDate, cycle))
    }

    @Test
    fun datesBeforeBaseDateCycleBackward() {
        assertEquals("休", ShiftCalculator.getShiftForDate(baseDate.minusDays(1), baseDate, cycle))
        assertEquals("白", ShiftCalculator.getShiftForDate(baseDate.minusDays(4), baseDate, cycle))
    }

    @Test
    fun calculationWorksAcrossLeapDay() {
        val leapBase = LocalDate.of(2028, 2, 28)
        assertEquals("休", ShiftCalculator.getShiftForDate(LocalDate.of(2028, 3, 1), leapBase, cycle))
    }

    @Test
    fun emptyCycleFallsBackToRest() {
        assertEquals("休", ShiftCalculator.getShiftForDate(baseDate, baseDate, emptyList()))
    }
}
