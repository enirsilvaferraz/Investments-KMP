package com.eferraz.presentation.design_system.components.inputs.state

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MoneyInputStateTest {

    @Test
    fun `GIVEN initial value WHEN initialized THEN should format correct string`() {
        val state = MoneyInputState(10.0)
        assertEquals("1000", state.textFieldValue.text)
    }

    @Test
    fun `GIVEN input value WHEN onValueChange THEN should update currentAmount`() {
        val state = MoneyInputState(0.0)
        state.onValueChange(TextFieldValue("1234", selection = TextRange(4)))
        assertEquals(12.34, state.currentAmount)
        assertEquals("1234", state.textFieldValue.text)
    }

    @Test
    fun `GIVEN empty input WHEN onValueChange THEN should set currentAmount to null`() {
        val state = MoneyInputState(10.0)
        state.onValueChange(TextFieldValue("", selection = TextRange(0)))
        assertNull(state.currentAmount)
        assertEquals("", state.textFieldValue.text)
    }

    @Test
    fun `GIVEN non-digit input WHEN onValueChange THEN should filter and update`() {
        val state = MoneyInputState(0.0)
        state.onValueChange(TextFieldValue("12a34", selection = TextRange(5)))
        assertEquals(12.34, state.currentAmount)
        assertEquals("1234", state.textFieldValue.text)
    }

    @Test
    fun `GIVEN external value update WHEN syncWithExternalValue THEN should update text field`() {
        val state = MoneyInputState(0.0)
        state.syncWithExternalValue(50.0)
        assertEquals("5000", state.textFieldValue.text)
    }
}
