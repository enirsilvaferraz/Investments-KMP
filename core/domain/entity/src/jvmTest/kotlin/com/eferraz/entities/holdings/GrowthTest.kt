package com.eferraz.entities.holdings

import org.junit.Assert
import org.junit.Test

class GrowthTest {

    @Test
    fun `GIVEN pure appreciation without contributions WHEN calculating THEN should return correct growth and percentage`() {

        // WHEN
        val result = Growth.calculate(
            previousValue = 1000.0,
            currentValue = 1100.0,
            contributions = 0.0,
            withdrawals = 0.0
        )

        // THEN
        Assert.assertEquals(100.0, result.value, 0.001)
        Assert.assertEquals(10.0, result.percentage, 0.001)
    }

    @Test
    fun `GIVEN appreciation with contribution WHEN calculating THEN should return correct growth and percentage`() {

        // WHEN
        val result = Growth.calculate(
            previousValue = 1000.0,
            currentValue = 1600.0,
            contributions = 500.0,
            withdrawals = 0.0
        )

        // THEN
        Assert.assertEquals(600.0, result.value, 0.001)
        Assert.assertEquals(60.0, result.percentage, 0.001)
    }

    @Test
    fun `GIVEN appreciation with withdrawal WHEN calculating THEN should return correct growth and percentage`() {

        // WHEN
        val result = Growth.calculate(
            previousValue = 1000.0,
            currentValue = 900.0,
            contributions = 0.0,
            withdrawals = 100.0
        )

        // THEN
        Assert.assertEquals(-100.0, result.value, 0.001)
        Assert.assertEquals(-10.0, result.percentage, 0.001)
    }

    @Test
    fun `GIVEN only contributions with appreciation WHEN calculating THEN should return correct growth and percentage`() {

        // WHEN
        val result = Growth.calculate(
            previousValue = 0.0,
            currentValue = 1500.0,
            contributions = 1000.0,
            withdrawals = 0.0
        )

        // THEN
        Assert.assertEquals(500.0, result.value, 0.001)
        Assert.assertEquals(50.0, result.percentage, 0.001)
    }

    @Test
    fun `GIVEN loss with contributions WHEN calculating THEN should return correct growth and percentage`() {

        // WHEN
        val result = Growth.calculate(
            previousValue = 1000.0,
            currentValue = 1400.0,
            contributions = 500.0,
            withdrawals = 0.0
        )

        // THEN
        Assert.assertEquals(400.0, result.value, 0.001)
        Assert.assertEquals(40.0, result.percentage, 0.001)
    }

    @Test
    fun `GIVEN profit with partial withdrawal WHEN calculating THEN should return correct growth and percentage`() {

        // WHEN
        val result = Growth.calculate(
            previousValue = 1000.0,
            currentValue = 850.0,
            contributions = 0.0,
            withdrawals = 150.0
        )

        // THEN
        Assert.assertEquals(-150.0, result.value, 0.001)
        Assert.assertEquals(-15.0, result.percentage, 0.001)
    }

    @Test
    fun `GIVEN first month without previous value WHEN calculating THEN should return correct growth and percentage`() {

        // WHEN
        val result = Growth.calculate(
            previousValue = 0.0,
            currentValue = 1000.0,
            contributions = 1000.0,
            withdrawals = 0.0
        )

        // THEN
        Assert.assertEquals(0.0, result.value, 0.001)
        Assert.assertEquals(0.0, result.percentage, 0.001)
    }

    @Test
    fun `GIVEN first month with depreciation value WHEN calculating THEN should return correct growth and percentage`() {

        // WHEN
        val result = Growth.calculate(
            previousValue = 0.0,
            currentValue = 900.0,
            contributions = 1000.0,
            withdrawals = 0.0
        )

        // THEN
        Assert.assertEquals(-100.0, result.value, 0.001)
        Assert.assertEquals(-10.0, result.percentage, 0.001)
    }

    @Test
    fun `GIVEN negative growth WHEN calculating THEN should return correct negative values`() {

        // WHEN
        val result = Growth.calculate(
            previousValue = 2000.0,
            currentValue = 1500.0,
            contributions = 0.0,
            withdrawals = 0.0
        )

        // THEN
        Assert.assertEquals(-500.0, result.value, 0.001)
        Assert.assertEquals(-25.0, result.percentage, 0.001)
    }

    @Test
    fun `GIVEN no previous value and no contributions WHEN calculating THEN should return zero`() {

        // WHEN
        val result = Growth.calculate(
            previousValue = 0.0,
            currentValue = 0.0,
            contributions = 0.0,
            withdrawals = 0.0
        )

        // THEN
        Assert.assertEquals(0.0, result.value, 0.001)
        Assert.assertEquals(0.0, result.percentage, 0.001)
    }
}