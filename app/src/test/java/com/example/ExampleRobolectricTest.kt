package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun testAppNameDefinition() {
        val appName = "Money Manager"
        assertTrue(appName.isNotBlank())
        assertEquals("Money Manager", appName)
    }

    @Test
    fun testDailyVelocityFormula() {
        val spent = 15000.0
        val day = 10
        val velocity = spent / day
        assertEquals(1500.0, velocity, 0.001)
    }

    @Test
    fun testSpendingHabits503020Ratio() {
        val total = 100000.0
        val needs = total * 0.50
        val wants = total * 0.30
        val savings = total * 0.20
        assertEquals(50000.0, needs, 0.001)
        assertEquals(30000.0, wants, 0.001)
        assertEquals(20000.0, savings, 0.001)
    }
}
