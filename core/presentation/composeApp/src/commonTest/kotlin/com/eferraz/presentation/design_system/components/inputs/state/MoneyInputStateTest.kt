package com.eferraz.presentation.design_system.components.inputs.state

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Unit tests for [MoneyInputState] text field and amount parsing.
 */
class MoneyInputStateTest {

    /**
     * Initial amount is exposed as scaled digits in the text field (e.g. cents).
     */
    @Test
    fun `GIVEN initial value WHEN initialized THEN formatted text matches internal scale`() {
        // GIVEN
        val initialAmount = 10.0

        // WHEN
        val state = MoneyInputState(initialAmount)

        // THEN
        assertEquals("1000", state.textFieldValue.text)
    }

    /**
     * Typing updates [MoneyInputState.currentAmount] and normalizes the text buffer.
     */
    @Test
    fun `GIVEN input value WHEN onValueChange THEN currentAmount reflects parsed decimal`() {
        // GIVEN
        val state = MoneyInputState(0.0)

        // WHEN
        state.onValueChange(TextFieldValue("1234", selection = TextRange(4)))

        // THEN
        val amount = assertNotNull(state.currentAmount)
        assertEquals(12.34, amount, 0.01)
        assertEquals("1234", state.textFieldValue.text)
    }

    /**
     * Clearing the field clears the parsed amount.
     */
    @Test
    fun `GIVEN empty input WHEN onValueChange THEN currentAmount is null`() {
        // GIVEN
        val state = MoneyInputState(10.0)

        // WHEN
        state.onValueChange(TextFieldValue("", selection = TextRange(0)))

        // THEN
        assertNull(state.currentAmount)
        assertEquals("", state.textFieldValue.text)
    }

    /**
     * Non-digit characters are stripped before parsing the monetary value.
     */
    @Test
    fun `GIVEN non-digit input WHEN onValueChange THEN digits are filtered and amount updates`() {
        // GIVEN
        val state = MoneyInputState(0.0)

        // WHEN
        state.onValueChange(TextFieldValue("12a34", selection = TextRange(5)))

        // THEN
        val amount = assertNotNull(state.currentAmount)
        assertEquals(12.34, amount, 0.01)
        assertEquals("1234", state.textFieldValue.text)
    }

    /**
     * External model updates resync the visible text to the scaled representation.
     */
    @Test
    fun `GIVEN external value update WHEN syncWithExternalValue THEN text field updates`() {
        // GIVEN
        val state = MoneyInputState(0.0)

        // WHEN
        state.syncWithExternalValue(50.0)

        // THEN
        assertEquals("5000", state.textFieldValue.text)
    }
}
